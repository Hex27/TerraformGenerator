package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.coregen.HeightMap;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.GenUtils;

import java.util.Random;
import java.util.Stack;

public class ValuesCommand extends DCCommand {

    public ValuesCommand(DrycellPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public String getDefaultDescription() {
        return "Shows a range of values for stuff";
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
        MathValues vals = new MathValues();
        TerraformWorld tw = TerraformWorld.get("world-1232341234", new Random().nextInt(99999));
        for (int i = 0; i < 20000; i++) {
            int x = GenUtils.randInt(-10000, 10000);
            //int y = GenUtils.randInt(0,100);
            int z = GenUtils.randInt(-10000, 10000);
            int height = HeightMap.getHeight(tw, x, z);
            vals.addValue(height);
        }
        sender.sendMessage("Finished");
        sender.sendMessage("Highest: " + vals.getHighest());
        sender.sendMessage("Lowest: " + vals.getLowest());
    }

    @SuppressWarnings("unused")
    private class MathValues {
        private double total = 0;
        private double lowest = 99999;
        private double highest = -99999;
        private int count = 0;

        public MathValues() {
        }

        public void addValue(double value) {
            total += value;
            count++;
            if (value < lowest) lowest = value;
            if (value > highest) highest = value;
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

        public String toString() {
            return getLowest() + " to " + getHighest() + ": " + avg();
        }
    }


}
