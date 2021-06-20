package org.terraform.structure.portal;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Stairs;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.SimpleBlock;
import org.terraform.schematic.SchematicParser;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Random;

public class BastionRemnantPortalBigSchematicParser extends SchematicParser {
    private final PopulatorDataAbstract pop;
    private final Random rand;
    private final ArrayList<SimpleBlock> pillarPositions = new ArrayList<>();
    private final ArrayList<SimpleBlock> portalPositions = new ArrayList<>();

    public BastionRemnantPortalBigSchematicParser(PopulatorDataAbstract pop, Random random) {
        this.pop = pop;
        this.rand = random;
    }

    private Material getBricks() {
        return GenUtils.randMaterial(rand,
                Material.CRACKED_POLISHED_BLACKSTONE_BRICKS,
                Material.POLISHED_BLACKSTONE,
                Material.POLISHED_BLACKSTONE_BRICKS,
                Material.POLISHED_BLACKSTONE_BRICKS,
                Material.POLISHED_BLACKSTONE_BRICKS);
    }

    @Override
    public void finalizeSchematic() {
        // Scatter debris around pillars after everything else is done
        for (SimpleBlock pillar : pillarPositions) {
            int debrisCount = rand.nextInt(10);
            for (; debrisCount > 0; debrisCount--) {
                SimpleBlock block = GenUtils.getHighestGround(pop,
                        pillar.getRelative(GenUtils.randInt(rand, -4, 4),
                                0, GenUtils.randInt(rand, -4, 4)));

                Material mat = GenUtils.randMaterial(rand,
                        Material.POLISHED_BLACKSTONE_BUTTON,
                        Material.POLISHED_BLACKSTONE_BUTTON,
                        Material.POLISHED_BLACKSTONE,
                        Material.POLISHED_BLACKSTONE_BRICKS,
                        Material.POLISHED_BLACKSTONE_SLAB,
                        Material.POLISHED_BLACKSTONE_BRICK_SLAB,
                        Material.POLISHED_BLACKSTONE_BRICK_SLAB,
                        Material.SOUL_LANTERN,
                        Material.REDSTONE_WIRE);

                if (block.getType() == Material.OBSIDIAN || block.getType() == Material.CHISELED_POLISHED_BLACKSTONE)
                    continue;
                if (mat == Material.POLISHED_BLACKSTONE_BUTTON) {
                    Directional pebble = (Directional) Material.POLISHED_BLACKSTONE_BUTTON.createBlockData("[face=floor]");
                    block.getRelative(0, 1, 0).lsetBlockData(pebble);
                } else if (mat == Material.LANTERN) {// I'm lazy, place half of these
                    if (rand.nextBoolean()) block.getRelative(0, 1, 0).lsetType(mat);
                } else {
                    block.getRelative(0, 1, 0).lsetType(mat);
                }
            }
        }

        // Damage portal
        boolean destroyedFirst = false;
        for (SimpleBlock pos : portalPositions) {
            pos = GenUtils.getHighestGround(pop, pos);

            if (rand.nextBoolean()) {
                int width = rand.nextInt(4) + 1;
                int height = rand.nextInt(5);
                if (destroyedFirst) width = 5;
                for (int side = 1; side < width; side++) {
                    for (BlockFace face : BlockUtils.directBlockFaces) {
                        pos.getRelative(face, side)
                                .replaceType(Material.AIR, Material.OBSIDIAN, Material.CHISELED_POLISHED_BLACKSTONE);
                    }
                }
                for (int y = 0; y < height; y++) {
                        pos.getRelative(BlockFace.DOWN, y)
                                .replaceType(Material.AIR, Material.OBSIDIAN, Material.CHISELED_POLISHED_BLACKSTONE);
                }
                destroyedFirst = true;
            }
        }
    }

    @Override
    public void applyData(SimpleBlock block, BlockData data) {
        System.out.println(data.getMaterial().name());
        SimpleBlock ground = GenUtils.getHighestGround(pop, block);
        if (data.getMaterial() == Material.GREEN_CONCRETE) {
            if (rand.nextInt(4) != 0)
                ground.setType(getBricks());
        } else if (data.getMaterial() == Material.YELLOW_CONCRETE) {
            ground.setType(getBricks());
        } else if (data.getMaterial() == Material.BLACK_CONCRETE) {
            portalPositions.add(ground);
        }
        // Powders are for generating portal stairs
        else if (data.getMaterial() == Material.GREEN_CONCRETE_POWDER) {
            ground.setType(getBricks());
            ground.getRelative(0, 1, 0).setType(getBricks());
            ground.getRelative(0, 2, 0).setType(Material.POLISHED_BLACKSTONE_BRICK_SLAB);
        } else if (data.getMaterial() == Material.YELLOW_CONCRETE_POWDER) {
            ground.setType(getBricks());
            ground.getRelative(0, 1, 0).setType(getBricks());
        } else if (data.getMaterial() == Material.RED_CONCRETE_POWDER) {
            ground.setType(getBricks());
            ground.getRelative(0, 1, 0).setType(Material.POLISHED_BLACKSTONE_BRICK_SLAB);
        } else if (data.getMaterial() == Material.CHEST) {
            if (rand.nextInt(5) == 0) {
                SimpleBlock chest = ground.getRelative(0 , 1, 0);
                chest.setBlockData(data);
                pop.lootTableChest(chest.getX(), chest.getY(), chest.getZ(), TerraLootTable.RUINED_PORTAL);
            }
        } else if (data.getMaterial() == Material.RED_CONCRETE) {
            // Spawn broken pillars
            int lowest = GenUtils.getLowestGround(pop, ground.getX(), ground.getZ(), ground.getX() + 1, ground.getZ() + 1);
            spawnPillar(ground.getAtY(lowest + 1));
            pillarPositions.add(ground);
        } else {
            super.applyData(block, data);
        }
    }

    private void spawnPillar(SimpleBlock start) {
        int height = GenUtils.randInt(rand, 4, 10);
        int thirdOfHeight = (int) (height * 0.3);
        int highestX = rand.nextInt(2);
        int highestZ = rand.nextInt(2);

        // Base blocks
        SimpleBlock highest = start.getRelative(highestX, 0, highestZ);
        SimpleBlock mid1 = start.getRelative(highestX == 1 ? 0 : 1, 0, highestZ);
        SimpleBlock mid2 = start.getRelative(highestX, 0, highestZ == 1 ? 0 : 1);
        SimpleBlock lowest = start.getRelative(highestX == 1 ? 0 : 1, 0, highestZ == 1 ? 0 : 1);

        // Generate four sides of the pillar
        generateSide(highest, height, BlockUtils.getDirectBlockFace(rand));
        int mid1Height = height - GenUtils.randInt(rand, 1, thirdOfHeight);
        generateSide(mid1, mid1Height, getBlockFace(highest, mid1));
        int mid2Height = height - GenUtils.randInt(rand, 1, thirdOfHeight);
        generateSide(mid2, mid2Height, getBlockFace(highest, mid2));
        int lowestHeight = Math.max(mid2Height, mid1Height) - GenUtils.randInt(rand, 1, thirdOfHeight);
        generateSide(lowest, lowestHeight, getBlockFace(highest, lowest));
    }

    private BlockFace getBlockFace(SimpleBlock highest, SimpleBlock current) {
        ArrayList<BlockFace> faces = new ArrayList<>();
        for (BlockFace face : BlockUtils.directBlockFaces) {
            if (face.getModX() == highest.getX() - current.getX()) faces.add(face);
            if (face.getModZ() == highest.getZ() - current.getZ()) faces.add(face);
        }

        return faces.get(rand.nextInt(faces.size()));
    }

    private void generateSide(SimpleBlock base, int height, BlockFace stairsFace) {
        boolean didPlaceWall = false;
        for (int y = 0; y < height; y++) {
            SimpleBlock b = base.getRelative(BlockFace.UP, y);
            if (didPlaceWall) {
                b.setType(Material.POLISHED_BLACKSTONE_BRICK_WALL);
                continue;
            }
            if (y > height - 0.3 * height && rand.nextBoolean()) {
                didPlaceWall = true;
                b.setType(Material.POLISHED_BLACKSTONE_BRICK_WALL);
                continue;
            } else if (y == height - 1) {
                placeStairs(b, stairsFace);
            } else {
                b.setType(getBricks());
            }
        }
    }

    private void placeStairs(SimpleBlock b, BlockFace face) {
        Stairs stairs = (Stairs) Bukkit.createBlockData(Material.POLISHED_BLACKSTONE_BRICK_STAIRS);
        stairs.setFacing(face);
        b.setBlockData(stairs);
    }
}
