package org.terraform.command;

import java.util.Random;
import java.util.Stack;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.GenUtils;

public class PreviewCommand extends DCCommand {

	public PreviewCommand(DrycellPlugin plugin, String... aliases) {
		super(plugin, aliases);
	}

	@Override
	public String getDefaultDescription() {
		return "Shows a preview of a specified generation technique";
	}

	@Override
	public boolean canConsoleExec() {
		return true;
	}

	@Override
	public boolean hasPermission(CommandSender sender) {
		
		return true;
	}
	
	private double getCoreHeight(int seed, int x, int z){
		SimplexOctaveGenerator gen = new SimplexOctaveGenerator(new Random(seed), 8);
		gen.setScale(0.007D);
		
		double height = gen.noise(x,z,1,0.5)*25+TerraformGenerator.seaLevel;
		
		if(height > TerraformGenerator.seaLevel + 10){
			height = (height-TerraformGenerator.seaLevel-10)*0.3 + TerraformGenerator.seaLevel + 10;
		}
		if(height < TerraformGenerator.seaLevel - 20){
			height = -(TerraformGenerator.seaLevel-20-height)*0.3 + TerraformGenerator.seaLevel -20;
		}
		
		return height;
	}
	
	private double getSeaHeight(int seed, int x, int z){

		FastNoise gen = new FastNoise(seed);
		gen.SetNoiseType(NoiseType.Cubic);
		gen.SetFrequency(0.005f);		
		
		double height = gen.GetNoise(x, z)*40;
		if(height > 0) return 0.0;
		
		return height;
	}
	
//	private double getMountainMap(int seed, int x, int z){
//
//		FastNoise gen = new FastNoise(seed);
//		gen.SetNoiseType(NoiseType.Cubic);
//		gen.SetFrequency(0.005f);
//		double height = (gen.GetNoise(x, z)+0.5)*0.5 + 1 ;
//		
//		return height;
//	}

	@Override
	public void execute(CommandSender sender, Stack<String> args)
			throws InvalidArgumentException {
		int seed = GenUtils.randInt(100, 10000);
//		PerlinOctaveGenerator gen = new PerlinOctaveGenerator(new Random(),1);
//		gen.setScale(0.001D);
		int x = 110;
		int z = 30;
		int[][] heightMap = new int[x][z];
		int highest = -1;
		int lowest = 10000;	
		
		for(int nz = 0; nz < z; nz++){
			String message = "";
			for(int nx = 0; nx < x; nx++){
				
				int noise = (int) (getCoreHeight(seed,nx,nz)+getSeaHeight(seed,nx,nz));
				//int noise = 2+(int) (getCoreHeight(seed,nx,nz)*getSeaHeight(seed,nx,nz));
				//if(noise < 0) noise = 0;
				if(noise > highest) highest = noise;
				if(noise < lowest) lowest = noise;
				
				//heightMap[nx][nz] = noise;
				String tag = ("" + ((int) (noise/10))).replaceAll("-","");
				if(noise > 9) tag = "X";
				String seg = colorFromGen(noise,lowest,highest) + tag;
				//if(seg.equals(ChatColor.BLACK + tag)) seg = ""+noise;
				message += seg;
			}
			sender.sendMessage(message);
		}
		sender.sendMessage("");
		sender.sendMessage(ChatColor.YELLOW + "============================================================");
		sender.sendMessage(ChatColor.AQUA + "" + lowest + " - " + highest);
//		int nz = 25;
//		for(int nx = 0; nx < x; nx++){
//			String message = ChatColor.AQUA + "";
//			for(int i = 0; i <= heightMap[nx][nz]; i++){
//				message += "X";
//			}
//			sender.sendMessage(message);
//		}
	}
	
	private ChatColor colorFromGen(int y, int min, int max){
		int seaLevel = TerraformGenerator.seaLevel;
		
		if(y<=seaLevel-33) return ChatColor.DARK_BLUE;
		if(y < seaLevel){
			return ChatColor.AQUA;
		}else if(y <= seaLevel+3){
			return ChatColor.YELLOW;
		}else if(y < 80){
			return ChatColor.GREEN;
//		}else if(y < 80){
//			return ChatColor.DARK_GREEN;
		}else if(y < 110){
			return ChatColor.GRAY;
		}else if(y < 150)
			return ChatColor.WHITE;
		else
			return ChatColor.RED;
	}

}
