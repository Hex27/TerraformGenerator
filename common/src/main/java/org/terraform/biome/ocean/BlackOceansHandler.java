package org.terraform.biome.ocean;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.util.Vector;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.HeightMap;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class BlackOceansHandler extends BiomeHandler {

    public static void genSpike(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z, int baseRadius, int height) {
        y -= height / 5;
        //Vector one to two;
        Vector base = new Vector(x, y, z);
        Vector base2 = new Vector(x + GenUtils.randInt(random, -2 * baseRadius, 2 * baseRadius), y + height, z + GenUtils.randInt(random, -2 * baseRadius, 2 * baseRadius));
        Vector v = base2.subtract(base);

        int segments = height;
        SimpleBlock one = new SimpleBlock(data, x, y, z);
        double radius = baseRadius;
        for (int i = 0; i <= segments; i++) {
            Vector seg = v.clone().multiply((float) i / ((float) segments));
            SimpleBlock segment = one.getRelative(seg);
//			segment.setHardReplace();
//			segment.setType(type);
            BlockUtils.replaceSphere(
                    (int) (tw.getSeed() * 12),
                    (float) radius, 2, (float) radius,
                    segment, false, false,
                    Material.STONE,
                    Material.STONE,
                    Material.STONE,
                    Material.STONE,
                    Material.STONE,
                    Material.STONE,
                    Material.STONE,
                    Material.STONE,
                    Material.STONE,
                    Material.STONE,
                    Material.STONE,
                    Material.STONE,
                    Material.STONE,
                    Material.STONE,
                    Material.STONE,
                    Material.STONE,
                    Material.STONE,
                    Material.OBSIDIAN,
                    Material.OBSIDIAN,
                    Material.OBSIDIAN,
                    Material.IRON_ORE);
//			Block segment = one.getLocation().add(seg).getBlock();
//			segment.setType(type);
            radius = ((double) baseRadius) * (1 - ((double) i) / ((double) segments));
        }
    }

    @Override
    public boolean isOcean() {
        return true;
    }

    @Override
    public Biome getBiome() {
        return Biome.COLD_OCEAN;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{GenUtils.randMaterial(rand, Material.DIRT, Material.STONE, Material.COBBLESTONE, Material.STONE, Material.GRAVEL, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE, Material.STONE, Material.STONE, Material.GRAVEL, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE, Material.GRAVEL, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }

    @Override
    public void populateSmallItems(TerraformWorld world, Random random, PopulatorDataAbstract data) {
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int coreHeight = HeightMap.getBlockHeight(world, x, z);
                if (data.getBiome(x, coreHeight + 1, z) != getBiome()) continue;

                int y = GenUtils.getTrueHighestBlock(data, x, z);
                if (!BlockUtils.isStoneLike(data.getType(x, y, z))) continue;
                if (GenUtils.chance(random, 1, 80)) { //SEA GRASS/KELP
                    CoralGenerator.generateKelpGrowth(data, x, y + 1, z);
                }
            }
        }
    }

	@Override
	public void populateLargeItems(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int coreHeight = HeightMap.getBlockHeight(tw, x, z);
                if (data.getBiome(x, coreHeight + 1, z) != getBiome()) continue;
                //black spike
                if (GenUtils.chance(random, 1, 200)) {
                    genSpike(tw, random, data, x, coreHeight, z,
                            GenUtils.randInt(5, 15), //radius
                            GenUtils.randInt(50, 100));
                }
            }
        }
	}
}
