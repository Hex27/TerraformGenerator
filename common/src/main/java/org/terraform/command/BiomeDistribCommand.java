package org.terraform.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeClimate;
import org.terraform.biome.BiomeSection;
import org.terraform.biome.BiomeType;
import org.terraform.command.contants.TerraCommand;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

public class BiomeDistribCommand extends TerraCommand {

    public BiomeDistribCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Displays a test for biome distribution with the current configuration options";
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
        HashMap<BiomeBank, Integer> counts = new HashMap<>();
        HashMap<BiomeClimate, Integer> climates = new HashMap<>();
        MathValues temperature = new MathValues();
        MathValues moisture = new MathValues();
        double numOceans = 0;
        double numMountains = 0;
        double total = 0;
        TerraformWorld tw = TerraformWorld.get("world-" + new Random().nextInt(99999), new Random().nextInt(99999));
        for (int nx = -50; nx < 50; nx++) {
            for (int nz = -50; nz < 50; nz++) {
                BiomeSection sect = BiomeBank.getBiomeSectionFromSectionCoords(tw, nx, nz, true);
                if (sect.getBiomeBank().getType() == BiomeType.OCEANIC
                    || sect.getBiomeBank().getType() == BiomeType.DEEP_OCEANIC)
                {
                    numOceans++;
                }
                if (sect.getBiomeBank().getType() == BiomeType.MOUNTAINOUS
                    || sect.getBiomeBank().getType() == BiomeType.HIGH_MOUNTAINOUS)
                {
                    numMountains++;
                }

                temperature.addValue(sect.getTemperature());
                moisture.addValue(sect.getMoisture());

                if (!counts.containsKey(sect.getBiomeBank())) {
                    counts.put(sect.getBiomeBank(), 1);
                }
                else {
                    counts.put(sect.getBiomeBank(), counts.get(sect.getBiomeBank()) + 1);
                }

                if (!climates.containsKey(sect.getBiomeBank().getClimate())) {
                    climates.put(sect.getBiomeBank().getClimate(), 1);
                }
                else {
                    climates.put(sect.getBiomeBank().getClimate(), climates.get(sect.getBiomeBank().getClimate()) + 1);
                }
            }
        }

        sender.sendMessage("Temperature: " + temperature);
        sender.sendMessage("Moisture: " + moisture);
        for (int val : counts.values()) {
            total += val;
        }


        for (BiomeBank b : BiomeBank.values()) {
            if (b.getType() != BiomeType.BEACH && b.getType() != BiomeType.RIVER) {
                String count = "" + counts.getOrDefault(b, 0);
                String percent = "(" + Math.round(100 * counts.getOrDefault(b, 0) / total);
                if (count.equals("0")) {
                    count = ChatColor.RED + count;
                }
                if (100 * counts.getOrDefault(b, 0) / total < 5) {
                    percent = ChatColor.RED + percent;
                }

                sender.sendMessage("%-35s(%-10s, %-10s): %-10s%s)".formatted(b.toString(),
                        b.getClimate().getTemperatureRange(),
                        b.getClimate().getMoistureRange(),
                        count,
                        percent + "%)"
                ));
            }
        }

        sender.sendMessage("=====================================");
        sender.sendMessage("Percent Ocean: " + (100.0 * numOceans / total) + "%");
        sender.sendMessage("Percent Mountain: " + (100.0 * numMountains / total) + "%");
        sender.sendMessage("===================================");
        total = 0;

        for (int val : climates.values()) {
            total += val;
        }
        for (BiomeClimate c : BiomeClimate.values()) {
            String count = "" + climates.getOrDefault(c, 0);
            String percent = "(" + Math.round(100 * climates.getOrDefault(c, 0) / total);

            if (count.equals("0")) {
                count = ChatColor.RED + count;
            }
            if (100 * climates.getOrDefault(c, 0) / total < 5) {
                percent = ChatColor.RED + percent;
            }

            int biomeTypes = 0;
            for (BiomeBank b : BiomeBank.values()) {
                if (b.getClimate() == c && (b.getType() == BiomeType.FLAT
                                            || b.getType() == BiomeType.MOUNTAINOUS
                                            || b.getType() == BiomeType.HIGH_MOUNTAINOUS))
                {
                    biomeTypes++;
                }
            }

            float biomesPerPercent = (float) (biomeTypes / (100 * climates.getOrDefault(c, 0)
                                                            / total));// Math.round(100*climates.getOrDefault(c, 0)/total)/((float)biomeTypes);

            sender.sendMessage("%-30s%-10s %-10s (%d registered biomes) (numBiomes/percent: %.2f)".formatted(c.toString(),
                    count,
                    percent + "%)",
                    biomeTypes,
                    biomesPerPercent
            ));
        }
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
