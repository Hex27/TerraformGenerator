package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.utils.TickTimer;

import java.util.HashMap;
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

    @SuppressWarnings("unchecked")
    @Override
    public void execute(CommandSender sender, Stack<String> args)
            throws InvalidArgumentException {

        sender.sendMessage("=====Avg Timings=====");
        HashMap<String, Long> c = (HashMap<String, Long>) TickTimer.TIMINGS.clone();
        for (String key : c.keySet()) {
            sender.sendMessage("- " + key + ":" + c.get(key));
        }

    }

}
