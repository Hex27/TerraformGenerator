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
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;

public class GorgeHandler extends BiomeHandler {
    static final BiomeHandler plainsHandler = BiomeBank.PLAINS.getHandler();
    static final boolean slabs = TConfig.c.MISC_USE_SLABS_TO_SMOOTH;
    static BiomeBlender biomeBlender;

    private static @NotNull BiomeBlender getBiomeBlender(TerraformWorld tw) {
        if (biomeBlender == null) {
            biomeBlender = new BiomeBlender(tw, true, true).setGridBlendingFactor(2).setSmoothBlendTowardsRivers(4);
        }
        return biomeBlender;
    }

    @Override
    public boolean isOcean() {
        return plainsHandler.isOcean();
    }

    @Override
    public Biome getBiome() {
        return plainsHandler.getBiome();
    }

    // Remove rivers from gorges.
    @Override
    public double calculateHeight(TerraformWorld tw, int x, int z) {
        double height = super.calculateHeight(tw, x, z);
        double riverDepth = HeightMap.getRawRiverDepth(tw, x, z);
        if (riverDepth > 0) {
            height += riverDepth;
        }
        return height;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return plainsHandler.getSurfaceCrust(rand);
    }

    @Override
    public void populateSmallItems(TerraformWorld world,
                                   @NotNull Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   @NotNull PopulatorDataAbstract data)
    {

        SimpleBlock target = new SimpleBlock(data, rawX, surfaceY + 1, rawZ);
        boolean wasBelowSea = false;
        // the repair work is here because it needs the 3x3 boundary
        // for cave air that is BESIDE the water
        // DOES NOT change height truth because another block MUST be above the
        // one being changed due to the way this works
        while (target.getY() <= TerraformGenerator.seaLevel - 20) {
            wasBelowSea = true;
            if (target.getType() == Material.WATER) {
                for (BlockFace face : BlockUtils.directBlockFaces) {
                    if (BlockUtils.isAir(target.getRelative(face).getType())) {
                        target.getRelative(face).setType(Material.STONE);
                    }
                }
            }
            target = target.getUp();
        }

        // Do not do dry decorations if this was water
        if (wasBelowSea) {
            return;
        }

        target = target.getGround();

        if (!BlockUtils.isWet(target.getUp()) && target.getType() == Material.STONE) {
            // Make the ground more dynamic
            target.setType(Material.GRASS_BLOCK);
            target.getDown().setType(Material.DIRT);
            if (random.nextBoolean()) {
                target.getDown(2).setType(Material.DIRT);
                if (random.nextBoolean()) {
                    target.getDown(3).setType(Material.DIRT);
                }
            }
        }

        plainsHandler.populateSmallItems(world, random, rawX, surfaceY, rawZ, data);
    }

    @Override
    public BiomeHandler getTransformHandler() {
        return this;
    }

    @Override
    public void transformTerrain(@NotNull ChunkCache cache,
                                 TerraformWorld tw,
                                 Random random,
                                 ChunkGenerator.@NotNull ChunkData chunk,
                                 int x,
                                 int z,
                                 int chunkX,
                                 int chunkZ)
    {

        FastNoise cliffNoise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_GORGE_CLIFFNOISE, world -> {
            FastNoise n = new FastNoise();
            n.SetNoiseType(FastNoise.NoiseType.CubicFractal);
            n.SetFractalOctaves(3);
            n.SetFrequency(0.04f);
            return n;
        });

        FastNoise detailsNoise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_GORGE_DETAILS, world -> {
            FastNoise n = new FastNoise();
            n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
            n.SetFrequency(0.03f);
            return n;
        });


        double threshold = 0.1;
        int heightFactor = 12;

        int rawX = chunkX * 16 + x;
        int rawZ = chunkZ * 16 + z;

        double preciseHeight = HeightMap.getPreciseHeight(tw, rawX, rawZ);
        int height = (int) preciseHeight;

        double rawCliffNoiseVal = cliffNoise.GetNoise(rawX, rawZ);
        double noiseValue = rawCliffNoiseVal * getBiomeBlender(tw).getEdgeFactor(BiomeBank.GORGE, rawX, rawZ);
        double detailsValue = detailsNoise.GetNoise(rawX, rawZ);

        // Raise up a tall area
        if (noiseValue >= 0) {
            double d = (noiseValue / threshold) - (int) (noiseValue / threshold) - 0.5;
            double platformHeight = (int) (noiseValue / threshold) * heightFactor
                                    + (64 * Math.pow(d, 7) * heightFactor)
                                    + detailsValue * heightFactor * 0.5;

            if (Math.round(platformHeight) >= 1) {
                cache.writeTransformedHeight(x, z, (short) (Math.round(platformHeight) + height));
            }
            for (int y = 1; y <= (int) Math.round(platformHeight); y++) {
                Material material = GenUtils.randChoice(Material.STONE,
                        Material.STONE,
                        Material.STONE,
                        Material.STONE,
                        Material.COBBLESTONE,
                        Material.COBBLESTONE,
                        Material.ANDESITE,
                        Material.ANDESITE
                );

                if (slabs
                    && material != Material.GRASS_BLOCK
                    && y == (int) Math.round(platformHeight)
                    && platformHeight - (int) platformHeight >= 0.5)
                {
                    material = Material.getMaterial(material.name() + "_SLAB");
                }
                chunk.setBlock(x, height + y, z, material);
            }
            if (detailsValue < 0.2 && GenUtils.chance(3, 4)) {
                chunk.setBlock(x, height + (int) Math.round(platformHeight), z, Material.GRASS_BLOCK);

            }
        }
        else // Burrow a gorge deep down like a ravine
        {
            int depth = (int) Math.sqrt(Math.abs(rawCliffNoiseVal * getBiomeBlender(tw).getEdgeFactor(
                    BiomeBank.GORGE,
                    rawX,
                    rawZ
            )) * 200 * 50);

            // Smooth out anything that crosses the water threshold
            if (height - depth < TerraformGenerator.seaLevel - 20) {
                int depthToPreserve = height - (TerraformGenerator.seaLevel - 20);
                depth = (int) (depthToPreserve + Math.round(Math.sqrt(depth - depthToPreserve)));
            }

            // Prevent going beneath y = 10
            if (depth > height - 10) {
                depth = height - 10;
            }
            // No guard here, depth is an integer, so if its 0, this cache write is safe
            cache.writeTransformedHeight(x, z, (short) (height - depth));
            for (int y = 0; y < depth; y++) {
                if (TerraformGenerator.seaLevel - 20 >= height - y) {
                    chunk.setBlock(x, height - y, z, Material.WATER);
                }
                else {
                    chunk.setBlock(x, height - y, z, Material.AIR);
                }
            }

            // Stop water from escaping. Also makes the highest-ground assertion true
            // MYSTERIO IS THE TRUTH
            if (height - depth <= TerraformGenerator.seaLevel - 20) {
                chunk.setBlock(x, height - depth, z, Material.STONE);
            }
        }
    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data)
    {
        plainsHandler.populateLargeItems(tw, random, data);

        // Spawn rocks
        SimpleLocation[] rocks = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 17, 0.4f);

        for (SimpleLocation sLoc : rocks) {
            if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome()) {
                int rockY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
                sLoc = sLoc.getAtY(rockY);
                if (rockY > TerraformGenerator.seaLevel - 18) {
                    continue;
                }

                BlockUtils.replaceSphere(random.nextInt(91822),
                        (float) GenUtils.randDouble(random, 3, 6),
                        (float) GenUtils.randDouble(random, 4, 7),
                        (float) GenUtils.randDouble(random, 3, 6),
                        new SimpleBlock(data, sLoc),
                        true,
                        GenUtils.randChoice(Material.GRANITE, Material.ANDESITE, Material.DIORITE)
                );
            }
        }
    }


}
