package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.coregen.bukkit.NativeGeneratorPatcherPopulator;

import java.util.Stack;

public class FixerCacheFlushCommand extends DCCommand {

    public FixerCacheFlushCommand(DrycellPlugin plugin, String... aliases) {
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
