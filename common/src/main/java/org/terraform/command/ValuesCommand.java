package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.terraform.command.contants.InvalidArgumentException;
import org.terraform.command.contants.TerraCommand;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.GenUtils;

import java.util.Random;
import java.util.Stack;

public class ValuesCommand extends TerraCommand {

    public ValuesCommand(TerraformGeneratorPlugin plugin, String... aliases) {
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
    
    private double warpSine(double tempUnwarpedSineX, int period, int seed) {
		double warp = GenUtils.randInt(new Random(3*seed),-3, 3);
		if(warp == 0) warp = 1;
		if(warp < 0) {
			warp = (10-2*warp)/10.0;
		}
		
		double warpedValue;
		if(tempUnwarpedSineX == 0 && warp == 0) { //Prevent math error
			warpedValue = 0;
		}else {
			warpedValue = Math.pow(Math.abs(tempUnwarpedSineX),warp);
		}
		if(tempUnwarpedSineX < 0) {
			warpedValue = -warpedValue; //Preserve sign
		}
		return warpedValue;
	}

    @Override
    public void execute(CommandSender sender, Stack<String> args)
            throws InvalidArgumentException {
        MathValues vals = new MathValues();
        MathValues unwarped = new MathValues();
        MathValues warped = new MathValues();
        TerraformWorld tw = TerraformWorld.get("world-1232341234", new Random().nextInt(99999));
        int period = 4;
        for (int i = 0; i < 9000000; i++) {
            int x = i;
            //int y = GenUtils.randInt(0,100);
            int z = GenUtils.randInt(-10000, 10000);
    		int sineSegmentHash = (x/period)+11*(z/period);
    		
            double unwarpedSineX = Math.sin((2.0*Math.PI/((double)period))*((double)x));
            double unwarpedSineZ = Math.sin((2.0*Math.PI/((double)period))*((double)z));
            unwarped.addValue(unwarpedSineX);
    		double temperatureX = warpSine(unwarpedSineX, period, 71+sineSegmentHash);
    		double temperatureZ = warpSine(unwarpedSineZ, period, 71+sineSegmentHash);
            
    		warped.addValue(temperatureX);
    		vals.addValue(2.5*temperatureX*temperatureZ);
        }
        sender.sendMessage("Finished");
        sender.sendMessage("Highest: " + vals.getHighest());
        sender.sendMessage("Lowest: " + vals.getLowest());
        sender.sendMessage("Mean: " + vals.avg());
        sender.sendMessage("Warped: " + warped);
        sender.sendMessage("Unwarped" + unwarped);
    }

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
