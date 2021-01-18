package org.terraform.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.drycell.command.DCArgument;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.LangOpt;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.*;
import org.terraform.structure.stronghold.StrongholdPopulator;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;
import java.util.UUID;


public class LocateCommand extends DCCommand implements Listener {

    public LocateCommand(DrycellPlugin plugin, String... aliases) {
        super(plugin, aliases);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.parameters.add(new StructurePopulatorArgument("structureType", true));
    }

    @EventHandler
    public void onLocateCommand(PlayerCommandPreprocessEvent event) {
        if (event.getPlayer().getWorld().getGenerator() instanceof TerraformGenerator) {
            if (event.getMessage().startsWith("/locate")) {
                event.getPlayer().sendMessage(LangOpt.COMMAND_LOCATE_NOVANILLA.parse());
                event.getPlayer().sendMessage("");
            }
        }
    }

    @Override
    public String getDefaultDescription() {
        return "Locates nearest TerraformGenerator structures. Do /terra locate for all searchable structures.";
    }

    @Override
    public boolean canConsoleExec() {
        return true;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.isOp() || sender.hasPermission("terraformgenerator.locate");
    }

    @Override
    public void execute(CommandSender sender, Stack<String> args)
            throws InvalidArgumentException {
        ArrayList<Object> params = this.parseArguments(sender, args);
        if (params.size() == 0) {
            sender.sendMessage(LangOpt.COMMAND_LOCATE_LIST_HEADER.parse());
            for (StructurePopulator spop : StructureRegistry.getAllPopulators()) {
                sender.sendMessage(LangOpt.COMMAND_LOCATE_LIST_ENTRY.parse("%entry%", spop.getClass().getSimpleName().replace("Populator", "")));
            }
            return;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(LangOpt.fetchLang("permissions.console-cannot-exec"));
            return;
        }
        StructurePopulator spop = (StructurePopulator) params.get(0); //TODO: Get populator by name

        Player p = (Player) sender;
//		if(!(p.getWorld().getGenerator() instanceof TerraformGenerator)) {
//			p.sendMessage(ChatColor.RED + "Can only be used in TerraformGenerator worlds!");
//			return;
//		}
        if (!spop.isEnabled()) {
            p.sendMessage(LangOpt.COMMAND_LOCATE_STRUCTURE_NOT_ENABLED.parse());
            return;
        }

        if (spop instanceof StrongholdPopulator ||
                (!(spop instanceof SingleMegaChunkStructurePopulator) && !(spop instanceof MultiMegaChunkStructurePopulator))) {
            int[] coords = spop.getNearestFeature(TerraformWorld.get(p.getWorld()), p.getLocation().getBlockX(), p.getLocation().getBlockZ());
            syncSendMessage(p.getUniqueId(), LangOpt.COMMAND_LOCATE_LOCATE_COORDS.parse("%x%", coords[0] + "", "%z%", coords[1] + ""));
            return;
        }

        if (spop instanceof SingleMegaChunkStructurePopulator) {
            locateSingleMegaChunkStructure(p, (SingleMegaChunkStructurePopulator) spop);
        } else {
            locateMultiMegaChunkStructure(p, (MultiMegaChunkStructurePopulator) spop);
        }
    }

    private void locateMultiMegaChunkStructure(Player p, MultiMegaChunkStructurePopulator populator) {

        MegaChunk center = new MegaChunk(
                p.getLocation().getBlockX(),
                p.getLocation().getBlockY(),
                p.getLocation().getBlockZ());
        TerraformWorld tw = TerraformWorld.get(p.getWorld());
        p.sendMessage(LangOpt.COMMAND_LOCATE_SEARCHING.parse());
        UUID uuid = p.getUniqueId();

        long startTime = System.currentTimeMillis();

        BukkitRunnable runnable = new BukkitRunnable() {
            public void run() {
                int blockX = -1;
                int blockZ = -1;
                int radius = 0;
                boolean found = false;

                while (!found) {
                    for (MegaChunk mc : getSurroundingChunks(center, radius)) {
                        for (int[] coords : populator.getCoordsFromMegaChunk(tw, mc)) {
                            if (coords == null) continue;

                            ArrayList<BiomeBank> banks = GenUtils.getBiomesInChunk(tw, coords[0] >> 4, coords[1] >> 4);

                            if (populator.canSpawn(tw, coords[0] >> 4, coords[1] >> 4, banks)) {
                                found = true;
                                blockX = coords[0];
                                blockZ = coords[1];
                                break;
                            }
                        }
                        if (found) break;
                    }
                    radius++;
                }
                long timeTaken = System.currentTimeMillis() - startTime;

                syncSendMessage(uuid, LangOpt.COMMAND_LOCATE_COMPLETED_TASK.parse("%time%", timeTaken + ""));

                if (found)
                    syncSendMessage(uuid, ChatColor.GREEN + "[" + populator.getClass().getSimpleName() + "] " + LangOpt.COMMAND_LOCATE_LOCATE_COORDS.parse("%x%", blockX + "",
                            "%z%", blockZ + ""));
                else
                    syncSendMessage(uuid, ChatColor.RED + "Failed to find structure. Somehow.");

            }
        };
        runnable.runTaskAsynchronously(plugin);
    }

    private void locateSingleMegaChunkStructure(Player p, SingleMegaChunkStructurePopulator populator) {

        MegaChunk center = new MegaChunk(
                p.getLocation().getBlockX(),
                p.getLocation().getBlockY(),
                p.getLocation().getBlockZ());
        TerraformWorld tw = TerraformWorld.get(p.getWorld());
        p.sendMessage(LangOpt.COMMAND_LOCATE_SEARCHING.parse());
        UUID uuid = p.getUniqueId();


        long startTime = System.currentTimeMillis();

        BukkitRunnable runnable = new BukkitRunnable() {
            public void run() {
                MegaChunk lowerBound = null;
                MegaChunk upperBound = null;
                int blockX = -1;
                int blockZ = -1;
                int radius = 0;
                boolean found = false;
                //syncSendMessage(uuid, ChatColor.YELLOW + "Using Location " + p.getLocation().getX() + "," + p.getLocation().getZ());
                //syncSendMessage(uuid, ChatColor.YELLOW + "Using Center MC: " + center.getX() + "," + center.getZ());

                while (!found) {
                    for (MegaChunk mc : getSurroundingChunks(center, radius)) {
                        if (lowerBound == null) lowerBound = mc;
                        if (upperBound == null) upperBound = mc;
                        if (mc.getX() < lowerBound.getX() || mc.getZ() < lowerBound.getZ())
                            lowerBound = mc;
                        if (mc.getX() > upperBound.getX() || mc.getZ() > upperBound.getZ())
                            upperBound = mc;
                        int[] coords = populator.getCoordsFromMegaChunk(tw, mc);
                        if (coords == null) continue;
                        //Right bitshift of 4 is conversion from block coords to chunk coords.
                        ArrayList<BiomeBank> banks = GenUtils.getBiomesInChunk(tw, coords[0] >> 4, coords[1] >> 4);

                        if (populator.canSpawn(tw, coords[0] >> 4, coords[1] >> 4, banks)) {

                            //Mega Dungeons will always spawn if they can.
                            if (StructureRegistry.getStructureType(populator.getClass()) == StructureType.MEGA_DUNGEON) {
                                found = true;
                                blockX = coords[0];
                                blockZ = coords[1];
                                break;
                            } else {
                                //If it is not a mega dungeon, the structure registry must be checked.
                                for (SingleMegaChunkStructurePopulator availablePops : StructureRegistry.getLargeStructureForMegaChunk(tw, mc)) {
                                    if (availablePops == null) continue;
                                    if (availablePops.getClass().equals(populator.getClass())) {
                                        //Can spawn
                                        found = true;
                                        blockX = coords[0];
                                        blockZ = coords[1];
                                        break;
                                    }
                                }
                                if (found) break;
                            }
                        }
                    }
                    radius++;
                    //syncSendMessage(uuid,ChatColor.YELLOW + "[" + populator.getClass().getSimpleName() + "] Searching MegaChunk Radius: " + radius);

                }
                //syncSendMessage(uuid,ChatColor.YELLOW + "[" + populator.getClass().getSimpleName() + "] UpperBound: " + upperBound.getX() + "," + upperBound.getZ());
                //syncSendMessage(uuid,ChatColor.YELLOW + "[" + populator.getClass().getSimpleName() + "] LowerBound: " + lowerBound.getX() + "," + lowerBound.getZ());
                long timeTaken = System.currentTimeMillis() - startTime;

                syncSendMessage(uuid, LangOpt.COMMAND_LOCATE_COMPLETED_TASK.parse("%time%", timeTaken + ""));

                if (found)
                    syncSendMessage(uuid, ChatColor.GREEN + "[" + populator.getClass().getSimpleName() + "] " + LangOpt.COMMAND_LOCATE_LOCATE_COORDS.parse("%x%", blockX + "",
                            "%z%", blockZ + ""));
                else
                    syncSendMessage(uuid, ChatColor.RED + "Failed to find structure. Somehow.");

            }
        };
        runnable.runTaskAsynchronously(plugin);
    }

    @SuppressWarnings("serial")
	private Collection<MegaChunk> getSurroundingChunks(MegaChunk center, int radius) {
        if (radius == 0) return new ArrayList<MegaChunk>() {{
            add(center);
        }};
        //     xxxxx
        //xxx  x   x
        //xox  x o x
        //xxx  x   x
        //     xxxxx
        ArrayList<MegaChunk> candidates = new ArrayList<MegaChunk>();
        for (int rx = -radius; rx <= radius; rx++) {
            for (int rz = -radius; rz <= radius; rz++) {

                //Check that this is a border coord
                if (Math.abs(rx) == radius || Math.abs(rz) == radius) {
                    //Bukkit.getLogger().info(center.getX() + "+" + rx + "," + center.getZ() + "+"+rz);
                    candidates.add(center.getRelative(rx, rz));
                }
            }
        }

        return candidates;
    }

    private void syncSendMessage(UUID uuid, String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getUniqueId() == uuid) {
                p.sendMessage(message);
                break;
            }
        }
        TerraformGeneratorPlugin.logger.info("[Locate Command] " + message);
    }

    public static class StructurePopulatorArgument extends DCArgument<StructurePopulator> {

        public StructurePopulatorArgument(String name, boolean isOptional) {
            super(name, isOptional);
        }

        @Override
        public StructurePopulator parse(CommandSender arg0, String arg1) {

            for (StructurePopulator spop : StructureRegistry.getAllPopulators()) {
                if (spop.getClass().getSimpleName().equalsIgnoreCase(arg1) ||
                        spop.getClass().getSimpleName().equalsIgnoreCase(arg1 + "populator"))
                    return spop;
            }
            return null;
        }

        @Override
        public String validate(CommandSender arg0, String arg1) {
            if (this.parse(arg0, arg1) != null)
                return "";
            else
                return "Structure type does not exist";
        }
    }

}
