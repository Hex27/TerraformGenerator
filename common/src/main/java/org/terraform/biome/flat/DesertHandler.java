package org.terraform.biome.flat;

import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeHandler;
import org.terraform.biome.beach.OasisBeach;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.version.V_1_21_5;
import org.terraform.utils.version.Version;

import java.util.Random;

public class DesertHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.DESERT;
    }

    @Override
    public BiomeHandler getTransformHandler() {
        return this;
    }

    @Override
    public @NotNull BiomeBank getRiverType() {
        return BiomeBank.DESERT_RIVER;
    }

    // Pad more sandstone so that mountains don't get stone exposed vertically
    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[] {
                Material.SAND,
                Material.SAND,
                GenUtils.randChoice(rand, Material.SANDSTONE, Material.SAND),
                Material.SANDSTONE,
                Material.SANDSTONE,
                Material.SANDSTONE,
                Material.SANDSTONE,
                GenUtils.randChoice(rand, Material.SANDSTONE, Material.STONE),
                GenUtils.randChoice(rand, Material.SANDSTONE, Material.STONE)
        };
    }

    @Override
    public void populateSmallItems(@NotNull TerraformWorld world,
                                   @NotNull Random random,
                                   int rawX,
                                   int surfaceY,
                                   int rawZ,
                                   @NotNull PopulatorDataAbstract data)
    {
        boolean cactusGathering = GenUtils.chance(random, 1, 100);
        OasisBeach.generateOasisBeach(world, random, data, rawX, rawZ, BiomeBank.DESERT);

        Material base = data.getType(rawX, surfaceY, rawZ);

        if (cactusGathering) {
            if (GenUtils.chance(random, 5, 100)) {
                data.setType(rawX, surfaceY, rawZ, Material.DIRT_PATH);
            }
        }

        if (base == Material.SAND) {

            //Checks for cactus
            if (GenUtils.chance(random, 1, 100)
                || (GenUtils.chance(random, 1, 20) && cactusGathering)) {
                for (BlockFace face : BlockUtils.directBlockFaces) {
                    if (data.getType(rawX + face.getModX(), surfaceY + 1, rawZ + face.getModZ()) != Material.AIR) {
                        return;
                    }
                }
                int cactusHeight = PlantBuilder.CACTUS.build(random, data, rawX, surfaceY + 1, rawZ, 3, 5);
                if(Version.isAtLeast(21.5)
                   && GenUtils.chance(random, 1, 10))
                    data.setType(rawX, surfaceY+1+cactusHeight, rawZ, V_1_21_5.CACTUS_FLOWER);
            }
            else if (GenUtils.chance(random, 1, 80)) {
                PlantBuilder.build(new SimpleBlock(data,rawX, surfaceY+1,rawZ),
                        PlantBuilder.DEAD_BUSH, PlantBuilder.SHORT_DRY_GRASS, PlantBuilder.TALL_DRY_GRASS);
            }
        }
    }

    @Override
    public void populateLargeItems(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data)
    {

        // Rib cages
        SimpleLocation[] ribCages = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 256, 0.6f);

        for (SimpleLocation sLoc : ribCages) {
            int ribY = GenUtils.getHighestGround(data, sLoc.getX(), sLoc.getZ());
            sLoc = sLoc.getAtY(ribY - GenUtils.randInt(random, 0, 6));
            if (data.getBiome(sLoc.getX(), sLoc.getZ()) == getBiome()
                && data.getType(sLoc.getX(), ribY, sLoc.getZ()) == Material.SAND)
            {
                spawnRibCage(random, new SimpleBlock(data, sLoc.getX(), sLoc.getY(), sLoc.getZ()));
            }
        }
    }

    public void spawnRibCage(@NotNull Random random, @NotNull SimpleBlock target) {
        if (!TConfig.areStructuresEnabled()) {
            return;
        }

        BlockFace direction = BlockUtils.getDirectBlockFace(random);
        int spineLength = GenUtils.randInt(random, 10, 14);
        float ribWidthRadius = GenUtils.randInt(random, 1, 2) + (float) spineLength / 2;
        float ribHeightRadius = 0.7f * ribWidthRadius; // GenUtils.randInt(random, 6, 8);
        // eqn -> ((y-ribHeight)/(ribHeight))^2 + ((x)/(ribWidth))^2 = 1
        int interval = 2;
        if (random.nextBoolean()) {
            interval += 1;
        }

        float ribSizeMultiplier = 1f;

        for (int segmentIndex = 0; segmentIndex < spineLength; segmentIndex++) {
            Wall seg = new Wall(target.getRelative(direction, segmentIndex), direction);
            new OrientableBuilder(Material.BONE_BLOCK).setAxis(BlockUtils.getAxisFromBlockFace(direction)).apply(seg);

            if (segmentIndex < (int) (spineLength / 2f)) {
                ribSizeMultiplier += 0.05f;
            }
            else if (segmentIndex > (int) (spineLength / 2f)) {
                ribSizeMultiplier -= 0.05f;
            }

            if (segmentIndex % interval == 0 && segmentIndex > spineLength / 6) {
                for (float nHor = 1; nHor <= ribWidthRadius * ribSizeMultiplier; nHor += 0.01F) {

                    int[] multipliers = {-1};
                    if (nHor > ribWidthRadius * ribSizeMultiplier / 3) {
                        multipliers = new int[] {-1, 1};
                    }

                    for (int multiplier : multipliers) {
                        int ny = (int) Math.round(ribHeightRadius * ribSizeMultiplier + (multiplier
                                                                                         * ribHeightRadius
                                                                                         * ribSizeMultiplier
                                                                                         * Math.sqrt(1 - Math.pow((nHor)
                                                                                                                  / (ribWidthRadius
                                                                                                                     * ribSizeMultiplier),
                                2
                        ))));

                        int horRel = Math.round(nHor);
                        Axis axis = BlockUtils.getAxisFromBlockFace(BlockUtils.getLeft(direction));
                        if (ny > ribSizeMultiplier * ribHeightRadius / 3
                            && ny < 5 * ribSizeMultiplier * ribHeightRadius / 3)
                        {
                            axis = Axis.Y;
                        }

                        new OrientableBuilder(Material.BONE_BLOCK).setAxis(axis)
                                                                  .apply(seg.getRelative(0, ny, 0).getRight(horRel))
                                                                  .apply(seg.getRelative(0, ny, 0).getLeft(horRel));
                    }
                }

            }
        }

    }
}
