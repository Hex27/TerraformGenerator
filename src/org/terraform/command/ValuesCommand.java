package org.terraform.command;

import java.util.Random;
import java.util.Stack;

import org.bukkit.command.CommandSender;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.GenUtils;

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

		MathValues mountainV = new MathValues();
		MathValues heightMulV = new MathValues();
		MathValues heightV = new MathValues();
		MathValues simplexV = new MathValues();
		MathValues perlinV = new MathValues();
		
		FastNoise caveNoise = new FastNoise(new Random().nextInt(9999));
		caveNoise.SetNoiseType(NoiseType.PerlinFractal);
		caveNoise.SetFractalOctaves(3);
		caveNoise.SetFrequency(0.03f);
		
		SimplexOctaveGenerator gen = new SimplexOctaveGenerator(new Random(),4);
		gen.setScale(0.003D);
		
		for(int i = 0; i<10000; i++){
			int x = GenUtils.randInt(0,1000);
			int y = GenUtils.randInt(0,100);
			int z = GenUtils.randInt(0,1000);

			double height = gen.noise(x, z, 0.1, 1.9, true)*500.0;
			//sender.sendMessage(x + "," + z + ":" + height);
			heightV.addValue(height);
			
//			PerlinOctaveGenerator mountainGenerator = new PerlinOctaveGenerator(new Random(),1);
//			mountainGenerator.setScale(0.03D);
//			
//			SimplexOctaveGenerator heightGen = new SimplexOctaveGenerator(new Random(),1);
//			heightGen.setScale(0.0001D);
//			
//	        SimplexOctaveGenerator gen = new SimplexOctaveGenerator(new Random(), 8);
//			gen.setScale(2*0.005D);
//			
//			PerlinOctaveGenerator pgen = new PerlinOctaveGenerator(new Random(), 8);
//			pgen.setScale(0.005D);
//
//			double mountainHeight = Math.round((Math.abs(gen.noise(x, z, 0.0001,0.5))+1));
//			mountainV.addValue(mountainHeight);
//			double heightMultiplier = (heightGen.noise(x, z, 0.01,0.5)+0.2)*2;
//			heightMulV.addValue(heightMultiplier);
//			double perlin = pgen.noise(x, z, 0.1, 0.5)*50;
//			perlinV.addValue(perlin);
//			double simplex = gen.noise(x, z, 0.1, 0.5)*30;
//			simplexV.addValue(simplex);
//			
//			int height = (int) ((50 + ((perlin+simplex)/2)*heightMultiplier)*mountainHeight);
//			heightV.addValue(height);
		}
		//sender.sendMessage("Height multiplier:"+heightMulV.toString());
		//sender.sendMessage("Mountain multiplier:"+mountainV.toString());
		//sender.sendMessage("Perlin:"+perlinV.toString());
		//sender.sendMessage("Simplex:"+simplexV.toString());
		sender.sendMessage("Height:"+heightV.toString());
		
	}
	
	private class MathValues{
		private double total = 0;
		private double lowest = 99999;
		private double highest = -99999;
		private int count = 0;
		
		public MathValues(){}
		
		public void addValue(double value){
			total += value;
			count++;
			if(value < lowest) lowest = value;
			if(value > highest) highest = value;
		}
		
		public double avg(){
			return total/count;
		}
		
		public double getLowest(){
			return lowest;
		}
		
		public double getHighest(){
			return highest;
		}
		
		public String toString(){
			return getLowest() + " to " + getHighest() + ": " + avg();
		}
	}
	

}
