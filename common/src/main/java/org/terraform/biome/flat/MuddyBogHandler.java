package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.MushroomBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.Random;

public class MuddyBogHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.SWAMP;
    }

    @Override
    public @NotNull CustomBiomeType getCustomBiome() {
        return CustomBiomeType.MUDDY_BOG;
    }

    // Beach type. This will be used instead if the height is too close to sea level.
    public @NotNull BiomeBank getBeachType() {
        return BiomeBank.BOG_BEACH;
    }

    // River type. This will be used instead if the heightmap got carved into a river.
    public @NotNull BiomeBank getRiverType() {
        return BiomeBank.BOG_RIVER;
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
        SimpleBlock block = new SimpleBlock(data, rawX, surfaceY, rawZ);
        if (block.getUp().getType() == Material.AIR && block.getType() == Material.GRASS_BLOCK) {
            if (GenUtils.chance(random, 1, 85)) {
                PlantBuilder.DEAD_BUSH.build(block.getUp());
            }
            else if (GenUtils.chance(random, 1, 85)) {
                PlantBuilder.BROWN_MUSHROOM.build(block.getUp());
            }
            else if (GenUtils.chance(random, 1, 85)) {
                PlantBuilder.GRASS.build(block.getUp());
            }
            else if (GenUtils.chance(random, 1, 85)) {
                PlantBuilder.TALL_GRASS.build(data, rawX, surfaceY + 1, rawZ);
            }
            else if (TConfig.areDecorationsEnabled() && GenUtils.chance(random, 1, 300)) {// Dripstone Cluster
                BlockUtils.replaceCircularPatch(random.nextInt(9999), 2.5f, block, Material.DRIPSTONE_BLOCK);
                if (GenUtils.chance(random, 1, 7)) {
                    BlockUtils.upLPointedDripstone(GenUtils.randInt(random, 2, 4), block.getUp());
                }
                for (BlockFace face : BlockUtils.xzPlaneBlockFaces) {
                    if (GenUtils.chance(random, 1, 7)) {
                        BlockUtils.upLPointedDripstone(GenUtils.randInt(random, 2, 4),
                                block.getRelative(face).getGround().getUp()
                        );
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
        // Small brown mushrooms on dry areas
        SimpleLocation[] shrooms = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 16);

        for (SimpleLocation sLoc : shrooms) {
            int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            sLoc = sLoc.getAtY(treeY);
            if (isRightBiome(tw.getBiomeBank(sLoc.getX(), sLoc.getZ())) && !BlockUtils.isWet(new SimpleBlock(data,
                    sLoc.getX(),
                    sLoc.getY() + 1,
                    sLoc.getZ())) && BlockUtils.isDirtLike(data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ())))
            {
                if (data.getType(sLoc.getX(), sLoc.getY() + 1, sLoc.getZ()) == Material.AIR) {
                    if (random.nextBoolean()) {
                        new MushroomBuilder(FractalTypes.Mushroom.SMALL_BROWN_MUSHROOM).build(tw,
                                data,
                                sLoc.getX(),
                                sLoc.getY() + 1,
                                sLoc.getZ()
                        );
                    }
                    else {
                        new MushroomBuilder(FractalTypes.Mushroom.TINY_BROWN_MUSHROOM).build(tw,
                                data,
                                sLoc.getX(),
                                sLoc.getY() + 1,
                                sLoc.getZ()
                        );
                    }
                }
            }
        }
    }

    private boolean isRightBiome(BiomeBank bank) {
        return bank == BiomeBank.MUDDY_BOG || bank == BiomeBank.BOG_BEACH;
    }

    @Override
    public double calculateHeight(TerraformWorld tw, int x, int z) {

        double height = super.calculateHeight(tw, x, z) - 5;

        FastNoise sinkin = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_MUDDYBOG_HEIGHTMAP, world -> {
            FastNoise n = new FastNoise((int) world.getSeed());
            n.SetNoiseType(NoiseType.SimplexFractal);
            n.SetFractalOctaves(4);
            n.SetFrequency(0.08f);
            return n;
        });

        if (sinkin.GetNoise(x, z) < -0.15) {
            if (height > TerraformGenerator.seaLevel) {
                height -= (height - TerraformGenerator.seaLevel) + 2;
            }
        }

        return height;
    }
}
