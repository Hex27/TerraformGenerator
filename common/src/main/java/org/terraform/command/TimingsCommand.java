package org.terraform.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.terraform.command.contants.TerraCommand;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.TickTimer;

import java.util.Map;
import java.util.Stack;

public class TimingsCommand extends TerraCommand {
    public TimingsCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Shows timings of monitored functions";
    }

    @Override
    public boolean canConsoleExec() {
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull CommandSender sender) {
        return sender.isOp();
    }

    @Override
    public void execute(@NotNull CommandSender sender, Stack<String> args) {
        sender.sendMessage("=====Avg Timings=====");
        for (Map.Entry<String, Long> entry : TickTimer.TIMINGS.entrySet()) {
            sender.sendMessage(ChatColor.GRAY
                               + "- "
                               + ChatColor.GREEN
                               + entry.getKey()
                               + ChatColor.DARK_GRAY
                               + ": "
                               + ChatColor.GOLD
                               + entry.getValue());
        }
    }
}
