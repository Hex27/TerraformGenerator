package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.terraform.command.contants.InvalidArgumentException;
import org.terraform.command.contants.TerraCommand;
import org.terraform.coregen.bukkit.NativeGeneratorPatcherPopulator;
import org.terraform.main.TerraformGeneratorPlugin;

import java.util.Stack;

public class FixerCacheFlushCommand extends TerraCommand {

    public FixerCacheFlushCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public String getDefaultDescription() {
        return "Flushes the chunk fixer cache.";
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
    	NativeGeneratorPatcherPopulator.flushChanges();
    	sender.sendMessage("Flushing changes.");
    }

}
