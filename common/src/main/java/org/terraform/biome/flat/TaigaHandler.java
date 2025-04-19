package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.Ageable;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler;

import java.util.Random;

public class TaigaHandler extends BiomeHandler {
    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.TAIGA;
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
    public void populateSmallItems(TerraformWorld tw,
                                   @NotNull Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   @NotNull PopulatorDataAbstract data)
    {
        // Use noise to group sweet berry bushes
        FastNoise sweetBerriesNoise = NoiseCacheHandler.getNoise(tw,
                NoiseCacheHandler.NoiseCacheEntry.BIOME_TAIGA_BERRY_BUSHNOISE,
                w -> {
                    FastNoise n = new FastNoise((int) (w.getSeed() * 2));
                    n.SetNoiseType(NoiseType.SimplexFractal);
                    n.SetFrequency(0.04f);

                    return n;
                }
        );

        if (BlockUtils.isDirtLike(data.getType(rawX, surfaceY, rawZ))
            && data.getType(rawX, surfaceY+1,rawZ) == Material.AIR) {

            // Generate sweet berry bushes
            if (TConfig.arePlantsEnabled()
                && sweetBerriesNoise.GetNoise(rawX, rawZ) > 0.3
                && sweetBerriesNoise.GetNoise(rawX, rawZ) * random.nextFloat() > 0.35)
            {
                Ageable bush = (Ageable) Material.SWEET_BERRY_BUSH.createBlockData(); // TODO: SmallItemsBuilder
                bush.setAge(GenUtils.randInt(random, 1, 3));
                data.setBlockData(rawX, surfaceY + 1, rawZ, bush);
                return;
            }

            // Generate grass and flowers
            if (GenUtils.chance(random, 1, 16)) {
                int i = random.nextInt(4);

                if (i >= 2) {
                    if (random.nextBoolean()) {
                        PlantBuilder.TALL_GRASS.build(data, rawX, surfaceY + 1, rawZ);
                    }
                    else {
                        PlantBuilder.LARGE_FERN.build(data, rawX, surfaceY + 1, rawZ);
                    }
                }
                else if (i == 1) {
                    if (random.nextBoolean()) {
                        PlantBuilder.GRASS.build(data, rawX, surfaceY + 1, rawZ);
                    }
                    else {
                        PlantBuilder.FERN.build(data, rawX, surfaceY + 1, rawZ);
                    }
                }
                else {
                    BlockUtils.pickFlower().build(data, rawX, surfaceY + 1, rawZ);
                }
            }
        }
    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data)
    {
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 11);

        for (SimpleLocation sLoc : trees) {
            if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome()) {
                int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
                sLoc = sLoc.getAtY(treeY);
                // Rarely spawn huge taiga trees
                if (TConfig.c.TREES_TAIGA_BIG_ENABLED && GenUtils.chance(random, 1, 20)) {
                    FractalTypes.Tree.TAIGA_BIG.build(tw,
                            new SimpleBlock(data, sLoc.getX(), sLoc.getY(), sLoc.getZ())
                    );
                }
                else { // Normal trees
                    FractalTypes.Tree.TAIGA_SMALL.build(tw,
                            new SimpleBlock(data, sLoc.getX(), sLoc.getY(), sLoc.getZ())
                    );
                }
            }
        }

    }

    @Override
    public @NotNull BiomeBank getBeachType() {
        return BiomeBank.ROCKY_BEACH;
    }
}
