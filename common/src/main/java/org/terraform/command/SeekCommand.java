package org.terraform.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.terraform.command.contants.InvalidArgumentException;
import org.terraform.command.contants.TerraCommand;
import org.terraform.command.contants.TerraCommandArgument;
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
import java.util.Objects;
import java.util.Stack;


public class SeekCommand extends TerraCommand implements Listener {

    public SeekCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
        this.parameters.add(new StructurePopulatorArgument("structureType", true));
    }

    @Override
    public String getDefaultDescription() {
        return "Locates the nearest structure to 0,0 and requests a chunk from its coords, forcing its generation from console. Operates on world \"world\"";
    }

    @Override
    public boolean canConsoleExec() {
        return true;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.isOp();
    }

    @Override
    public void execute(CommandSender sender, Stack<String> args)
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
        
        StructurePopulator spop = (StructurePopulator) params.get(0); //TODO: Get populator by name

        if (!spop.isEnabled() && !(spop instanceof StrongholdPopulator)) {
            sender.sendMessage(LangOpt.COMMAND_LOCATE_STRUCTURE_NOT_ENABLED.parse());
            return;
        }

        World w = Objects.requireNonNull(Bukkit.getWorld("world"));
        
        //Stronghold Special Case
        if (spop instanceof StrongholdPopulator) {
            int[] coords = ((StrongholdPopulator)spop).getNearestFeature(TerraformWorld.get(Objects.requireNonNull(Bukkit.getWorld("world"))), 0,0);
            syncSendMessage(LangOpt.COMMAND_LOCATE_LOCATE_COORDS.parse("%x%", coords[0] + "", "%z%", coords[1] + ""));
            return;
        }

        if (spop instanceof SingleMegaChunkStructurePopulator) {
            generateSingleMegaChunkStructure(w, (SingleMegaChunkStructurePopulator) spop);
        } else {
            generateMultiMegaChunkStructure(w, (MultiMegaChunkStructurePopulator) spop);
        }
    }

    private void generateMultiMegaChunkStructure(World w, MultiMegaChunkStructurePopulator populator) {

        MegaChunk center = new MegaChunk(0,0,0);
        TerraformWorld tw = TerraformWorld.get(w);
        Bukkit.getConsoleSender().sendMessage(LangOpt.COMMAND_LOCATE_SEARCHING.parse());

        long startTime = System.currentTimeMillis();

        BukkitRunnable runnable = new BukkitRunnable() {
            public void run() {
            	int[] loc = StructureLocator.locateMultiMegaChunkStructure(tw, center, populator, -1);
            	long timeTaken = System.currentTimeMillis() - startTime;
            	
                syncSendMessage(LangOpt.COMMAND_LOCATE_COMPLETED_TASK.parse("%time%", timeTaken + ""));

                if (loc != null) {
                    syncSendMessage(ChatColor.GREEN + "[" + populator.getClass().getSimpleName() + "] " + LangOpt.COMMAND_LOCATE_LOCATE_COORDS.parse("%x%", loc[0] + "",
                            "%z%", loc[1] + ""));
                    w.getChunkAt(new Location(w, loc[0], 0, loc[1]));
                }
                else
                    syncSendMessage(ChatColor.RED + "Failed to find structure. Somehow.");
            }
        };
        runnable.runTaskAsynchronously(plugin);
    }

    private void generateSingleMegaChunkStructure(World w, SingleMegaChunkStructurePopulator populator) {

        MegaChunk center = new MegaChunk(0,0,0);
        TerraformWorld tw = TerraformWorld.get(w);
        Bukkit.getConsoleSender().sendMessage(LangOpt.COMMAND_LOCATE_SEARCHING.parse());

        long startTime = System.currentTimeMillis();

        BukkitRunnable runnable = new BukkitRunnable() {
            public void run() {
            	int[] loc = StructureLocator.locateSingleMegaChunkStructure(tw, center, populator, -1);
            	long timeTaken = System.currentTimeMillis() - startTime;
            	
                syncSendMessage(LangOpt.COMMAND_LOCATE_COMPLETED_TASK.parse("%time%", timeTaken + ""));

                if (loc != null) {
                    syncSendMessage(ChatColor.GREEN + "[" + populator.getClass().getSimpleName() + "] " + LangOpt.COMMAND_LOCATE_LOCATE_COORDS.parse("%x%", loc[0] + "",
                            "%z%", loc[1] + ""));
                    w.getChunkAt(new Location(w, loc[0], 0, loc[1]));
                }
                else
                    syncSendMessage(ChatColor.RED + "Failed to find structure. Somehow.");
            }
        };
        runnable.runTaskAsynchronously(plugin);
    }

    private void syncSendMessage(String message) {
        TerraformGeneratorPlugin.logger.info("[Seek Command] " + message);
    }

    public static class StructurePopulatorArgument extends TerraCommandArgument<StructurePopulator> {

        public StructurePopulatorArgument(String name, boolean isOptional) {
            super(name, isOptional);
        }

        @Override
        public StructurePopulator parse(CommandSender arg0, String arg1) {
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
        public String validate(CommandSender arg0, String arg1) {
            if (this.parse(arg0, arg1) != null)
                return "";
            else
                return "Structure type does not exist";
        }

        @Override
        public ArrayList<String> getTabOptions(String[] args) {
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
