package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.SphereBuilder;
import org.terraform.utils.version.V_1_20;
import org.terraform.utils.version.Version;

import java.util.Random;

public class CherryGroveHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.PLAINS;
    }

    @Override
    public @NotNull CustomBiomeType getCustomBiome() {
        return CustomBiomeType.CHERRY_GROVE;
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

        if (data.getType(rawX, surfaceY, rawZ) == Material.GRASS_BLOCK) {

            if (GenUtils.chance(random, 2, 10)) { // Grass
                if (GenUtils.chance(random, 8, 10)) {
                    // Pink petals. No longer generate tall grass.
                    if (Version.isAtLeast(20) && TConfig.arePlantsEnabled() && GenUtils.chance(random, 6, 10)) {
                        data.setBlockData(
                                rawX,
                                surfaceY + 1,
                                rawZ,
                                V_1_20.getPinkPetalData(GenUtils.randInt(1, 4))
                        );
                    }
                    else {
                        PlantBuilder.GRASS.build(data, rawX, surfaceY + 1, rawZ);
                    }
                }
                else {
                    if (GenUtils.chance(random, 7, 10)) {
                        PlantBuilder.ALLIUM.build(data, rawX, surfaceY + 1, rawZ);
                    }
                    else {
                        PlantBuilder.PEONY.build(data, rawX, surfaceY + 1, rawZ);
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

        // Small trees or grass poffs
        SimpleLocation[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 20);

        for (SimpleLocation sLoc : trees) {

            int treeY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            sLoc = sLoc.getAtY(treeY);

            if (tw.getBiomeBank(sLoc.getX(), sLoc.getZ()) == BiomeBank.CHERRY_GROVE
                && BlockUtils.isDirtLike(data.getType(sLoc.getX(), sLoc.getY(), sLoc.getZ())))
            {
                switch (random.nextInt(20)) // 0 to 19 inclusive
                {
                    case 19, 18, 17, 16, 15 -> // Rock (5/20)
                            new SphereBuilder(random,
                                    new SimpleBlock(data, sLoc),
                                    Material.COBBLESTONE,
                                    Material.STONE,
                                    Material.STONE,
                                    Material.STONE,
                                    Material.MOSSY_COBBLESTONE
                            ).setRadius(GenUtils.randInt(random, 3, 5)).setRY(GenUtils.randInt(random, 6, 10)).build();
                    default -> { // Tree (15/20)
                        if (random.nextBoolean())  // small trees
                        {
                            new FractalTreeBuilder(FractalTypes.Tree.CHERRY_SMALL).build(tw,
                                    data,
                                    sLoc.getX(),
                                    sLoc.getY(),
                                    sLoc.getZ()
                            );
                        }
                        else {
                            new FractalTreeBuilder(FractalTypes.Tree.CHERRY_THICK).build(tw,
                                    data,
                                    sLoc.getX(),
                                    sLoc.getY(),
                                    sLoc.getZ()
                            );
                        }
                        // No spore blossoms on 1.20 as the new cherry trees already drop petals
                        if (!Version.isAtLeast(20)) {
                            for (int rX = sLoc.getX() - 6; rX <= sLoc.getX() + 6; rX++) {
                                for (int rZ = sLoc.getZ() - 6; rZ <= sLoc.getZ() + 6; rZ++) {
                                    Wall ceil = new Wall(new SimpleBlock(data, rX, sLoc.getY(), rZ)).findCeiling(15);
                                    if (ceil != null && GenUtils.chance(random, 1, 30)) {
                                        if (ceil.getType() == Material.DARK_OAK_LEAVES) {
                                            PlantBuilder.SPORE_BLOSSOM.build(ceil.getDown());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public @NotNull BiomeBank getBeachType() {
        return BiomeBank.CHERRY_GROVE_BEACH;
    }

    public @NotNull BiomeBank getRiverType() {
        return BiomeBank.CHERRY_GROVE_RIVER;
    }

}
