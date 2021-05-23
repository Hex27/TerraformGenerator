package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;

import java.util.Random;

public class TaigaHandler extends BiomeHandler {
    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.TAIGA;
    }

//	@Override
//	public int getHeight(int x, int z, Random rand) {
//		SimplexOctaveGenerator gen = new SimplexOctaveGenerator(rand, 2);
//		gen.setScale(0.005);
//		
//		return (int) (gen.noise(x, z, 0.5, 0.5)*7D+50D);
//	}

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{
        		Material.GRASS_BLOCK,
                Material.DIRT,
                Material.DIRT,
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {

        // Generate grass
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getTrueHighestBlock(data, x, z);
                if (data.getBiome(x, z) != getBiome()) continue;

                if (BlockUtils.isDirtLike(data.getType(x, y, z))) {
                    if (GenUtils.chance(random, 1, 20)) {
                        data.setType(x, y + 1, z, Material.GRASS);
                        if (random.nextBoolean()) {
                            BlockUtils.setDoublePlant(data, x, y + 1, z, Material.TALL_GRASS);
                        } else {
                            data.setType(x, y + 1, z, BlockUtils.pickFlower());
                        }
                    }
                }
            }
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 11);

        for (SimpleLocation sLoc : trees) {
            if (data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome()) {
                int treeY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
                sLoc.setY(treeY);
                // Rarely spawn huge taiga trees
                if (TConfigOption.TREES_TAIGA_BIG_ENABLED.getBoolean() && GenUtils.chance(random, 1, 20)) {
                    if(new FractalTreeBuilder(FractalTypes.Tree.TAIGA_BIG).build(tw, data, sLoc.getX(),sLoc.getY(),sLoc.getZ())) 
	                    replacePodzol(
	                    		tw.getHashedRand(sLoc.getX(),sLoc.getY(),sLoc.getZ()).nextInt(9999),
	                    		5f,
	                    		new SimpleBlock(data,sLoc.getX(),sLoc.getY()-1,sLoc.getZ()));
                }else { // Normal trees
                    if(new FractalTreeBuilder(FractalTypes.Tree.TAIGA_SMALL)
                    		.build(tw, data, sLoc.getX(),sLoc.getY(),sLoc.getZ()))
	                    replacePodzol(
	                    		tw.getHashedRand(sLoc.getX(),sLoc.getY(),sLoc.getZ()).nextInt(9999),
	                    		3.5f,
	                    		new SimpleBlock(data,sLoc.getX(),sLoc.getY()-1,sLoc.getZ()));
                }
            }
        }

	}
	
	@Override
	public BiomeBank getBeachType() {
		return BiomeBank.ROCKY_BEACH;
	}
	
    /**
     * Replaces the highest dirt-like blocks with a noise-fuzzed 
     * circle of Podzol. Fuzzes the edges.
     * @param seed
     * @param radius
     * @param base
     * @param type
     */
    public static void replacePodzol(int seed, float radius, SimpleBlock base) {
    	if (radius <= 0) return;
        if (radius <= 0.5) {
            //block.setReplaceType(ReplaceType.ALL);
            base.setType(GenUtils.randMaterial(new Random(seed), Material.PODZOL));
            return;
        }
        
        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.13f);
        Random rand = new Random(seed);
        for (float x = -radius; x <= radius; x++) {
            for (float z = -radius; z <= radius; z++) {
                SimpleBlock rel = base.getRelative(Math.round(x), 0, Math.round(z));
                rel = rel.getGround();
                if(!BlockUtils.isDirtLike(rel.getType()))
                	continue;
                //double radiusSquared = Math.pow(trueRadius+noise.GetNoise(rel.getX(), rel.getY(), rel.getZ())*2,2);
                double equationResult = Math.pow(x, 2) / Math.pow(radius, 2)
                        + Math.pow(z, 2) / Math.pow(radius, 2);
                double noiseVal = Math.abs(noise.GetNoise(rel.getX(), rel.getZ()));
                if (equationResult <= 1.0+noiseVal) {
                    //if(rel.getLocation().distanceSquared(block.getLocation()) <= radiusSquared){          
                    if(equationResult * 4 > 0.7+noiseVal) {
                    	if(rand.nextBoolean())
                    		rel.setType(Material.PODZOL);
                    }else {
                    	rel.setType(Material.PODZOL);
                    }
                }
            }
        }
    }
}
