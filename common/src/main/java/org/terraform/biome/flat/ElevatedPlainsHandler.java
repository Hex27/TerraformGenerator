package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeBlender;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;
import org.terraform.tree.FractalLeaves;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import java.util.Random;

public class ElevatedPlainsHandler extends BiomeHandler {
    static BiomeBlender biomeBlender;

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.PLAINS;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
    	return new Material[] {Material.STONE};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, int rawX, int surfaceY, int rawZ, PopulatorDataAbstract data) {
        boolean gradient = HeightMap.getTrueHeightGradient(data, rawX, rawZ, 3)
                <= TConfigOption.MISC_TREES_GRADIENT_LIMIT.getDouble();
        if(gradient) {
            data.setType(rawX, surfaceY, rawZ, Material.GRASS_BLOCK);
            if(random.nextBoolean())
                data.setType(rawX, surfaceY-1, rawZ, Material.DIRT);
        }

        if (data.getType(rawX, surfaceY, rawZ) == Material.GRASS_BLOCK &&
                !BlockUtils.isWet(new SimpleBlock(data,rawX,surfaceY,rawZ))) {

            if (GenUtils.chance(random, 1, 10)) { //Grass
                if (GenUtils.chance(random, 6, 10)) {
                    data.setType(rawX, surfaceY + 1, rawZ, Material.GRASS);
                    if (random.nextBoolean()) {
                        BlockUtils.setDoublePlant(data, rawX, surfaceY + 1, rawZ, Material.TALL_GRASS);
                    }
                } else {
                    if (GenUtils.chance(random, 7, 10))
                        data.setType(rawX, surfaceY + 1, rawZ, BlockUtils.pickFlower());
                    else
                        BlockUtils.setDoublePlant(data, rawX, surfaceY + 1, rawZ, BlockUtils.pickTallFlower());
                }
            }
        }
    }

    @Override
    public BiomeHandler getTransformHandler() {
        return this;
    }

    @Override
    public void transformTerrain(ChunkCache cache, TerraformWorld tw, Random random, ChunkGenerator.ChunkData chunk, int x, int z, int chunkX, int chunkZ) {

        int heightFactor = 15;
        int rawX = chunkX * 16 + x;
        int rawZ = chunkZ * 16 + z;

        double preciseHeight = HeightMap.getPreciseHeight(tw, rawX, rawZ);
        int height = (int) preciseHeight;

        int noiseValue = (int) Math.round(heightFactor * getBiomeBlender(tw).getEdgeFactor(BiomeBank.ELEVATED_PLAINS, rawX, rawZ));
        if(noiseValue < 1) return; //If no changes are made, DO NOT TOUCH CACHE

        for (int y = 1; y <= noiseValue; y++) {
            chunk.setBlock(x, height + y, z, getRockAt(random, x,y,z));
        }
        cache.writeTransformedHeight(x,z, (short) (height+noiseValue));

    }
    
    private static final Material[] rocks = new Material[] {
    		Material.GRANITE, Material.GRANITE, Material.GRANITE, 
    		Material.GRANITE, Material.GRANITE, Material.GRANITE, 
    		Material.DIORITE, Material.DIORITE, Material.DIORITE, 
    		Material.ANDESITE, Material.ANDESITE, Material.ANDESITE,
    		Material.DIORITE, Material.DIORITE, Material.DIORITE
    		};
    private Material getRockAt(Random rand, int rawX, int y, int rawZ) {
    	return rocks[((int)Math.round(0.7*rawX + 0.7*(GenUtils.randInt(rand, -1, 1)+y) + 0.7*rawZ)) % rocks.length];
    }

    private static BiomeBlender getBiomeBlender(TerraformWorld tw) {
        if (biomeBlender == null) biomeBlender = new BiomeBlender(tw, true, true)
                .setRiverThreshold(4).setBlendBeaches(false);
        return biomeBlender;
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 18);

        for (SimpleLocation sLoc : trees) {
            if (data.getBiome(sLoc.getX(),sLoc.getZ()) == getBiome()) {
                int treeY = GenUtils.getHighestGround(data, sLoc.getX(),sLoc.getZ());
                sLoc.setY(treeY);
                if(data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ()) != Material.GRASS_BLOCK)
                	continue;
                
                FractalTreeBuilder builder = new FractalTreeBuilder(FractalTypes.Tree.TAIGA_SMALL);
                builder.setTrunkType(Material.OAK_LOG);
        		builder.setFractalLeaves(
        				new FractalLeaves()
        				.setLeafNoiseFrequency(0.65f)
        				.setLeafNoiseMultiplier(0.8f)
                        .setRadius(2).setMaterial(Material.OAK_LEAVES)
                        .setConeLeaves(true));
                
                if(builder.build(tw, data, sLoc.getX(),sLoc.getY(),sLoc.getZ()))
                	BlockUtils.replaceCircularPatch(random.nextInt(99999), 2.5f, new SimpleBlock(data, sLoc), Material.PODZOL);

            }
        }
	}
}
