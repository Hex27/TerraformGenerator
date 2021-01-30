package org.terraform.biome.flat;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.Rotatable;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.tree.FractalTreeBuilder;
import org.terraform.tree.FractalTypes;
import org.terraform.tree.MushroomBuilder;
import org.terraform.tree.TreeDB;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.Vector2f;

import java.util.ArrayList;
import java.util.Random;

public class DarkForestHandler extends BiomeHandler {

    private static void spawnRock(Random rand, PopulatorDataAbstract data, int x, int y, int z) {
        ArrayList<int[]> locations = new ArrayList<>();
        locations.add(new int[]{x, y, z});
        locations.add(new int[]{x, y + 2, z});

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

        for (int[] coords : locations) {
            int Tx = coords[0];
            int Ty = coords[1];
            int Tz = coords[2];
            if (!data.getType(Tx, Ty, Tz).isSolid() ||
                    data.getType(Tx, Ty, Tz).toString().contains("LEAVES")) {
                BlockUtils.setDownUntilSolid(Tx, Ty, Tz, data,
                        Material.COBBLESTONE, Material.MOSSY_COBBLESTONE,
                        Material.STONE, Material.CHISELED_STONE_BRICKS,
                        Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS,
                        Material.MOSSY_STONE_BRICKS);
            }
        }
    }

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.DARK_FOREST;
    }

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[]{GenUtils.weightedRandomMaterial(rand, Material.GRASS_BLOCK, 35, Material.COARSE_DIRT, 5),
                Material.DIRT,
                Material.DIRT,
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }

    @Override
    public void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data) {
        Vector2f[] bigTrees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 2 * 16);
        Vector2f[] trees = GenUtils.randomObjectPositions(tw, data.getChunkX(), data.getChunkZ(), 25);

        // Big trees and giant mushrooms
        for (Vector2f tree : bigTrees) {
            int treeY = GenUtils.getHighestGround(data, (int) tree.x, (int) tree.y);

            if (data.getBiome((int) tree.x, (int) tree.y) == getBiome() &&
                    BlockUtils.isDirtLike(data.getType((int) tree.x, treeY, (int) tree.y))) {
                if (GenUtils.chance(random, 1, 10)) {
                    FractalTypes.Mushroom type = FractalTypes.Mushroom.GIANT_RED_MUSHROOM;
                    if (GenUtils.chance(random, 1, 3)) {
                        if (random.nextBoolean())
                            type = FractalTypes.Mushroom.GIANT_BROWN_MUSHROOM;
                        else
                            type = FractalTypes.Mushroom.GIANT_BROWN_FUNNEL_MUSHROOM;
                    }
                    new MushroomBuilder(type).build(tw, data, (int) tree.x, treeY, (int) tree.y);
                } else if (TConfigOption.TREES_DARK_FOREST_BIG_ENABLED.getBoolean()) {
                    TreeDB.spawnBigDarkOakTree(tw, data, (int) tree.x, treeY, (int) tree.y);
                }
            }
        }

        // Small trees
        for (Vector2f tree : trees) {
            int treeY = GenUtils.getHighestGround(data, (int) tree.x, (int) tree.y);

            if (data.getBiome((int) tree.x, (int) tree.y) == getBiome() &&
                    BlockUtils.isDirtLike(data.getType((int) tree.x, treeY, (int) tree.y))) {
                new FractalTreeBuilder(FractalTypes.Tree.DARK_OAK_SMALL)
                        .build(tw, data, (int) tree.x, treeY + 1, (int) tree.y);
            }
        }

        boolean spawnHeads = GenUtils.chance(random, 1, 100);

        //Small decorations
        for (int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for (int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getHighestGround(data, x, z);
                if (data.getType(x, y, z) == Material.GRASS_BLOCK) {
                    if (GenUtils.chance(random, 3, 10)) {
                        if (data.getType(x, y + 1, z) != Material.AIR) continue;
                        //Only grass and mushrooms
                        data.setType(x, y + 1, z, Material.GRASS);
                        if (random.nextBoolean()) {
                            BlockUtils.setDoublePlant(data, x, y + 1, z, Material.TALL_GRASS);
                        } else {
                            Material mushroom = Material.RED_MUSHROOM;
                            if (random.nextBoolean())
                                mushroom = Material.BROWN_MUSHROOM;
                            data.setType(x, y + 1, z, mushroom);
                        }
                    }

                    //Obelisks
                    if (GenUtils.chance(random, 1, 1000)) {
                        if (BlockUtils.isDirtLike(data.getType(x, y, z))) {
                            for (int i = 0; i < GenUtils.randInt(3, 6); i++) {
                                spawnRock(random, data, x, y + i + 1, z);
                            }
                        }
                    }
                }

                if (spawnHeads && GenUtils.chance(random, 1, 50)) {
                    if (BlockUtils.isDirtLike(data.getType(x, y, z))) {
                        Rotatable skull = (Rotatable) Bukkit.createBlockData(Material.PLAYER_HEAD);
                        skull.setRotation(BlockUtils.getXZPlaneBlockFace(random));

                        data.setBlockData(x, y + 1, z, skull);
                    }
                }
            }
        }

    }
}
