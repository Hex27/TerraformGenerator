package org.terraform.biome.flat;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.FastNoise;
import org.terraform.utils.FastNoise.NoiseType;
import org.terraform.utils.GenUtils;
import org.terraform.utils.Vector2f;

import java.util.ArrayList;
import java.util.Random;

public class ForestHandler extends BiomeHandler {

    private static void spawnRock(Random rand, PopulatorDataAbstract data, int x, int y, int z) {
        ArrayList<int[]> locations = new ArrayList<>(20);
        locations.add(new int[]{x, y, z});

        locations.add(new int[]{x, y + 1, z});
        locations.add(new int[]{x + 1, y + 1, z});
        locations.add(new int[]{x - 1, y + 1, z});
        locations.add(new int[]{x, y + 1, z + 1});
        locations.add(new int[]{x, y + 1, z - 1});

        locations.add(new int[]{x + 1, y, z});
        locations.add(new int[]{x - 1, y, z});
        locations.add(new int[]{x, y, z + 1});
        locations.add(new int[]{x, y, z - 1});
        locations.add(new int[]{x + 1, y, z});
        locations.add(new int[]{x - 1, y, z + 1});
        locations.add(new int[]{x + 1, y, z + 1});
        locations.add(new int[]{x - 1, y, z - 1});

        locations.add(new int[]{x, y - 1, z});
        locations.add(new int[]{x + 1, y - 1, z});
        locations.add(new int[]{x - 1, y - 1, z});
        locations.add(new int[]{x, y - 1, z + 1});
        locations.add(new int[]{x, y - 1, z - 1});

        for (int[] coords : locations) {
            int Tx = coords[0];
            int Ty = coords[1];
            int Tz = coords[2];
            if (!data.getType(Tx, Ty, Tz).isSolid() ||
                    data.getType(Tx, Ty, Tz).toString().contains("LEAVES")) {
                data.setType(Tx, Ty, Tz, GenUtils.randMaterial(rand,
                        Material.COBBLESTONE, Material.STONE, Material.MOSSY_COBBLESTONE));
            }
        }
    }

    @Override
    public boolean isOcean() {
        return false;
    }

//	@Override
//	public int getHeight(int x, int z, Random rand) {
//		SimplexOctaveGenerator gen = new SimplexOctaveGenerator(rand, 2);
//		gen.setScale(0.005);
//		
//		return (int) (gen.noise(x, z, 0.5, 0.5)*7D+50D);
//	}

    @Override
    public Biome getBiome() {
        return Biome.FOREST;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{GenUtils.weightedRandomMaterial(rand, Material.GRASS_BLOCK, 35, Material.PODZOL, 3),
                Material.DIRT,
                Material.DIRT,
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }

    @Override
    public void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data) {

        FastNoise pathNoise = new FastNoise((int) (tw.getSeed() * 12));
        pathNoise.SetNoiseType(NoiseType.SimplexFractal);
        pathNoise.SetFractalOctaves(3);
        pathNoise.SetFrequency(0.07f);

        //Most forest chunks have a big tree
        if (TConfigOption.TREES_FOREST_BIG_ENABLED.getBoolean() && GenUtils.chance(random, 6, 10)) {
            int treeX = GenUtils.randInt(random, 2, 12) + data.getChunkX() * 16;
            int treeZ = GenUtils.randInt(random, 2, 12) + data.getChunkZ() * 16;
            if (data.getBiome(treeX, treeZ) == getBiome()) {
                int treeY = GenUtils.getHighestGround(data, treeX, treeZ);

                if (BlockUtils.isDirtLike(data.getType(treeX, treeY, treeZ)))
                    new FractalTreeBuilder(FractalTypes.Tree.FOREST).build(tw, data, treeX, treeY, treeZ);
            }
        } else {
            // TODO Clearing
        }

        // Most forest chunks have a big tree
        ArrayList<Vector2f> trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 8);

        for (Vector2f tree : trees) {
            int treeY = GenUtils.getHighestGround(data, (int) tree.x, (int) tree.y);

            if(data.getBiome((int) tree.x, (int) tree.y) == getBiome() &&
                    BlockUtils.isDirtLike(data.getType((int) tree.x, treeY, (int) tree.y))) {
                new FractalTreeBuilder(FractalTypes.Tree.NORMAL_SMALL).build(tw, data, (int) tree.x, treeY, (int) tree.y);
            }
        }

        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getHighestGround(data, x, z);
                if (pathNoise.GetNoise(x, z) > 0.3) {
                    if (GenUtils.chance(random, 99, 100) &&
                            data.getBiome(x, z) == getBiome() &&
                            BlockUtils.isDirtLike(data.getType(x, y, z)))
                        data.setType(x, y, z, Material.GRASS_PATH);
                }
                if (data.getType(x, y, z) == Material.GRASS_BLOCK) {
                    if (GenUtils.chance(random, 3, 10)) {
                        if (data.getType(x, y + 1, z) != Material.AIR) continue;
                        //Grass & Flowers
                        data.setType(x, y + 1, z, Material.GRASS);
                        if (random.nextBoolean()) {
                            BlockUtils.setDoublePlant(data, x, y + 1, z, Material.TALL_GRASS);
                        } else {
                            data.setType(x, y + 1, z, BlockUtils.pickFlower());
                        }
                    }
                    if (GenUtils.chance(random, 1, 95)) {

                        if (BlockUtils.isDirtLike(data.getType(x, y, z)) ||
                                data.getType(x, y, z) == Material.COBBLESTONE ||
                                data.getType(x, y, z) == Material.MOSSY_COBBLESTONE ||
                                data.getType(x, y, z) == Material.STONE) {
                            int ny = GenUtils.randInt(random, -1, 1);
                            spawnRock(random, data, x, y + ny, z);
                            if (GenUtils.chance(random, 1, 3))
                                spawnRock(random, data, GenUtils.randInt(random, -1, 1) + x, y + ny + 1, z + GenUtils.randInt(random, -1, 1));
                        }
                    }
                }
            }
        }

    }
}
