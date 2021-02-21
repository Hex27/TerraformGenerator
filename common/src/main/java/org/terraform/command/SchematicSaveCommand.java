package org.terraform.command;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
        try {
            s.export("new-schematic-" + System.currentTimeMillis() + ".terra");
            p.sendMessage(ChatColor.GREEN + "Saved.");
        } catch (IOException e) {
            p.sendMessage(ChatColor.RED + "A problem occurred.");
            e.printStackTrace();
        }
    }
}
