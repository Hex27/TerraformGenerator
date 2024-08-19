package org.terraform.biome.mountainous;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class SnowyMountainsHandler extends AbstractMountainHandler {

    private static void stoneStack(Material stoneType,
                                   @NotNull PopulatorDataAbstract data,
                                   @NotNull Random rand,
                                   int x,
                                   int y,
                                   int z)
    {
        data.setType(x, y, z, stoneType);

        int depth = GenUtils.randInt(rand, 3, 7);
        for (int i = 1; i < depth; i++) {
            if (!BlockUtils.isStoneLike(data.getType(x, y - i, z))) {
                break;
            }
            data.setType(x, y - i, z, stoneType);
            if (BlockUtils.isExposedToNonSolid(new SimpleBlock(data, x, y - i, z))) {
                depth++;
            }
        }
    }

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.SNOWY_SLOPES;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(Random rand) {
        return new Material[] {Material.STONE};
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
        // Dirt Fixer
        // Snowy wastelands and the like will spawn snow blocks, then dirt blocks.
        // Analyze 5 blocks down. Replace the block if anything next to it is stone.
        correctDirt(new SimpleBlock(data, rawX, surfaceY, rawZ));

        // Snow on top if the biome is the same
        data.setType(rawX, surfaceY + 1, rawZ, Material.SNOW);


        // Make patches of decorative rock on the mountain sides.
        if (GenUtils.chance(random, 1, 25)) {
            Material stoneType = GenUtils.randChoice(Material.ANDESITE, Material.DIORITE);
            stoneStack(stoneType, data, random, rawX, surfaceY, rawZ);
            for (int nx = -2; nx <= 2; nx++) {
                for (int nz = -2; nz <= 2; nz++) {
                    if (GenUtils.chance(random, 1, 5)) {
                        continue;
                    }
                    int stoneY = GenUtils.getHighestGround(data, rawX + nx, rawZ + nz);

                    // Another check, make sure relative position isn't underwater.
                    if (stoneY < TerraformGenerator.seaLevel) {
                        continue;
                    }
                    stoneStack(stoneType, data, random, rawX + nx, stoneY, rawZ + nz);
                }
            }
        }

        // Thick Snow on shallow areas
        // Snowy Snow on near flat areas
        double gradient = HeightMap.getTrueHeightGradient(data, rawX, rawZ, 3);
        if (gradient < 1.4) {

            if (surfaceY < TerraformGenerator.seaLevel) {
                return;
            }
            if (gradient < 1.2) {
                data.setType(rawX, surfaceY, rawZ, Material.POWDER_SNOW);
                data.setType(rawX, surfaceY + 1, rawZ, Material.AIR); // remove snow
            }
            else {
                data.setType(rawX, surfaceY, rawZ, Material.SNOW_BLOCK);
            }
        }
    }

    private void correctDirt(@NotNull SimpleBlock start) {
        for (int depth = 0; depth < 5; depth++) {
            for (BlockFace face : BlockUtils.directBlockFaces) {
                if (start.getRelative(face).getType() == Material.STONE) {
                    start.setType(Material.STONE);
                    break;
                }
            }
            start = start.getDown();
        }
    }

    @Override
    public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
        // TODO Auto-generated method stub

    }

    @Override
    public @NotNull BiomeBank getBeachType() {
        return BiomeBank.ICY_BEACH;
    }

    @Override
    public @NotNull BiomeBank getRiverType() {
        return BiomeBank.FROZEN_RIVER;
    }
}
