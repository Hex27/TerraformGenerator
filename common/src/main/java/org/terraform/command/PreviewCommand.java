package org.terraform.command;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Stack;

import javax.imageio.ImageIO;

import org.bukkit.command.CommandSender;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeType;
import org.terraform.command.contants.InvalidArgumentException;
import org.terraform.command.contants.TerraCommand;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.noise.FastNoise;

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
        int maxX = 16*10;
        int maxY = TerraformGeneratorPlugin.injector.getMaxY()-TerraformGeneratorPlugin.injector.getMinY();

        TerraformWorld tw = TerraformWorld.get("test-world-"+new Random().nextInt(99999), new Random().nextInt(99999));//TerraformWorld.get("test-world", 11111);
        
        BufferedImage img = new BufferedImage(maxX, maxY, BufferedImage.TYPE_INT_RGB);
        //Delete existing
        File f = new File("terra-preview.png"); if (f.exists()) f.delete();
        for (int x = -maxX/2; x < maxX/2; x++) {
            double height = HeightMap.getPreciseHeight(tw,x,0);
            for (int y = TerraformGeneratorPlugin.injector.getMinY(); y < TerraformGeneratorPlugin.injector.getMaxY(); y++) {
                Color col = Color.WHITE;
                if(y <= height)
                {
                    //Stone
                    col = Color.LIGHT_GRAY;

                    //Apply cave stuff if it is below height
//                    if(cheeseCave(tw,x,y,0,height))
//                        col = Color.BLACK;
                    col = cheeseCave(tw,x,y,0,height);
                }
                else if(y <= TerraformGenerator.seaLevel)
                    col = Color.CYAN;

                //Flip maxY values, images have 0 at the top
                img.setRGB(x+maxX/2, maxY-(y-TerraformGeneratorPlugin.injector.getMinY())-1,
                        col.getRGB());
            }
        }
        try {
            f = new java.io.File("terra-preview.png");
            ImageIO.write(img, "png", f);
        } catch (IOException e) {
            System.out.println(e);
        }
        System.out.println("Done.");
    }

    /**
     * Determine if the x,y,z coordinate should be a noodle cave
     */
    public Color cheeseCave(TerraformWorld tw, int x, int y, int z, double height){
        float filterHeight = barrier(tw, x,y,z, (float)height, 10, 5);
        float filterGround = barrier(tw, x,y,z, (float) TerraformGeneratorPlugin.injector.getMinY(), 20, 5);

        FastNoise cheeseNoise = new FastNoise((int) tw.getSeed());
        cheeseNoise.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
        cheeseNoise.SetFrequency(0.05f);
        cheeseNoise.SetFractalOctaves(2);
        float cheese = cheeseNoise.GetNoise(x*0.5f,y,z*0.5f);

        float caveBoundary = -0.2f; //Check if less than this
        float coreCave = filterHeight*filterGround*cheese;

        //Attempt to make stalactites and stalagmites.
        //No need to apply the filter here because they exist
        //by not carving stuff.
        //Check if this is the wall of the cave. If it is,
        //ADD a low-frequency powered noise for spikes
        if(Math.abs(coreCave-caveBoundary) <= 0.175)
        {
            FastNoise spikeNoise = new FastNoise();
            spikeNoise.SetNoiseType(FastNoise.NoiseType.Simplex);
            spikeNoise.SetFrequency(0.2f);
            //spikeNoise.SetFractalOctaves(2);
            double spike = (1-(Math.abs(coreCave-caveBoundary)/0.175))*
                    Math.pow(Math.max(0,spikeNoise.GetNoise(x,z)),3);
            if(coreCave + spike > caveBoundary && coreCave <= caveBoundary)
                return Color.RED;
        }

        return coreCave <= caveBoundary ? Color.BLACK : Color.LIGHT_GRAY;
    }

    /**
     * Used to prevent functions from passing certain thresholds.
     * Useful for stuff like preventing caves from breaking into
     * the ocean or under minimum Y
     * @return a value between 0 and 1 inclusive.
     */
    public float barrier(TerraformWorld tw, float x, float y, float z, float v, float barrier, float limit){

        FastNoise boundaryNoise = new FastNoise((int) tw.getSeed()*5);
        boundaryNoise.SetNoiseType(FastNoise.NoiseType.Simplex);
        boundaryNoise.SetFrequency(0.01f);
        barrier += 3*boundaryNoise.GetNoise(x,z); //fuzz the boundary

        if(Math.abs(y-v) <= limit)
            return 0;
        else {
            float abs = Math.abs(y - v);
            if(abs < barrier+limit)
                return (abs-limit)/barrier;
            else
                return 1;
        }
    }
    @SuppressWarnings("unused")
	private Color getClimateColor(BiomeBank bank) {
    	if(bank.getType() == BiomeType.OCEANIC||bank.getType() == BiomeType.DEEP_OCEANIC)
    		return Color.blue;
    	switch(bank.getClimate()) {
    	case HUMID_VEGETATION:
    		return new Color(118,163,3);
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
