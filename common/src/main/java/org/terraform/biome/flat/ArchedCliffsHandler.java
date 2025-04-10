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
import org.terraform.data.DudChunkData;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.MushroomBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;

public class ArchedCliffsHandler extends BiomeHandler {
    static BiomeBlender biomeBlender;

    /**
     * Might want to phase this out
     */
    private static @NotNull BiomeBlender getBiomeBlender(TerraformWorld tw) {
        if (biomeBlender == null) {
            biomeBlender = new BiomeBlender(tw, true, true).setGridBlendingFactor(4).setSmoothBlendTowardsRivers(4);
        }
        return biomeBlender;
    }

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.PLAINS;
    }

    // Remove rivers from arched cliffs.
    // Arched cliffs are slightly higher than other biomes to lower beach sizes
    @Override
    public double calculateHeight(TerraformWorld tw, int x, int z) {
        double height = super.calculateHeight(tw, x, z);
        double riverDepth = HeightMap.getRawRiverDepth(tw, x, z);
        if (riverDepth > 0) {
            height += riverDepth;
        }
        return height + 3;
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
        SimpleBlock target = new SimpleBlock(data, rawX, surfaceY, rawZ);

        // Highest Ground decorations: grass and flowers
        if (GenUtils.chance(random, 1, 10)) { // Grass
            if (GenUtils.chance(random, 6, 10)) {
                PlantBuilder.GRASS.build(target.getUp());
                if (random.nextBoolean()) {
                    PlantBuilder.TALL_GRASS.build(target.getUp());
                }
            }
            else {
                if (GenUtils.chance(random, 7, 10)) {
                    BlockUtils.pickFlower().build(target.getUp());
                }
                else {
                    BlockUtils.pickTallFlower().build(target.getUp());
                }
            }
        }

        // Underside decorations: Mushrooms
        SimpleBlock underside = target.findAirPocket(30);
        if (underside != null && underside.getY() > TerraformGenerator.seaLevel) {
            // TODO: Consider optimization: calculateHeight() instead of this shit
            SimpleBlock grassBottom = underside.findStonelikeFloor(50);
            if (grassBottom != null && grassBottom.getY() > TerraformGenerator.seaLevel) {
                if (grassBottom.getType() == Material.GRASS_BLOCK) {
                    // Indicates that this area is valid for population

                    if (GenUtils.chance(random, 1, 10)) {
                        PlantBuilder.build(grassBottom.getUp(), PlantBuilder.RED_MUSHROOM, PlantBuilder.BROWN_MUSHROOM);
                    }

                    // If an underside was valid, you can check the upper area for
                    // decorating overhangs
                    for (BlockFace face : BlockUtils.directBlockFaces) {
                        if (TConfig.arePlantsEnabled() && target.getRelative(face).getType() == Material.AIR) {
                            if (GenUtils.chance(random, 1, 5))
                            // TODO:PlantBuilder
                            {
                                target.getRelative(face).downLPillar(random, random.nextInt(8), Material.OAK_LEAVES);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data)
    {

        // Highest Ground decorations
        // Small trees generate in the presence of light
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 6);

        for (SimpleLocation sLoc : trees) {
            int highestY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            if (BlockUtils.isWet(new SimpleBlock(data, sLoc.getX(), highestY + 1, sLoc.getZ()))) {
                continue;
            }

            sLoc = sLoc.getAtY(highestY);
            if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome() && BlockUtils.isDirtLike(data.getType(sLoc.getX(),
                    sLoc.getY(),
                    sLoc.getZ())))
            {
                new FractalTreeBuilder(FractalTypes.Tree.NORMAL_SMALL).build(
                        tw,
                        data,
                        sLoc.getX(),
                        sLoc.getY(),
                        sLoc.getZ()
                );
            }

        }

        // Mushrooms generate underneath the overhangs
        SimpleLocation[] shrooms = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 10);

        for (SimpleLocation sLoc : shrooms) {
            SimpleBlock target = new SimpleBlock(data, sLoc.getX(), 0, sLoc.getZ()).getGround();
            SimpleBlock underside = target.findAirPocket(30);
            if (underside != null && underside.getY() > TerraformGenerator.seaLevel) {
                SimpleBlock grassBottom = underside.findStonelikeFloor(50);
                if (grassBottom != null && grassBottom.getY() > TerraformGenerator.seaLevel) {
                    if (grassBottom.getType() == Material.GRASS_BLOCK) {
                        // Indicates that this area is valid for population
                        sLoc = sLoc.getAtY(grassBottom.getY());

                        FractalTypes.Mushroom type = switch (random.nextInt(6)) {
                            case 0 -> FractalTypes.Mushroom.MEDIUM_RED_MUSHROOM;
                            case 1 -> FractalTypes.Mushroom.MEDIUM_BROWN_MUSHROOM;
                            case 2 -> FractalTypes.Mushroom.MEDIUM_BROWN_FUNNEL_MUSHROOM;
                            case 3 -> FractalTypes.Mushroom.SMALL_BROWN_MUSHROOM;
                            case 4 -> FractalTypes.Mushroom.SMALL_POINTY_RED_MUSHROOM;
                            default -> FractalTypes.Mushroom.SMALL_RED_MUSHROOM;
                        };

                        new MushroomBuilder(type).build(tw, data, sLoc.getX(), sLoc.getY() + 1, sLoc.getZ());
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
    public void transformTerrain(@NotNull ChunkCache cache,
                                 @NotNull TerraformWorld tw,
                                 @NotNull Random random,
                                 ChunkGenerator.@NotNull ChunkData chunk,
                                 int x,
                                 int z,
                                 int chunkX,
                                 int chunkZ)
    {

        FastNoise platformNoise = NoiseCacheHandler.getNoise(tw,
                NoiseCacheEntry.BIOME_ARCHEDCLIFFS_PLATFORMNOISE,
                world -> {
                    FastNoise n = new FastNoise(tw.getRand(12115222).nextInt());
                    n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
                    n.SetFractalOctaves(3);
                    n.SetFrequency(0.01f);
                    return n;
                }
        );

        FastNoise pillarNoise = NoiseCacheHandler.getNoise(tw,
                NoiseCacheEntry.BIOME_ARCHEDCLIFFS_PILLARNOISE,
                world -> {
                    FastNoise n = new FastNoise(tw.getRand(12544422).nextInt());
                    n.SetNoiseType(FastNoise.NoiseType.SimplexFractal);
                    n.SetFractalOctaves(4);
                    n.SetFrequency(0.01f);
                    return n;
                }
        );

        int rawX = chunkX * 16 + x;
        int rawZ = chunkZ * 16 + z;

        double preciseHeight = HeightMap.getPreciseHeight(tw, rawX, rawZ);
        int height = (int) preciseHeight;

        // Round to force a 0 if the value is too low. Makes blending better.
        double platformNoiseVal = Math.round(Math.max(platformNoise.GetNoise(rawX, rawZ)
                                                      * 70
                                                      * getBiomeBlender(tw).getEdgeFactor(
                BiomeBank.ARCHED_CLIFFS,
                rawX,
                rawZ
        ), 0));

        if (platformNoiseVal >= 1) {
            int platformHeight = (int) (HeightMap.CORE.getHeight(tw, rawX, rawZ) - HeightMap.ATTRITION.getHeight(
                    tw,
                    rawX,
                    rawZ
            ) + 55);

            // for higher platform noise vals, make a thicker platform
            cache.writeTransformedHeight(x, z, (short) platformHeight);
            chunk.setBlock(x, platformHeight, z, Material.GRASS_BLOCK);
            Material[] crust = getSurfaceCrust(random);
            for (int i = 0; i < platformNoiseVal; i++) {
                if (i < crust.length) {
                    chunk.setBlock(x, platformHeight - i, z, crust[i]);
                }
                else {
                    chunk.setBlock(x, platformHeight - i, z, Material.STONE);
                }
            }

            // This is for the bottom platform
            // DOES NOT change height, so can be ignored in pure height calculation
            // This is bad practice
            if (!(chunk instanceof DudChunkData) && platformNoiseVal > 6) {
                int pillarNoiseVal = (int) ((platformNoiseVal / 10.0) * ((0.1 + Math.abs(pillarNoise.GetNoise(
                        rawX,
                        rawZ
                ))) * 20.0));
                if (pillarNoiseVal + height > platformHeight) {
                    pillarNoiseVal = platformHeight - height;
                }

                // Crust cannot be under solids.
                // Guarded from DudChunkData, so safe to read
                boolean applyCrust = !chunk.getType(x, height + pillarNoiseVal + 1, z).isSolid();

                for (int i = pillarNoiseVal; i >= 1; i--) {
                    if ((pillarNoiseVal - i) < crust.length && applyCrust) {
                        chunk.setBlock(x, height + i, z, crust[pillarNoiseVal - i]);
                    }
                    else {
                        chunk.setBlock(x, height + i, z, Material.STONE);
                    }
                }
            }
        }
    }

    @Override
    public int getMaxHeightForCaves(@NotNull TerraformWorld tw, int x, int z) {
        return (int) HeightMap.CORE.getHeight(tw, x, z);
    }


}
