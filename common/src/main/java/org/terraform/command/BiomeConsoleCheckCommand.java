package org.terraform.command;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.command.contants.TerraCommand;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

import java.util.Random;
import java.util.Stack;

public class BiomeConsoleCheckCommand extends TerraCommand {

    public BiomeConsoleCheckCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Checks biome at 10 random coordinates via console.";
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
        World world = Bukkit.getWorld("world");
        for (int i = 0; i < 10; i++) {
            int x = new Random().nextInt(1000);
            int z = new Random().nextInt(1000);

            BiomeBank bank = TerraformWorld.get(world).getBiomeBank(x, z);

            sender.sendMessage("[Iteration] " + i + " (" + x + "," + z + ")");
            sender.sendMessage("    BiomeBank: " + bank + " (" + bank.getHandler().getBiome() + ")");
            sender.sendMessage("    Minecraft Biome [0]" + world.getBiome(x, 0, z));
            sender.sendMessage("    Minecraft Biome [60]" + world.getBiome(x, 60, z));
            sender.sendMessage("    Minecraft Biome [300]" + world.getBiome(x, 300, z));
        }
    }

}
