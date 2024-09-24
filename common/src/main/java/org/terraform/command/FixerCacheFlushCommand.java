package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.terraform.command.contants.TerraCommand;
import org.terraform.coregen.bukkit.NativeGeneratorPatcherPopulator;
import org.terraform.coregen.bukkit.PhysicsUpdaterPopulator;
import org.terraform.main.TerraformGeneratorPlugin;

import java.util.Stack;

public class FixerCacheFlushCommand extends TerraCommand {

    public FixerCacheFlushCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Flushes the chunk fixer cache.";
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
    public void execute(@NotNull CommandSender sender, Stack<String> args)
    {
        NativeGeneratorPatcherPopulator.flushChanges();
        PhysicsUpdaterPopulator.flushChanges();
        sender.sendMessage("Flushing changes.");
    }

}
