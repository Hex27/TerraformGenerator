package org.terraform.main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.command.*;
import org.terraform.command.contants.InvalidArgumentException;
import org.terraform.command.contants.TerraCommand;
import org.terraform.command.contants.TerraCommandArgument;
import org.terraform.main.config.TConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

public class TerraformCommandManager implements TabExecutor {

    public final @NotNull ArrayList<String> bases = new ArrayList<String>();
    private final @NotNull ArrayList<TerraCommand> commands = new ArrayList<>();

    private final TerraformGeneratorPlugin plugin;
    public TerraformCommandManager(@NotNull TerraformGeneratorPlugin plugin, String @NotNull ... bases) {

        this.plugin = plugin;
        for (String base : bases) {
            this.bases.add(base);
            plugin.getCommand(base).setExecutor(this);
        }
        registerCommand(new HelpCommand(plugin, this, "help", "h", "?"));

        if (TConfig.c.DEVSTUFF_EXTENDED_COMMANDS) {
            this.registerCommand(new PreviewCommand(plugin, "preview"));
            this.registerCommand(new TimingsCommand(plugin, "timings", "lag"));
            this.registerCommand(new CoconutCommand(plugin, "coconut"));
            this.registerCommand(new ValuesCommand(plugin, "values"));
            this.registerCommand(new BiomeDistribCommand(plugin, "biomedistrib"));
            this.registerCommand(new SphereCommand(plugin, "sphere"));
            this.registerCommand(new FractalTreeCommand(plugin, "fractal", "fractaltree", "ftree"));
            this.registerCommand(new CaveCommand(plugin, "cave"));
            this.registerCommand(new WarmOceanRuinsCommand(plugin, "warmoceanruins", "wor"));
            this.registerCommand(new TrailRuinsCommand(plugin, "trailruins", "tr"));
            this.registerCommand(new IceSpikeCommand(plugin, "icespike"));
            this.registerCommand(new CoralCommand(plugin, "coral"));
            this.registerCommand(new WitchHutCommand(plugin, "hut"));
            this.registerCommand(new GiantPumpkinCommand(plugin, "giantpumpkin"));
            this.registerCommand(new MonumentCommand(plugin, "monument", "mon"));
            this.registerCommand(new LargeMonumentLampCommand(plugin, "lml"));
            this.registerCommand(new StrongholdCommand(plugin, "stronghold", "sh"));
            this.registerCommand(new AnimalFarmCommand(plugin, "animalfarm", "af"));
            this.registerCommand(new FarmhouseCommand(plugin, "farmhouse", "fh"));
            this.registerCommand(new MountainhouseCommand(plugin, "mountainhouse", "mh"));
            this.registerCommand(new AnimalSpawnerCommand(plugin, "animalspawner", "as"));
            this.registerCommand(new MineshaftCommand(plugin, "mineshaft", "ms"));
            this.registerCommand(new CatacombsCommand(plugin, "catacombs", "ccs"));
            this.registerCommand(new IglooCommand(plugin, "igloo"));
            this.registerCommand(new ShipwreckCommand(plugin, "shipwreck", "sw"));
            this.registerCommand(new OutpostCommand(plugin, "outpost"));
            this.registerCommand(new NMSChunkPacketRefreshCommand(plugin, "chunkrefresh"));
            this.registerCommand(new NMSChunkQueryCommand(plugin, "chunkquery"));
            this.registerCommand(new BiomeVisualiserCommand(plugin, "bv", "biomevisualiser"));
            this.registerCommand(new BiomeConsoleCheckCommand(plugin, "bcc", "biomeconsolecheck"));
            this.registerCommand(new UndergroundDungeonCommand(plugin, "ud", "undergrounddungeon"));
            this.registerCommand(new InjectorDebugTestCommand(plugin, "idt", "injectordebugtest"));
            this.registerCommand(new DrownedDungeonCommand(plugin, "dd", "drowneddungeon"));
            this.registerCommand(new CheckHeightCommand(plugin, "checkheight", "ch"));
            // this.registerCommand(new GuardianSpawnCheckCommand(plugin, "gsc","guardianspawncheck"));
            this.registerCommand(new WandCommand(plugin, "wand"));
            this.registerCommand(new MushroomCommand(plugin, "mushroom"));
            this.registerCommand(new RuinedPortalCommand(plugin, "ruinedportal"));
            this.registerCommand(new MansionCommand(plugin, "mansion"));
            this.registerCommand(new SchematicSaveCommand(plugin, "save"));
            this.registerCommand(new AncientCityCommand(plugin, "ancientcity", "ac"));
            this.registerCommand(new SchematicLoadCommand(plugin, "load"));
            this.registerCommand(new PyramidCommand(plugin, "pyramid"));
            this.registerCommand(new MazeCommand(plugin, "maze"));
            this.registerCommand(new DesertWellCommand(plugin, "desertwell"));
            this.registerCommand(new BlockDataTestCommand(plugin, "blockdatatest", "bdt"));
            this.registerCommand(new JigsawBuilderTestCommand(plugin, "jigsawbuildertest", "jbt"));
            this.registerCommand(new PlainsVillageCommand(plugin, "plainsvillage", "pv"));
            this.registerCommand(new RibCageCommand(plugin, "ribcage"));
            this.registerCommand(new OreDitCommand(plugin, "oredit"));
            this.registerCommand(new NewTreeCommand(plugin, "newtree", "nt"));
            this.registerCommand(new CrappyDebugStructureCommand(plugin, "cdsc"));
            this.registerCommand(new SeekCommand(plugin, "seek"));

        }
        this.registerCommand(new LocateCommand(plugin, "locate"));
        this.registerCommand(new FixerCacheFlushCommand(plugin, "fixercacheflush", "fcf"));
        this.registerCommand(new LocateBiomeCommand(plugin, "locatebiome", "lb"));
    }

    public void unregisterCommand(@NotNull Class<?> clazz) {
        commands.removeIf(clazz::isInstance);
    }

    public void unregisterCommand(String alias) {
        commands.removeIf(cmd -> cmd.matchCommand(alias));
    }

    public @NotNull ArrayList<TerraCommand> getCommands() {
        return commands;
    }

    public void registerCommand(@NotNull TerraCommand cmd) {
        this.commands.add(cmd);
        plugin.getLang().fetchLang("command." + cmd.aliases.get(0) + ".desc", cmd.getDefaultDescription());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command cmd,
                             @NotNull String arg2,
                             String @NotNull [] args)
    {
        if (args.length == 0) {
            sender.sendMessage(plugin.getLang().fetchLang("command.unknown"));
            new HelpCommand(plugin, this).execute(sender, new Stack<>());
            return false;
        }
        for (TerraCommand command : commands) {
            if (command.matchCommand(args[0].toLowerCase(Locale.ENGLISH))) {
                Stack<String> stack = new Stack<>();
                // Push arguments from back to front, except the 1st arg
                for (int i = args.length - 1; i >= 1; i--) {
                    stack.push(args[i]);
                }
                if (!command.hasPermission(sender)) {
                    sender.sendMessage(plugin.getLang().fetchLang("permissions.insufficient"));
                    return false;
                }
                if (!command.canConsoleExec() && !(sender instanceof Player)) {
                    sender.sendMessage(plugin.getLang().fetchLang("permissions.console-cannot-exec"));
                    return false;
                }
                if (!command.isInAcceptedParamRange(stack)) {
                    sender.sendMessage(plugin.getLang().fetchLang("command.wrong-arg-length"));
                    return false;
                }
                try {
                    command.execute(sender, stack);
                    return true;
                }
                catch (InvalidArgumentException e) {
                    sender.sendMessage(ChatColor.RED + e.getProblem());
                    return false;
                }
            }
        }
        sender.sendMessage(plugin.getLang().fetchLang("command.unknown"));
        return false;
    }


    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      @NotNull String @NotNull [] args)
    {
        List<String> options = new ArrayList<>();
        if (args.length == 0) {
            for (TerraCommand terraCommand : commands) {
                if (terraCommand.hasPermission(commandSender)) {
                    options.add(terraCommand.aliases.get(0));
                }
            }
        }
        else if (args.length == 1) {
            for (TerraCommand terraCommand : commands) {
                if (terraCommand.hasPermission(commandSender)) {
                    for (String a : terraCommand.aliases) {
                        if (a.startsWith(args[0].toLowerCase(Locale.ENGLISH))) {
                            options.add(terraCommand.aliases.get(0));
                            break;
                        }
                    }
                }
            }
        }
        else {
            for (TerraCommand terraCommand : commands) {
                if (terraCommand.matchCommand(args[0].toLowerCase(Locale.ENGLISH))) {
                    for (TerraCommandArgument<?> arg : terraCommand.parameters) {
                        options.addAll(arg.getTabOptions(args));
                    }
                    break;
                }
            }
        }

        return options;
    }
}
