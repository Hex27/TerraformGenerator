package org.terraform.biome.beach;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class IcyBeachHandler extends BiomeHandler {

    private static void makeIceSheet(int x, int y, int z, @NotNull PopulatorDataAbstract data, @NotNull Random random) {
        int length = GenUtils.randInt(6, 16);
        int nx = x;
        int nz = z;
        while (length > 0) {
            length--;
            if (data.getType(nx, y, nz).isSolid() && data.getType(nx, y + 1, nz) == Material.AIR) {
                data.setType(nx, y, nz, Material.ICE);
            }

            switch (random.nextInt(5)) {  // The direction chooser
                case 0 -> nx++;
                case 2 -> nz++;
                case 3 -> nx--;
                case 4 -> nz--;
            }
            y = GenUtils.getTransformedHeight(data.getTerraformWorld(), nx, nz);
        }
    }

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public @NotNull Biome getBiome() {
        return Biome.SNOWY_BEACH;
    }

    @Override
    public Material @NotNull [] getSurfaceCrust(@NotNull Random rand) {
        return new Material[] {
                GenUtils.weightedRandomMaterial(rand, Material.STONE, 35, Material.GRAVEL, 5, Material.COBBLESTONE, 10),
                GenUtils.weightedRandomMaterial(rand, Material.STONE, 35, Material.GRAVEL, 5, Material.COBBLESTONE, 10),
                GenUtils.randChoice(rand, Material.STONE, Material.COBBLESTONE, Material.GRAVEL),
                GenUtils.randChoice(rand, Material.STONE, Material.COBBLESTONE, Material.GRAVEL)
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

        if (GenUtils.chance(random, 7, 100)) {
            makeIceSheet(rawX, surfaceY, rawZ, data, random);
            return;
        }
    }

    @Override
    public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
        // TODO Auto-generated method stub

    }
}
