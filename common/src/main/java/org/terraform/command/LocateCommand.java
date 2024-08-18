package org.terraform.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.command.contants.InvalidArgumentException;
import org.terraform.command.contants.TerraCommand;
import org.terraform.command.contants.TerraCommandArgument;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.LangOpt;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.MultiMegaChunkStructurePopulator;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.structure.StructureLocator;
import org.terraform.structure.StructurePopulator;
import org.terraform.structure.StructureRegistry;
import org.terraform.structure.stronghold.StrongholdPopulator;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Stack;
import java.util.UUID;


public class LocateCommand extends TerraCommand implements Listener {

    public LocateCommand(@NotNull TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.parameters.add(new StructurePopulatorArgument("structureType", true));
    }

    @EventHandler
    public void onLocateCommand(@NotNull PlayerCommandPreprocessEvent event) {
        if (event.getPlayer().getWorld().getGenerator() instanceof TerraformGenerator) {
            if (event.getMessage().startsWith("/locate")) {
                event.getPlayer().sendMessage(LangOpt.COMMAND_LOCATE_NOVANILLA.parse());
                event.getPlayer().sendMessage("");
            }
        }
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Locates nearest TerraformGenerator structures. Do /terra locate for all searchable structures.";
    }

    @Override
    public boolean canConsoleExec() {
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull CommandSender sender) {
        return sender.isOp() || sender.hasPermission("terraformgenerator.locate");
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull Stack<String> args)
            throws InvalidArgumentException {
        ArrayList<Object> params = this.parseArguments(sender, args);
        if (params.isEmpty()) {
            sender.sendMessage(LangOpt.COMMAND_LOCATE_LIST_HEADER.parse());
            for (StructurePopulator spop : StructureRegistry.getAllPopulators()) {
                sender.sendMessage(LangOpt.COMMAND_LOCATE_LIST_ENTRY.parse("%entry%", spop.getClass().getSimpleName().replace("Populator", "")));
            }
            sender.sendMessage(LangOpt.COMMAND_LOCATE_LIST_ENTRY.parse("%entry%", "Stronghold"));
            return;
        }
        if (!(sender instanceof Player p)) {
            sender.sendMessage(LangOpt.fetchLang("permissions.console-cannot-exec"));
            return;
        }
        StructurePopulator spop = (StructurePopulator) params.get(0); //TODO: Get populator by name

        if (!spop.isEnabled() && !(spop instanceof StrongholdPopulator)) {
            p.sendMessage(LangOpt.COMMAND_LOCATE_STRUCTURE_NOT_ENABLED.parse());
            return;
        }
        
        //Stronghold Special Case
        if (spop instanceof StrongholdPopulator) {
            int[] coords = ((StrongholdPopulator)spop).getNearestFeature(TerraformWorld.get(p.getWorld()), p.getLocation().getBlockX(), p.getLocation().getBlockZ());
            syncSendMessage(p.getUniqueId(), LangOpt.COMMAND_LOCATE_LOCATE_COORDS.parse("%x%", coords[0] + "", "%z%", coords[1] + ""));
            return;
        }

        if (spop instanceof SingleMegaChunkStructurePopulator) {
            locateSingleMegaChunkStructure(p, (SingleMegaChunkStructurePopulator) spop);
        } else {
            locateMultiMegaChunkStructure(p, (MultiMegaChunkStructurePopulator) spop);
        }
    }

    private void locateMultiMegaChunkStructure(@NotNull Player p, @NotNull MultiMegaChunkStructurePopulator populator) {

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
            	int[] loc = StructureLocator.locateMultiMegaChunkStructure(tw, center, populator, -1);
            	long timeTaken = System.currentTimeMillis() - startTime;
            	
                syncSendMessage(uuid, LangOpt.COMMAND_LOCATE_COMPLETED_TASK.parse("%time%", timeTaken + ""));

                if (loc != null)
                    syncSendMessage(uuid, ChatColor.GREEN + "[" + populator.getClass().getSimpleName() + "] " + LangOpt.COMMAND_LOCATE_LOCATE_COORDS.parse("%x%", loc[0] + "",
                            "%z%", loc[1] + ""));
                else
                    syncSendMessage(uuid, ChatColor.RED + "Failed to find structure. Somehow.");
            }
        };
        runnable.runTaskAsynchronously(plugin);
    }

    private void locateSingleMegaChunkStructure(@NotNull Player p, @NotNull SingleMegaChunkStructurePopulator populator) {

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
            	int[] loc = StructureLocator.locateSingleMegaChunkStructure(tw, center, populator, -1);
            	long timeTaken = System.currentTimeMillis() - startTime;
            	
                syncSendMessage(uuid, LangOpt.COMMAND_LOCATE_COMPLETED_TASK.parse("%time%", timeTaken + ""));

                if (loc != null)
                    syncSendMessage(uuid, ChatColor.GREEN + "[" + populator.getClass().getSimpleName() + "] " + LangOpt.COMMAND_LOCATE_LOCATE_COORDS.parse("%x%", loc[0] + "",
                            "%z%", loc[1] + ""));
                else
                    syncSendMessage(uuid, ChatColor.RED + "Failed to find structure. Somehow.");
            }
        };
        runnable.runTaskAsynchronously(plugin);
    }

    private void syncSendMessage(UUID uuid, @NotNull String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getUniqueId() == uuid) {
                p.sendMessage(message);
                break;
            }
        }
        TerraformGeneratorPlugin.logger.info("[Locate Command] " + message);
    }

    public static class StructurePopulatorArgument extends TerraCommandArgument<StructurePopulator> {

        public StructurePopulatorArgument(String name, boolean isOptional) {
            super(name, isOptional);
        }

        @Override
        public @Nullable StructurePopulator parse(CommandSender arg0, @NotNull String arg1) {
            if(arg1.equalsIgnoreCase("stronghold")||arg1.equalsIgnoreCase("strongholdpopulator"))
                return new StrongholdPopulator();
            for (StructurePopulator spop : StructureRegistry.getAllPopulators()) {
                if (spop.getClass().getSimpleName().equalsIgnoreCase(arg1) ||
                        spop.getClass().getSimpleName().equalsIgnoreCase(arg1 + "populator"))
                    return spop;
            }
            return null;
        }

        @Override
        public @NotNull String validate(CommandSender arg0, @NotNull String arg1) {
            if (this.parse(arg0, arg1) != null)
                return "";
            else
                return "Structure type does not exist";
        }

        @Override
        public @NotNull ArrayList<String> getTabOptions(String @NotNull [] args) {
            if (args.length != 2) return new ArrayList<>();
            ArrayList<String> values = new ArrayList<>();

            for (StructurePopulator spop : StructureRegistry.getAllPopulators()) {
            	if(spop.getClass().getSimpleName().toUpperCase(Locale.ENGLISH).startsWith(args[1].toUpperCase(Locale.ENGLISH)))
            		values.add(spop.getClass().getSimpleName());
            }

            return values;
        }
    }

}
