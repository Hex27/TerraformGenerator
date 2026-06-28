package org.terraform.command;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.terraform.command.contants.IntegerArgument;
import org.terraform.command.contants.InvalidArgumentException;
import org.terraform.command.contants.TerraCommand;
import org.terraform.coregen.bukkit.NativeGeneratorPatcherPopulator;
import org.terraform.main.TerraformGeneratorPlugin;

import java.util.ArrayList;
import java.util.Stack;

public class DebugCachePushCommand extends TerraCommand {
    public DebugCachePushCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
        this.parameters.add(new IntegerArgument("count", false));
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Pushes the specified number of nativegeneratorpatcherpopulator cache entries at i*16,200,0 as air.";
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
    public void execute(@NotNull CommandSender sender, Stack<String> args) throws InvalidArgumentException {
        ArrayList<Object> params = this.parseArguments(sender, args);
        for(int i = 0; i < (Integer) params.get(0); i++)
            NativeGeneratorPatcherPopulator.pushChange("world",i*16,200,0, Bukkit.createBlockData(Material.AIR));
        sender.sendMessage("Done.");
    }
}
