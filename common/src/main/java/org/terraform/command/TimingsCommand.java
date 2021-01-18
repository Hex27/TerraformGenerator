package org.terraform.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.utils.TickTimer;

import java.util.Map;
import java.util.Stack;

public class TimingsCommand extends DCCommand {
    public TimingsCommand(DrycellPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public String getDefaultDescription() {
        return "Shows timings of monitored functions";
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
    public void execute(CommandSender sender, Stack<String> args) throws InvalidArgumentException {
        sender.sendMessage("=====Avg Timings=====");
        for (Map.Entry<String, Long> entry : TickTimer.TIMINGS.entrySet()) {
            sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + entry.getKey() + ChatColor.DARK_GRAY + ": " + ChatColor.GOLD + entry.getValue());
        }
    }
}
