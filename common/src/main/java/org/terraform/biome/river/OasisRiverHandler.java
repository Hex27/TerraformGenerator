package org.terraform.biome.river;

import org.bukkit.Material;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.beach.DesertBeachHandler;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

/**
 * This generates lush rivers in desert and badlands.
 * Might have to be replaced if desert or badlands get their own river handler.
 */
public class OasisRiverHandler extends RiverHandler {
    /**
     * @return true if (x, z) is inside oasis
     */
    public static boolean isLushRiver(TerraformWorld tw, int x, int z) {
        double lushRiverNoiseValue = DesertBeachHandler.getLushNoise(tw, x, z);
        double riverDepth = HeightMap.getRawRiverDepth(tw, x, z);
        BiomeBank biome = BiomeBank.calculateHeightIndependentBiome(tw, x, z);

        return lushRiverNoiseValue > DesertBeachHandler.lushThreshold &&
                riverDepth > 0 &&
                (biome == BiomeBank.DESERT ||
                        biome == BiomeBank.BADLANDS);
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {
        super.populateSmallItems(world, random, data);
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int riverBottom = GenUtils.getHighestGround(data, x, z);

                if (riverBottom > TerraformGenerator.seaLevel || !isLushRiver(world, x, z)) continue;

                // Lily pads
                JungleRiverHandler.generateLilyPad(world, random, data, x, z, riverBottom);

                // Kelp and sea grass
                if (random.nextInt(7) == 0) {
                    JungleRiverHandler.generateKelp(x, riverBottom + 1, z, data, random);
                } else if (random.nextInt(5) == 0) {
                    if (random.nextBoolean()) {
                        data.setType(x, riverBottom + 1, z, Material.SEAGRASS);
                    } else if (riverBottom + 1 < TerraformGenerator.seaLevel){
                        BlockUtils.setDoublePlant(data, x, riverBottom + 1, z, Material.TALL_SEAGRASS);
                    }
                }
            }
        }
    }
}
