package org.terraform.biome.mountainous;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBlender;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.DudChunkData;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;


/**
 * Shattered Savannas do not extend AbstractMountainHandler despite
 * being technically mountains
 * <br>
 * They instead artificially raise height to this point, then
 * just add a flat number and let the blurring process handle the rest.
 * <br>
 * The real interest in the biome lies in its transformer
 */
public class ShatteredSavannaHandler extends AbstractMountainHandler {

    static BiomeBlender biomeBlender;

    private static @NotNull BiomeBlender getBiomeBlender(TerraformWorld tw) {
        if (biomeBlender == null) {
            biomeBlender = new BiomeBlender(tw, true, true).setGridBlendingFactor(4).setSmoothBlendTowardsRivers(2);
        }
        return biomeBlender;
    }

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.SAVANNA_PLATEAU;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(Random rand) {
        return new Material[] {
                Material.GRASS_BLOCK,
                Material.DIRT,
                GenUtils.randChoice(Material.DIRT, Material.STONE),
                GenUtils.randChoice(Material.DIRT, Material.STONE),
                Material.STONE
        };
    }

    @Override
    public @NotNull BiomeHandler getTransformHandler() {
        return this;
    }

    /**
     * One 2D noise value to handle whether or not to carve
     * One 1D noise value as a Y-multiplier
     */
    @Override
    public void transformTerrain(@NotNull ChunkCache cache,
                                 @NotNull TerraformWorld tw,
                                 Random random,
                                 ChunkGenerator.@NotNull ChunkData chunk,
                                 int x,
                                 int z,
                                 int chunkX,
                                 int chunkZ)
    {

        FastNoise creviceNoise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_SHATTERED_SAVANNANOISE, world -> {
            FastNoise n = new FastNoise(tw.getHashedRand(181234, 32189, 16342134).nextInt());
            n.SetNoiseType(NoiseType.SimplexFractal);
            n.SetFractalType(FastNoise.FractalType.Billow);
            n.SetFractalOctaves(1);
            n.SetFrequency(0.02f);
            return n;
        });
        FastNoise yScaleNoise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_SHATTERED_SAVANNANOISE, world -> {
            FastNoise n = new FastNoise(tw.getHashedRand(982374, 18723, 1983701).nextInt());
            n.SetNoiseType(NoiseType.Simplex);
            n.SetFrequency(0.06f);
            return n;
        });
        int rawX = chunkX * 16 + x;
        int rawZ = chunkZ * 16 + z;
        double crevice = Math.abs(creviceNoise.GetNoise(rawX, rawZ));
        // peakHeight *= getBiomeBlender(tw).getEdgeFactor(BiomeBank.SHATTERED_SAVANNA, rawX, rawZ);
        if (crevice < 0.4f) {
            return;
        }

        short baseHeight = cache.getTransformedHeight(x, z);
        int low = (int) HeightMap.CORE.getHeight(tw, rawX, rawZ);
        boolean updateHeight = true;
        for (int y = baseHeight; y > low; y--) {
            // Noise meant to scale with y while making terraces every 10 blocks
            // Additionally, add a small curve to make the land bend a bit
            // Make the pillars connect around baseHeight+0.5*(peakHeight-baseHeight)
            // by multiplying a factor that approaches lower values there
            double scale = (1f - 0.4 * Math.abs(yScaleNoise.GetNoise(y, 0)));
            if (crevice * scale < 0.4f) {
                updateHeight = false;
                continue;
            }
            chunk.setBlock(x, y, z, Material.CAVE_AIR);
            if (updateHeight) {
                cache.writeTransformedHeight(x, z, (short) (y - 1));
            }
        }

        // Make write changes
        if (chunk instanceof DudChunkData) {
            return;
        }

        Material[] crust = getSurfaceCrust(new Random());
        for (int i = 0; i < crust.length; i++) {
            if (BlockUtils.isAir(chunk.getType(x, cache.getTransformedHeight(x, z) - i, z))) {
                return;
            }
            chunk.setBlock(x, cache.getTransformedHeight(x, z) - i, z, crust[i]);
        }
    }

    @Override
    public void populateSmallItems(TerraformWorld world,
                                   @NotNull Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   @NotNull PopulatorDataAbstract data)
    {
        if (surfaceY < TerraformGenerator.seaLevel) {
            return;
        }

        if (data.getType(rawX, surfaceY, rawZ) == Material.GRASS_BLOCK && !data.getType(rawX, surfaceY + 1, rawZ)
                                                                               .isSolid())
        {
            // Dense grass
            if (GenUtils.chance(random, 2, 10)) {
                PlantBuilder.GRASS.build(data, rawX, surfaceY + 1, rawZ);
            }
        }
    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data)
    {
        // Small trees
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 34);
        for (SimpleLocation sLoc : trees) {
            int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            sLoc = sLoc.getAtY(treeY);
            if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome() && BlockUtils.isDirtLike(data.getType(sLoc.getX(),
                    sLoc.getY(),
                    sLoc.getZ())))
            {
                new FractalTreeBuilder(FractalTypes.Tree.SAVANNA_SMALL).build(
                        tw,
                        data,
                        sLoc.getX(),
                        sLoc.getY(),
                        sLoc.getZ()
                );
            }
        }

        // Grass Poffs
        SimpleLocation[] poffs = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 35);
        for (SimpleLocation sLoc : poffs) {
            int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            sLoc = sLoc.getAtY(treeY);
            if (TConfig.arePlantsEnabled()
				&& data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome()
				&& BlockUtils.isDirtLike(data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ()))
				&& !data.getType(sLoc.getX(), sLoc.getY() + 1, sLoc.getZ()).isSolid())
            {
                SimpleBlock base = new SimpleBlock(data, sLoc.getX(), sLoc.getY() + 1, sLoc.getZ());
                int rX = GenUtils.randInt(random, 2, 4);
                int rY = GenUtils.randInt(random, 2, 4);
                int rZ = GenUtils.randInt(random, 2, 4);
                BlockUtils.replaceSphere(random.nextInt(999), rX, rY, rZ, base, false, Material.ACACIA_LEAVES);
            }
        }
    }

}
