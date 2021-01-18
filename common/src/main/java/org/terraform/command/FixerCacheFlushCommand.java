package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.coregen.bukkit.NativeGeneratorPatcherPopulator;
import org.terraform.data.TerraformWorld;
import org.terraform.structure.small.WitchHutPopulator;

import java.util.Random;
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
        return false;
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
