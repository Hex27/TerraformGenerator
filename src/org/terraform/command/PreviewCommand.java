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
import org.terraform.coregen.HeightMap;
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
		int x = 13000;
		int z = 8000;
		double highest = -1;
		double lowest = 10000;	
		BufferedImage img = new BufferedImage(x, z, BufferedImage.TYPE_INT_RGB);
		//file object
		File f = new File("terra-preview.png");
		if(f.exists())
			f.delete();
		
		for(int nz = 0; nz < z; nz++){
			for(int nx = 0; nx < x; nx++){
				int noise = (int) (HeightMap.getHeight(TerraformWorld.get("world-"+seed, seed), nx, nz)
						);//(realRidge(seed,nx,nz)*2);

				img.setRGB(nx, nz, getColorFromNoise(noise).getRGB());
			}
		}
		try{
			  f = new File("terra-preview.png");
			  ImageIO.write(img, "png", f);
			}catch(IOException e){
			  System.out.println(e);
			}
		sender.sendMessage("Exported. H: " + highest + ", L: " + lowest);
	}
	
	private Color getColorFromNoise(int noise){
		if(noise <= 62){ //Sea level
			return new Color(50,50,(int) (100+(noise*2)));//Blue
		}else if(noise < 62+4){ //Beaches?
			return new Color(240, 238, 108);//Yellow
		}else if(noise < 92){
			return new Color(37,(70+(noise*2)),2);//Green
		}else{ //Mountainous
			return new Color(255,255,255);//White
		}
	}
}
