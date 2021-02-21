package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeBlender;
import org.terraform.command.contants.InvalidArgumentException;
import org.terraform.command.contants.TerraCommand;
import org.terraform.coregen.HeightMap;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

import java.util.Stack;

public class CheckHeightCommand extends TerraCommand {

    public CheckHeightCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public String getDefaultDescription() {
        return "Checks the heights of various noise maps";
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

        Player p = (Player) sender;
        int x = p.getLocation().getBlockX();
        int z = p.getLocation().getBlockZ();

        TerraformWorld tw = TerraformWorld.get(p.getWorld());
        double rd = HeightMap.RIVER.getHeight(tw, x, z);
        BiomeBank biome = tw.getBiomeBank(x, z);
        p.sendMessage("Core Height: " + HeightMap.CORE.getHeight(tw, x, z));
        p.sendMessage("Mountainous Height: " + HeightMap.MOUNTAIN.getHeight(tw, x, z));
        p.sendMessage("Attrition Height: " + HeightMap.ATTRITION.getHeight(tw, x, z));
        p.sendMessage("Gradient (2,3,4): " + HeightMap.getNoiseGradient(tw, x, z, 2) + "," + HeightMap.getNoiseGradient(tw, x, z, 3) + "," + HeightMap.getNoiseGradient(tw, x, z,
                4));
        p.sendMessage("Result height: " + HeightMap.getBlockHeight(tw, x, z));
        p.sendMessage("River Depth: " + rd);
        p.sendMessage("Mega Chunk: " + new MegaChunk(x, 0, z).getX() + "," + new MegaChunk(x, 0, z).getZ());
        p.sendMessage("Temperature: " + tw.getTemperature(x, z));
        p.sendMessage("Moisture: " + tw.getMoisture(x, z));
        p.sendMessage("Biome edge factor: " + new BiomeBlender(tw, true, false, false)
                .setBiomeThreshold(0.45).getEdgeFactor(biome, x, z));
        p.sendMessage("Result Biome: " + biome);
    }
}
