package org.terraform.command;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Stack;

import javax.imageio.ImageIO;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.noise.PerlinOctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.TerraformGenerator;
import org.terraform.data.TerraformWorld;
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
		
		return sender.isOp();
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
		int x = 500;
		int z = 300;
		double highest = -1;
		double lowest = 10000;	
		BufferedImage img = new BufferedImage(x, z, BufferedImage.TYPE_INT_RGB);
		//file object
		File f = null;
		
		for(int nz = 0; nz < z; nz++){
			for(int nx = 0; nx < x; nx++){
				double ridge = TerraformWorld.get("world-"+seed, seed).getRiverDepth(nx, nz);
				int noise = (int) (new HeightMap().getHeight(TerraformWorld.get("world-"+seed, seed), nx, nz)
						);//(realRidge(seed,nx,nz)*2);
				if(ridge > highest) highest = ridge;
				if(ridge < lowest) lowest = ridge;
//				int r = (int) (perc*256.0); //red
//				int g = (int) (perc*256.0); //green
//				int b = (int) (perc*256.0); //blue
//				
				if(ridge <= 0 || noise < TerraformGenerator.seaLevel){
					img.setRGB(nx, nz, getColorFromNoise(noise).getRGB());
				}else
					img.setRGB(nx,nz,new Color(0,70,150).getRGB());
				//sender.sendMessage(new Color(r, g, b).getRGB() +"");
			}
		}
		try{
			  f = new File("output.png");
			  ImageIO.write(img, "png", f);
			}catch(IOException e){
			  System.out.println(e);
			}
		sender.sendMessage("Exported. H: " + highest + ", L: " + lowest);
	}
	
	private Color getColorFromNoise(int noise){
		if(noise <= TerraformGenerator.seaLevel){
			return new Color(50,150,50);//Blue
		}else if(noise < TerraformGenerator.seaLevel+30){
			return new Color(50,150,50);//Green
		}else{
			return new Color(255,255,255);//White
		}
	}
}
