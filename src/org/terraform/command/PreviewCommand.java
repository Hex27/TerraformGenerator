package org.terraform.command;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

import javax.imageio.ImageIO;

import org.bukkit.command.CommandSender;
import org.drycell.command.DCCommand;
import org.drycell.command.InvalidArgumentException;
import org.drycell.main.DrycellPlugin;
import org.terraform.data.TerraformWorld;
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

	@Override
	public void execute(CommandSender sender, Stack<String> args)
			throws InvalidArgumentException {
		int seed = GenUtils.randInt(1, 1000000);
		int x = 2000;
		int z = 1600;
		double highest = -1;
		double lowest = 10000;	
		BufferedImage img = new BufferedImage(x, z, BufferedImage.TYPE_INT_RGB);
		//file object
		File f = null;
		
		for(int nz = 0; nz < z; nz++){
			for(int nx = 0; nx < x; nx++){
//				double ridge = TerraformWorld.get("world-"+seed, seed).getRiverDepth(nx, nz);
//				int noise = (int) (new HeightMap().getHeight(TerraformWorld.get("world-"+seed, seed), nx, nz)
//						);//(realRidge(seed,nx,nz)*2);
//				if(ridge > highest) highest = ridge;
//				if(ridge < lowest) lowest = ridge;
				double moisture = 3+TerraformWorld.get("world-"+seed, seed).getMoisture(nx, nz);
				double temperature = 3+TerraformWorld.get("world-"+seed, seed).getTemperature(nx, nz);
//				int r = (int) (perc*256.0); //red
//				int g = (int) (perc*256.0); //green
//				int b = (int) (perc*256.0); //blue
//				
				int b = (int) (moisture)*30;
				int r = (int) (temperature)*30;
				
//				if(ridge > 0 && noise > 62) //River
//					img.setRGB(nx,nz,new Color(50,150,(int) (50+25*ridge)).getRGB());
//				else //Normal Gen
					img.setRGB(nx, nz, new Color(r,0,b).getRGB());
				
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
		if(noise <= 62){
			return new Color(50,50,(int) (100+(noise*2)));//Blue
		}else if(noise < 62+4){
			return new Color(240, 238, 108);//Green
		}else if(noise < 92){
			return new Color(50,(int) (100+(noise*1.5)),50);//Green
		}else{
			return new Color(255,255,255);//White
		}
	}
}
