package org.terraform.command;

import org.bukkit.command.CommandSender;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeSection;
import org.terraform.biome.BiomeType;
import org.terraform.command.contants.InvalidArgumentException;
import org.terraform.command.contants.TerraCommand;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.GenUtils;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;
import java.util.Stack;

public class PreviewCommand extends TerraCommand {

    public PreviewCommand(TerraformGeneratorPlugin plugin, String... aliases) {
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
        //int seed = GenUtils.randInt(1, 1000000);
        int x = 3000;
        int z = 3000;
        double highest = -1;
        double lowest = 10000;
        boolean hasdebugged = true;
        TerraformWorld tw = TerraformWorld.get("test-world-"+new Random().nextInt(99999), new Random().nextInt(99999));//TerraformWorld.get("test-world", 11111);
        
        BufferedImage img = new BufferedImage(x, z, BufferedImage.TYPE_INT_RGB);
        //file object
        File f = new File("terra-preview.png");
        if (f.exists())
            f.delete();
        double dither = 0.04;
        int debugX = 0;
        int debugZ = 0;
        for (int nz = -z/2; nz < z/2; nz++) {
            for (int nx = -x/2; nx < x/2; nx++) {
            	Random locationBasedRandom  = new Random(Objects.hash(tw.getSeed(),nx,nz));
            	SimpleLocation target  = new SimpleLocation(nx,0,nz);
            	BiomeSection homeSection = BiomeBank.getBiomeSection(tw, nx,nz);
            	boolean debugMe = !hasdebugged && homeSection.getX() == debugX && homeSection.getZ() == debugZ;
            	if(debugMe) {
            		hasdebugged = true;
            		TerraformGeneratorPlugin.logger.info("Debugging: "+homeSection.toString());
            		TerraformGeneratorPlugin.logger.info(nx + "," + nz);
            	}
            	Collection<BiomeSection> sections = BiomeSection.getSurroundingSections(tw, nx, nz);
            	BiomeSection mostDominant = homeSection;
            	if(debugMe)TerraformGeneratorPlugin.logger.info(homeSection.toString() + " Dom: " + homeSection.getDominance(target));
            	for(BiomeSection sect:sections) {
            		float dom = (float) (sect.getDominance(target)+GenUtils.randDouble(locationBasedRandom,-dither,dither));
            		if(debugMe)
            			TerraformGeneratorPlugin.logger.info(sect.toString() + " Dom: " + dom);
            		if(dom > mostDominant.getDominance(target)+GenUtils.randDouble(locationBasedRandom,-dither,dither))
            			mostDominant = sect;
            	}
            	
            	if(nx % BiomeSection.sectionWidth == 0 || nz % BiomeSection.sectionWidth == 0)
            		img.setRGB(nx+x/2, nz+z/2, new Color(255,0,0).getRGB());
            	else {
            		Color col = getClimateColor(mostDominant.getBiomeBank());
//            		if(homeSection.getX() % 2 == 0 && homeSection.getZ() % 2 == 0) {
//            			col = col.darker();
//            			if(homeSection.getDominanceBasedOnRadius(target.getX(), target.getZ()) > 0)
//            				col = col.darker();
//            		}
//            		
//            		//Debug this section
//            		if(homeSection.getX() == debugX && homeSection.getZ() == debugZ) {
//            			col = new Color((col.getRed() + 30) % 255, col.getBlue(), col.getGreen());
//            			
//            		}
            		img.setRGB(nx+x/2, nz+z/2, col.getRGB());
            	}
            }
        }
        try {
            f = new File("terra-preview.png");
            ImageIO.write(img, "png", f);
        } catch (IOException e) {
            System.out.println(e);
        }
        sender.sendMessage("Exported. H: " + highest + ", L: " + lowest);
    }

    
	private Color getClimateColor(BiomeBank bank) {
    	if(bank.getType() == BiomeType.OCEANIC||bank.getType() == BiomeType.DEEP_OCEANIC)
    		return Color.blue;
    	switch(bank.getClimate()) {
    	case HUMID_VEGETATION:
    		return new Color(118,163,3);
    	case WARM_VEGETATION:
    		return new Color(106,168,79);
    	case DRY_VEGETATION:
    		return new Color(172,187,2);
    	case HOT_BARREN:
    		return Color.red;
    	case COLD:
    		return new Color(59, 255, 150);
    	case SNOWY:
    		return Color.white;
		case TRANSITION:
    		return new Color(59, 255, 59);
    	}
    	return Color.pink;
    }

    @SuppressWarnings("unused")
    private Color getBiomeColor(BiomeBank bank) {
    	switch(bank) {
    	case SNOWY_WASTELAND:
    		return Color.white;
    	case SNOWY_TAIGA:
    		return new Color(217,234,211);
    	case ICE_SPIKES:
    		return new Color(207,226,243);
    	case TAIGA:
    		return new Color(56,118,29);
    	case PLAINS:
    		return new Color(59, 255, 59);
    	case ERODED_PLAINS:
    		return new Color(59, 255, 150);
    	case DARK_FOREST:
    		return new Color(39,78,19);
    	case SAVANNA:
    		return new Color(172,187,2);
    	case FOREST:
    		return new Color(106,168,79);
    	case JUNGLE:
    		return new Color(118,163,3);
    	case BAMBOO_FOREST:
    		return new Color(0,255,186);
    	case DESERT:
    		return Color.yellow;
    	case BADLANDS:
    		return Color.red;
    	default:
    		if(bank.getType() == BiomeType.OCEANIC || bank.getType() == BiomeType.DEEP_OCEANIC)
    			return Color.blue;
    		else
    			return Color.pink;
    	}
    }
    
    
    @SuppressWarnings("unused")
	private Color getHeightColorFromNoise(int noise) {
        if (noise <= 62) { //Sea level
            return new Color(50, 50, 100 + (noise * 2));//Blue
        } else if (noise < 62 + 4) { //Beaches?
            return new Color(240, 238, 108);//Yellow
        } else if (noise < 92) {
            return new Color(37, (70 + (noise * 2)), 2);//Green
        } else { //Mountainous
            return new Color(255, 255, 255);//White
        }
    }
}
