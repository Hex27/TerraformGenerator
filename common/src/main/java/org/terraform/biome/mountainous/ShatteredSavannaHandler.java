package org.terraform.biome.mountainous;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeSection;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CylinderBuilder;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;
import java.util.Random;

public class ShatteredSavannaHandler extends AbstractMountainHandler {
	
    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.SAVANNA_PLATEAU;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{
        		Material.GRASS_BLOCK,
        		Material.DIRT,
        		GenUtils.randMaterial(Material.DIRT, Material.STONE),
        		GenUtils.randMaterial(Material.DIRT, Material.STONE),
        		Material.STONE
        		};
    }

    @Override
    protected double getPeakMultiplier(BiomeSection section, Random sectionRandom)
    {
    	double original = super.getPeakMultiplier(section, sectionRandom);
    	return 1.0 + (original - 1.0)*0.2;
    }
//    FastNoise shatteredSavannaNoise = NoiseCacheHandler.getNoise(
//            tw,
//            NoiseCacheEntry.BIOME_SHATTERED_SAVANNANOISE,
//            world -> {
//                FastNoise n = new FastNoise((int) (world.getSeed()*2));
//                n.SetNoiseType(NoiseType.SimplexFractal);
//                n.SetFractalOctaves(6);
//                n.SetFrequency(0.03f);
//                return n;
//            });
    @Override
    public double calculateHeight(TerraformWorld tw, int x, int z) {
    	BiomeSection b = BiomeBank.getBiomeSectionFromBlockCoords(tw,x,z);
        int cap = tw.getHashedRand(b.getX(),b.getZ(),7312849).nextInt();
        double height = super.calculateHeight(tw, x, z);

        //Plateau-ify height
        //height = Math.max(Math.pow(height, 1.5), );

        
    	return height;
    }
    
    @Override
    public void populateSmallItems(TerraformWorld world, Random random, int rawX, int surfaceY, int rawZ,  PopulatorDataAbstract data) {
        if(surfaceY < TerraformGenerator.seaLevel) return;

        if (data.getType(rawX, surfaceY, rawZ) == Material.GRASS_BLOCK
                && !data.getType(rawX, surfaceY + 1, rawZ).isSolid()) {
            //Dense grass
            if (GenUtils.chance(random, 2, 10)) {
                data.setType(rawX, surfaceY+1, rawZ, Material.GRASS);
            }
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
        //Small trees
	    SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 34);
        for (SimpleLocation sLoc : trees) {
     	    int treeY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
		    sLoc.setY(treeY);
		    if(data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome() &&
		           BlockUtils.isDirtLike(data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ()))) {
		           new FractalTreeBuilder(FractalTypes.Tree.SAVANNA_SMALL).build(tw, data, sLoc.getX(), sLoc.getY(), sLoc.getZ());
		    }
        }
        
       
        //Grass Poffs
        SimpleLocation[] poffs = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 35);
        for (SimpleLocation sLoc : poffs) {
     	   int treeY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
		   sLoc.setY(treeY);
		   if(data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome() &&
		           BlockUtils.isDirtLike(data.getType(sLoc.getX(),sLoc.getY(),sLoc.getZ())) &&
		           !data.getType(sLoc.getX(),sLoc.getY()+1,sLoc.getZ()).isSolid()) {
               SimpleBlock base = new SimpleBlock(data, sLoc.getX(), sLoc.getY() + 1, sLoc.getZ());
               int rX = GenUtils.randInt(random, 2, 4);
               int rY = GenUtils.randInt(random, 2, 4);
               int rZ = GenUtils.randInt(random, 2, 4);
               BlockUtils.replaceSphere(random.nextInt(999), rX, rY, rZ, base, false, Material.ACACIA_LEAVES);
		    } 
        }
	}
	
	@Override
	public BiomeBank getBeachType() {
		return BiomeBank.SANDY_BEACH;
	}

	@Override
	public BiomeBank getRiverType() {
		return BiomeBank.RIVER;
	}
}
