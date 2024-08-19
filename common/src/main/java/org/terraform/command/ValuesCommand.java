package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.terraform.command.contants.TerraCommand;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;
import java.util.Stack;

public class ValuesCommand extends TerraCommand {

    public ValuesCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Shows a range of values for stuff";
    }

    @Override
    public boolean canConsoleExec() {
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull CommandSender sender) {

        return sender.isOp();
    }

    @SuppressWarnings("unused")
    private double warpSine(double tempUnwarpedSineX, int period, int seed) {
        double warp = GenUtils.randInt(new Random(3L * seed), -3, 3);
        if (warp == 0) {
            warp = 1;
        }
        if (warp < 0) {
            warp = (10 - 2 * warp) / 10.0;
        }

        double warpedValue;
        if (tempUnwarpedSineX == 0 && warp == 0) { // Prevent math error
            warpedValue = 0;
        }
        else {
            warpedValue = Math.pow(Math.abs(tempUnwarpedSineX), warp);
        }
        if (tempUnwarpedSineX < 0) {
            warpedValue = -warpedValue; // Preserve sign
        }
        return warpedValue;
    }

    @SuppressWarnings("unused")
    @Override
    public void execute(@NotNull CommandSender sender, Stack<String> args) {

        MathValues vals = new MathValues();
        MathValues unwarped = new MathValues();
        MathValues warped = new MathValues();

        TerraformWorld tw = TerraformWorld.get("world-1232341234", new Random().nextInt(99999));

        FastNoise carverEntranceStandard = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.CARVER_STANDARD, world -> {
            FastNoise n = new FastNoise((int) (world.getSeed() * 111));
            n.SetNoiseType(NoiseType.SimplexFractal);
            n.SetFractalOctaves(4);
            n.SetFrequency(0.07f);
            return n;
        });

        int period = 4;
        for (int x = 0; x < 9000000; x++) {
            int y = GenUtils.randInt(0, 100);
            int z = GenUtils.randInt(-10000, 10000);
            vals.addValue(carverEntranceStandard.GetNoise(x, y, z));
            // vals.addValue(50.0*tw.getOceanicNoise().GetNoise(x,z));
        }
        sender.sendMessage("Finished");
        sender.sendMessage("Highest: " + vals.getHighest());
        sender.sendMessage("Lowest: " + vals.getLowest());
        sender.sendMessage("Mean: " + vals.avg());
        sender.sendMessage("Warped: " + warped);
        sender.sendMessage("Unwarped" + unwarped);
    }

    private static class MathValues {
        private double total = 0;
        private double lowest = 99999;
        private double highest = -99999;
        private int count = 0;

        public MathValues() {
        }

        public void addValue(double value) {
            total += value;
            count++;
            if (value < lowest) {
                lowest = value;
            }
            if (value > highest) {
                highest = value;
            }
        }

        public double avg() {
            return total / count;
        }

        public double getLowest() {
            return lowest;
        }

        public double getHighest() {
            return highest;
        }

        public @NotNull String toString() {
            return getLowest() + " to " + getHighest() + ": " + avg();
        }
    }


}
