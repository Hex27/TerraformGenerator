package org.terraform.biome.cavepopulators;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.terraform.coregen.populatordata.PopulatorDataICABiomeWriterAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.MultipleFacingBuilder;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.version.OneOneNineBlockHandler;

import java.util.Random;

public class CaveFluidClusterPopulator extends AbstractCaveClusterPopulator {

    boolean skip = false;
    SimpleBlock fluidCenter;
    Random rand;
    Material fluid;
    int rX, rY, rZ;

	public CaveFluidClusterPopulator(float radius) {
		super(radius);
	}
    @Override
	public void oneUnit(TerraformWorld tw, Random doNotUse, SimpleBlock ceil, SimpleBlock floor) {
    	if(skip || ceil == null || floor == null) return;
        if(fluidCenter == null)
        {
            rand = tw.getHashedRand(center.getX(),center.getY(),center.getZ());
            //fluidCenter = center.findFloor(20);
//            if(fluidCenter == null){
//                skip = true;
//                return;
//            }

            fluid = GenUtils.choice(rand, new Material[]{Material.WATER, Material.LAVA});
            rX = -16 + rand.nextInt(33);
            rY = -8 + rand.nextInt(17); //Squashed Y
            rZ = -16 + rand.nextInt(33);
        }

/*        FastNoise fluidNoise = NoiseCacheHandler.getNoise(tw, NoiseCacheHandler.NoiseCacheEntry.CAVE_CHEESE_NOISE, world -> {
            FastNoise n = new FastNoise((int) (tw.getSeed() + 723891));
            n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
            n.SetFrequency(0.03f);
            n.SetFractalOctaves(2);
            return n;
        });

        float fluidNoiseVal = fluidNoise.GetNoise(floor.getX(), floor.getY());*/
        /*if(floor.getY() > fluidCenter.getY()
                || Math.abs(floor.getX() - fluidCenter.getX()) > rX
                || Math.abs(floor.getZ() - fluidCenter.getZ()) > rZ) return;
*/
        Material original = floor.getType();
        //floor = floor.getDown();
        for(int i = 0; i < rY; i++)
        {
/*            if(i != 0 && !floor.getType().isSolid())
            {
                floor.setType(original);
                break;
            }*/
            floor.setType(fluid);
            floor = floor.getDown();
        }

    }
    
    
}
