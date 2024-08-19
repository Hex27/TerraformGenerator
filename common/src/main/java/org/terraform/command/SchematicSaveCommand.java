package org.terraform.command;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.terraform.command.contants.FilenameArgument;
import org.terraform.command.contants.InvalidArgumentException;
import org.terraform.command.contants.TerraCommand;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.SchematicListener;
import org.terraform.schematic.TerraRegion;
import org.terraform.schematic.TerraSchematic;

import java.io.IOException;
import java.util.Stack;

public class SchematicSaveCommand extends TerraCommand {

    public SchematicSaveCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
        this.parameters.add(new FilenameArgument("schem-name", false));
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Saves a schematic in the schematics folder in plugins/TerraformGenerator";
    }

    @Override
    public boolean canConsoleExec() {
        return false;
    }

    @Override
    public boolean hasPermission(@NotNull CommandSender sender) {

        return sender.isOp();
    }

    @Override
    public void execute(CommandSender sender, @NotNull Stack<String> args) throws InvalidArgumentException {
        Player p = (Player) sender;
        TerraRegion rg = SchematicListener.rgs.get(p.getUniqueId());

        if (args.size() != 1) {
            p.sendMessage(ChatColor.RED + "Usage: /terra save [schematic name]");
            return;
        }

        String name = (String) this.parseArguments(sender, args).get(0);

        if (rg == null || !rg.isComplete()) {
            p.sendMessage(ChatColor.RED + "Selection not ready.");
            return;
        }

        TerraSchematic s = new TerraSchematic(p.getLocation().clone());
        for (Block b : rg.getBlocks()) {
            if (b.getType() == Material.AIR) {
                continue;
            }
            if (b.getType() == Material.BARRIER) {
                b.setType(Material.AIR);
            }
            s.registerBlock(b);
        }

        try {
            s.export(name + ".terra");
            p.sendMessage(ChatColor.GREEN + "Schematic saved with name " + name);
        }
        catch (IOException e) {
            p.sendMessage(ChatColor.RED + "A problem occurred.");
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }
    }
}
