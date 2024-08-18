package org.terraform.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.terraform.command.contants.TerraCommand;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.SchematicListener;

import java.util.Stack;

public class WandCommand extends TerraCommand {

    public WandCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Gives a schematic wand";
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
    public void execute(CommandSender sender, Stack<String> args) {
        Player p = (Player) sender;
        p.getInventory().addItem(SchematicListener.getWand());
        p.sendMessage(ChatColor.GREEN + "Wand added.");
    }
}
