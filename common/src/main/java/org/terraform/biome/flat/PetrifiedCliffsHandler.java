package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeBlender;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.small_items.PlantBuilder;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes.Tree;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Random;

public class PetrifiedCliffsHandler extends BiomeHandler {
    public static final EnumSet<Material> endWithStones = EnumSet.of(
        Material.STONE,
        Material.MOSSY_COBBLESTONE,
        Material.COBBLESTONE
    );
    static BiomeBlender biomeBlender;

    private static @NotNull BiomeBlender getBiomeBlender(TerraformWorld tw) {
        if (biomeBlender == null) {
            biomeBlender = new BiomeBlender(tw, true, true).setRiverThreshold(4).setBlendBeaches(false);
        }
        return biomeBlender;
    }

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.BIRCH_FOREST;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[] {
                Material.GRASS_BLOCK,
                Material.DIRT,
                Material.DIRT,
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE),
                GenUtils.randChoice(rand, Material.DIRT, Material.STONE)
        };
    }

    @Override
    public void populateSmallItems(TerraformWorld world,
                                   @NotNull Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   @NotNull PopulatorDataAbstract data)
    {
        for (int i = 0; i < 30; i++) {
            if (data.getType(rawX, surfaceY, rawZ) == Material.DIORITE
                || data.getType(rawX, surfaceY, rawZ) == Material.ANDESITE
                || data.getType(rawX, surfaceY, rawZ) == Material.GRANITE
                || data.getType(rawX, surfaceY, rawZ) == Material.POLISHED_DIORITE
                || data.getType(rawX, surfaceY, rawZ) == Material.POLISHED_ANDESITE
                || data.getType(rawX, surfaceY, rawZ) == Material.POLISHED_GRANITE)
            {
                surfaceY--;
            }
            else {
                break;
            }
        }

        if (data.getType(rawX, surfaceY, rawZ) == Material.GRASS_BLOCK) {
            SimpleBlock core = new SimpleBlock(data, rawX, surfaceY + 1, rawZ);
            boolean continueOut = false;
            for (BlockFace face : BlockUtils.directBlockFaces) {
                Material relType = core.getRelative(face).getType();
                if (endWithStones.contains(relType)) {
                    core.setType(Material.DIORITE_SLAB);
                    continueOut = true;
                    break;
                }
            }
            if (continueOut) {
                return;
            }

            if (GenUtils.chance(random, 1, 10)) { // Grass
                if (GenUtils.chance(random, 6, 10)) {
                    PlantBuilder.GRASS.build(data, rawX, surfaceY + 1, rawZ);
                    if (random.nextBoolean()) {
                        PlantBuilder.TALL_GRASS.build(data, rawX, surfaceY + 1, rawZ);
                    }
                }
                else {
                    if (GenUtils.chance(random, 7, 10)) {
                        BlockUtils.pickFlower().build(data, rawX, surfaceY + 1, rawZ);
                    }
                    else {
                        BlockUtils.pickTallFlower().build(data, rawX, surfaceY + 1, rawZ);
                    }
                }
            }
        }
    }

    @Override
    public BiomeHandler getTransformHandler() {
        return this;
    }

    @Override
    public int getMaxHeightForCaves(@NotNull TerraformWorld tw, int x, int z) {
        return (int) HeightMap.CORE.getHeight(tw, x, z);
    }

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

        FastNoise noise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_PETRIFIEDCLIFFS_CLIFFNOISE, world -> {
            FastNoise n = new FastNoise(tw.getHashedRand(123, 2222, 1111).nextInt(99999));
            n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
            n.SetFractalOctaves(3);
            n.SetFrequency(0.03f);
            return n;
        });

        FastNoise details = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_PETRIFIEDCLIFFS_INNERNOISE, world -> {
            FastNoise n = new FastNoise(tw.getHashedRand(111, 102, 1).nextInt(99999));
            n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
            n.SetFractalOctaves(3);
            n.SetFrequency(0.05f);
            return n;
        });
        // Generates -0.8 to 0.8
        int rawX = chunkX * 16 + x;
        int rawZ = chunkZ * 16 + z;

        double preciseHeight = HeightMap.getPreciseHeight(tw, rawX, rawZ);
        int height = (int) preciseHeight;

        double noiseValue = Math.max(0, noise.GetNoise(rawX, rawZ))
                            * getBiomeBlender(tw).getEdgeFactor(BiomeBank.PETRIFIED_CLIFFS, rawX, rawZ);
        if (noiseValue == 0) {
            return;
        }

        double platformHeight = 7 + noiseValue * 50;

        if (platformHeight > 15) {
            platformHeight = 15 + Math.sqrt(0.5 * (platformHeight - 15));
        }

        for (int y = 1; y <= (int) Math.round(platformHeight); y++) {
            double detailsNoiseMultiplier = Math.pow(1.0 - (1.0 / (Math.pow(platformHeight / 2.0, 2))) * Math.pow(
                    y
                    - platformHeight
                      / 2.0,
                    2
            ), 2);
            double detailsNoise = details.GetNoise(rawX, height + y, rawZ);

            if (0.85 + detailsNoise > detailsNoiseMultiplier) {
                chunk.setBlock(x, height + y, z, GenUtils.randChoice(Material.STONE,
                        Material.STONE,
                        Material.STONE,
                        Material.COBBLESTONE,
                        Material.MOSSY_COBBLESTONE
                ));
                cache.writeTransformedHeight(x, z, (short) Math.max(cache.getTransformedHeight(x, z), height + y));
            }
        }
    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data)
    {
        // Rock trees
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 13, 0.2f);

        for (SimpleLocation sLoc : trees) {
            if (random.nextBoolean()) {
                int treeY = GenUtils.getTrueHighestBlock(data, sLoc.getX(), sLoc.getZ());
                sLoc = sLoc.getAtY(treeY);
                if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome() && data.getType(sLoc.getX(),
                        sLoc.getY(),
                        sLoc.getZ()).toString().endsWith("STONE"))
                {
                    Tree treeType = switch (random.nextInt(3)) {
                        case 0 -> Tree.ANDESITE_PETRIFIED_SMALL;
                        case 1 -> Tree.GRANITE_PETRIFIED_SMALL;
                        default -> Tree.DIORITE_PETRIFIED_SMALL;
                    };
                    new FractalTreeBuilder(treeType).build(tw, data, sLoc.getX(), sLoc.getY(), sLoc.getZ());
                }
            }
        }
    }

    @Override
    public @NotNull BiomeBank getBeachType() {
        return BiomeBank.ROCKY_BEACH;
    }
}
