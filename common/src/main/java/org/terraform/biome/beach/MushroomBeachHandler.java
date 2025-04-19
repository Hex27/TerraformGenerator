package org.terraform.biome.beach;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.HeightMap;
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

import java.util.Random;

public class MushroomBeachHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.MUSHROOM_FIELDS;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[] {
                Material.MYCELIUM,
                Material.DIRT,
                GenUtils.randChoice(rand, Material.STONE, Material.DIRT, Material.DIRT),
                GenUtils.randChoice(rand, Material.STONE, Material.DIRT)
        };
    }

    @Override
    public void populateSmallItems(TerraformWorld tw,
                                   Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   @NotNull PopulatorDataAbstract data)
    {
        if (surfaceY < TerraformGenerator.seaLevel) {
            return;
        }

        // Generate small shrooms
        if (BlockUtils.isDirtLike(data.getType(rawX, surfaceY, rawZ))) {
            if (BlockUtils.isAir(data.getType(rawX, surfaceY + 1, rawZ)) && GenUtils.chance(1, 60)) {
                PlantBuilder.build(
                        data,
                        rawX,
                        surfaceY + 1,
                        rawZ,
                        PlantBuilder.RED_MUSHROOM,
                        PlantBuilder.BROWN_MUSHROOM
                );
            }
        }
    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data)
    {
        SimpleLocation[] bigTrees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 33, 0.15f);
        SimpleLocation[] smallDecorations = GenUtils.randomObjectPositions(
                tw,
                data.getChunkX(),
                data.getChunkZ(),
                15,
                0.3f
        );

        // Giant mushrooms
        for (SimpleLocation sLoc : bigTrees) {
            int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            sLoc = sLoc.getAtY(treeY);
            if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome() && BlockUtils.isDirtLike(data.getType(sLoc.getX(),
                    sLoc.getY(),
                    sLoc.getZ())))
            {
                int choice = random.nextInt(3);
                FractalTypes.Mushroom type = switch (choice) {
                    case 0 -> FractalTypes.Mushroom.GIANT_RED_MUSHROOM;
                    case 1 -> FractalTypes.Mushroom.GIANT_BROWN_MUSHROOM;
                    default -> FractalTypes.Mushroom.GIANT_BROWN_FUNNEL_MUSHROOM;
                };

                if (HeightMap.getTrueHeightGradient(data, sLoc.getX(), sLoc.getZ(), 3)
                    <= TConfig.c.MISC_TREES_GRADIENT_LIMIT)
                {
                    new MushroomBuilder(type).build(tw, data, sLoc.getX(), sLoc.getY(), sLoc.getZ());
                }
            }
        }

        // Small mushrooms and rocks
        for (SimpleLocation sLoc : smallDecorations) {
            int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            sLoc = sLoc.getAtY(treeY);
            if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome() && BlockUtils.isDirtLike(data.getType(sLoc.getX(),
                    sLoc.getY(),
                    sLoc.getZ())))
            {
                int choice = random.nextInt(4);
                switch (choice) {
                    case 0 -> new MushroomBuilder(FractalTypes.Mushroom.SMALL_POINTY_RED_MUSHROOM).build(tw,
                            data,
                            sLoc.getX(),
                            sLoc.getY() + 1,
                            sLoc.getZ()
                    );
                    case 1 -> new MushroomBuilder(FractalTypes.Mushroom.SMALL_BROWN_MUSHROOM).build(tw,
                            data,
                            sLoc.getX(),
                            sLoc.getY() + 1,
                            sLoc.getZ()
                    );
                    case 2 -> new MushroomBuilder(FractalTypes.Mushroom.SMALL_RED_MUSHROOM).build(tw,
                            data,
                            sLoc.getX(),
                            sLoc.getY() + 1,
                            sLoc.getZ()
                    );
                    default -> new MushroomBuilder(FractalTypes.Mushroom.TINY_RED_MUSHROOM).build(tw,
                            data,
                            sLoc.getX(),
                            sLoc.getY() + 1,
                            sLoc.getZ()
                    );
                }
            }
        }

        // Bracket fungus
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x += 2) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z += 2) {
                int y = GenUtils.getHighestGround(data, x, z);
                if (data.getBiome(x, z) != getBiome()) {
                    continue;
                }
                if (y < TerraformGenerator.seaLevel + 4) {
                    continue;
                }
                if (HeightMap.getTrueHeightGradient(data, x, z, 3) > 2 && GenUtils.chance(random, 1, 20)) {
                    BlockUtils.replaceCircle(random.nextInt(919292),
                            3,
                            new SimpleBlock(data, x, y - 2, z),
                            GenUtils.randChoice(random, Material.BROWN_MUSHROOM_BLOCK, Material.RED_MUSHROOM_BLOCK)
                    );
                }
            }
        }
    }
}
