package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.terraform.command.contants.TerraCommand;
import org.terraform.main.TerraformGeneratorPlugin;

import java.util.Stack;

public class NMSChunkPacketRefreshCommand extends TerraCommand {

    public NMSChunkPacketRefreshCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Sets current biome you're on to muddy bog and forces a packet refresh for you for the chunk you're standing on";
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
    public void execute(CommandSender sender, Stack<String> args)
    {
        // Commented out to prevent runtime errors for older versions
    }

}
