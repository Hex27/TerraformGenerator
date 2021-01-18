package org.terraform.command;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.type.Wall;
import org.bukkit.command.CommandSender;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;

import java.util.Stack;

public class BlockDataTestCommand extends DCCommand {

    public BlockDataTestCommand(DrycellPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public String getDefaultDescription() {
        return "Shows some new blockdata values in 1.16";
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
        Wall facing = (Wall) Bukkit.createBlockData(Material.COBBLESTONE_WALL);
        sender.sendMessage(facing.getAsString());
    }


}
