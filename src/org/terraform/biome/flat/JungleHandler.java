package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTreeType;
import org.terraform.tree.TreeDB;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class JungleHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.JUNGLE;
    }

//	@Override
//	public int getHeight(int x, int z, Random rand) {
//		SimplexOctaveGenerator gen = new SimplexOctaveGenerator(rand, 2);
//		gen.setScale(0.005);
//
//		return (int) (gen.noise(x, z, 0.5, 0.5)*7D+50D);
//	}

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{GenUtils.weightedRandomMaterial(rand, Material.GRASS_BLOCK, 35, Material.PODZOL, 5),
                Material.DIRT,
                Material.DIRT,
                GenUtils.randMaterial(rand, Material.DIRT, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE, Material.STONE)};
    }

    @Override
    public void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
        FastNoise groundWoodNoise = new FastNoise((int) (tw.getSeed() * 12));
        groundWoodNoise.SetNoiseType(NoiseType.SimplexFractal);
        groundWoodNoise.SetFractalOctaves(3);
        groundWoodNoise.SetFrequency(0.07f);

        FastNoise groundLeavesNoise = new FastNoise((int) (tw.getSeed() * 2));
        groundLeavesNoise.SetNoiseType(NoiseType.SimplexFractal);
        groundLeavesNoise.SetFrequency(0.07f);

        //Most jungle chunks have a big jungle tree
        if (TConfigOption.TREES_JUNGLE_BIG_ENABLED.getBoolean() && GenUtils.chance(random, 6, 10)) {
            int treeX = GenUtils.randInt(random, 2, 12) + data.getChunkX() * 16;
            int treeZ = GenUtils.randInt(random, 2, 12) + data.getChunkZ() * 16;

            if (data.getBiome(treeX, treeZ) == getBiome()) {
                int treeY = GenUtils.getHighestGround(data, treeX, treeZ);
                if (BlockUtils.isDirtLike(data.getType(treeX, treeY, treeZ)))
                    new FractalTreeBuilder(FractalTreeType.JUNGLE_BIG).build(tw, data, treeX, treeY, treeZ);
            }
        }
        // Small jungle trees
        else if (GenUtils.chance(random, 7, 10)) {
            int treeX = GenUtils.randInt(random, 2, 12) + data.getChunkX() * 16;
            int treeZ = GenUtils.randInt(random, 2, 12) + data.getChunkZ() * 16;

            if (data.getBiome(treeX, treeZ) == getBiome()) {
                int treeY = GenUtils.getHighestGround(data, treeX, treeZ);
                if (BlockUtils.isDirtLike(data.getType(treeX, treeY, treeZ)))
                    TreeDB.spawnSmallJungleTree(tw, data, treeX, treeY, treeZ);
            }
        }

        // More small jungle trees
        for (int i = 0; i < GenUtils.randInt(1, 5); i++) {
            int treeX = GenUtils.randInt(random, 0, 15) + data.getChunkX() * 16;
            int treeZ = GenUtils.randInt(random, 0, 15) + data.getChunkZ() * 16;

            if (data.getBiome(treeX, treeZ) == getBiome()) {
                int treeY = GenUtils.getHighestGround(data, treeX, treeZ);
                TreeDB.spawnSmallJungleTree(tw, data, treeX, treeY, treeZ);
            }
        }

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {

                int y = GenUtils.getHighestGround(data, x, z);

                if (data.getBiome(x, z) == getBiome() &&
                        BlockUtils.isDirtLike(data.getType(x, y, z))) {

                    // Generate random wood, or "roots" on the ground
                    if (groundWoodNoise.GetNoise(x, z) > 0.3) {
                        if (GenUtils.chance(random, 99, 100))
                            data.setType(x, y + 1, z, Material.JUNGLE_WOOD);
                    }

                    // Generate some ground leaves
                    float leavesNoiseValue = groundLeavesNoise.GetNoise(x, z);

                    if (leavesNoiseValue > -0.18f) {
                        data.setType(x, y + 1, z, Material.JUNGLE_LEAVES);

                        if (leavesNoiseValue > -0.1f)
                            data.setType(x, y + 2, z, Material.JUNGLE_LEAVES);
                        if (leavesNoiseValue > 0.20f)
                            data.setType(x, y + 3, z, Material.JUNGLE_LEAVES);
                        if (leavesNoiseValue > 0.28f)
                            data.setType(x, y + 4, z, Material.JUNGLE_LEAVES);

                        // Random wood so that leaves don't wither
                        if (leavesNoiseValue > -0.1f && random.nextBoolean()) {
                            data.setType(x, y, z, Material.JUNGLE_LOG);
//                            data.setType(x, y + 1, z, Material.JUNGLE_LEAVES);
                        }
                    }

                    // Generate grass and mushrooms
                    else {
                        if (data.getType(x, y + 1, z).isAir() && random.nextFloat() > 0.35) {
                            if (random.nextBoolean()) {
                                data.setType(x, y + 1, z, GenUtils.weightedRandomMaterial(random, Material.GRASS, 5, BlockUtils.pickFlower(), 1));
                            } else {
                                if (data.getType(x, y + 2, z).isAir())
                                    BlockUtils.setDoublePlant(data, x, y + 1, z, Material.TALL_GRASS);
                            }
                        } else if (data.getType(x, y + 1, z) == Material.JUNGLE_WOOD
                                && data.getType(x, y + 2, z).isAir()
                                && random.nextFloat() > 0.85) {
                            data.setType(x, y + 2, z, GenUtils.randMaterial(Material.RED_MUSHROOM, Material.BROWN_MUSHROOM));
                        }
                    }
                }

            }
        }
    }
}
