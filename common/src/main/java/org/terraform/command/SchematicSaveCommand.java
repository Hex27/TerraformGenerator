package org.terraform.command;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.terraform.command.contants.InvalidArgumentException;
import org.terraform.command.contants.SchematicArgument;
import org.terraform.command.contants.TerraCommand;
import org.terraform.command.contants.TerraCommandArgument;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.SchematicListener;
import org.terraform.schematic.TerraRegion;
import org.terraform.schematic.TerraSchematic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

public class SchematicSaveCommand extends TerraCommand {

    public SchematicSaveCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
        this.parameters.add(new TerraCommandArgument<String>("schem-name", false) {
            @Override
            public String parse(CommandSender sender, String value) {
                return value;
            }

            @Override
            public String validate(CommandSender sender, String value) {
                return value;
            }
        });
    }

    @Override
    public String getDefaultDescription() {
        return "Saves a schematic";
    }

    @Override
    public boolean canConsoleExec() {
        return false;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {

        return sender.isOp();
    }

    @Override
    public void execute(CommandSender sender, Stack<String> args)
            throws InvalidArgumentException {
        Player p = (Player) sender;
        TerraRegion rg = SchematicListener.rgs.get(p.getUniqueId());

        if(args.size() == 0) {
            p.sendMessage(ChatColor.RED + "Specify a schematic name.");
            return;
        }

        if (rg == null || !rg.isComplete()) {
            p.sendMessage(ChatColor.RED + "Selection not ready.");
            return;
        }

        TerraSchematic s = new TerraSchematic(p.getLocation().clone());
        for (Block b : rg.getBlocks()) {
            if (b.getType() == Material.AIR)
                continue;
            if (b.getType() == Material.BARRIER)
                b.setType(Material.AIR);
            s.registerBlock(b);
        }

        String name = args.pop();

        try {
            s.export(name + ".terra");
            p.sendMessage(ChatColor.GREEN + "Schematic saved with name " + name);
        } catch (IOException e) {
            p.sendMessage(ChatColor.RED + "A problem occurred.");
            e.printStackTrace();
        }
    }
}
