package org.terraform.structure.mineshaft;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rail;
import org.bukkit.block.data.Rail.Shape;
import org.bukkit.block.data.type.Lantern;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.IPopulatorDataMinecartSpawner;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.structure.room.PathPopulatorAbstract;
import org.terraform.structure.room.PathPopulatorData;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class MineshaftPathPopulator extends PathPopulatorAbstract {
    private final Random rand;

    public MineshaftPathPopulator(Random rand) {
        this.rand = rand;
    }

    /**
     * @Deprecated Kept for legacy reasons for BadlandsMine.
     * Does not actually get used anywhere else.
     */
    @Deprecated
    @Override
    public boolean customCarve(@NotNull SimpleBlock base, BlockFace dir, int pathWidth) {
        Wall core = new Wall(base.getUp(), dir);
        int seed = 55 + core.getX() + core.getY() ^ 2 + core.getZ() ^ 3;
        BlockUtils.carveCaveAir(seed,
                pathWidth,
                pathWidth + 1,
                pathWidth,
                core.get(),
                false,
                BlockUtils.caveCarveReplace
        );

        return true;
    }

    @Override
    public void populate(@NotNull PathPopulatorData ppd) {
        Wall core = new Wall(ppd.base, ppd.dir);

        if (ppd.dir == BlockFace.UP) {
            // Make a platform
            for (int nx = -1; nx <= 1; nx++) {
                for (int nz = -1; nz <= 1; nz++) {
                    core.getRelative(nx, 0, nz).setType(getPathMaterial());
                }
            }
            return;
        }
        // God this sucks
        legacyPopulate(core.getRear());
        legacyPopulate(core);
        legacyPopulate(core.getFront());
    }

    public void legacyPopulate(@NotNull Wall core) {
        // what the fuck is wrong with you

        // Was populated before.
        if (core.getType() != Material.CAVE_AIR) {
            return;
        }

        Wall ceiling = core.findCeiling(10);
        if (ceiling != null) {
            ceiling = ceiling.getDown();
        }
        Wall left = core.findLeft(10);
        Wall right = core.findRight(10);

        // Central Pathway
        core.setType(GenUtils.randChoice(Material.COBBLESTONE,
                Material.COBBLESTONE,
                Material.COBBLESTONE,
                Material.ANDESITE,
                Material.DIORITE,
                Material.MOSSY_COBBLESTONE
        ));
        core.getRight().setType(GenUtils.randChoice(getPathMaterial()));
        core.getLeft().setType(GenUtils.randChoice(getPathMaterial()));

        // Pillars supporting the mineshaft area
        if (GenUtils.chance(rand, 1, 5)) {
            core.getDown().getRight().downUntilSolid(rand, getFenceMaterial());
            core.getDown().getLeft().downUntilSolid(rand, getFenceMaterial());
        }

        // Broken feel
        if (rand.nextBoolean()) {
            core.getRight(2).setType(GenUtils.randChoice(getPathMaterial()));
        }
        if (rand.nextBoolean()) {
            core.getLeft(2).setType(GenUtils.randChoice(getPathMaterial()));
        }

        // Decorate with pebbles n shit lol
        for (int i = -2; i <= 2; i++) {
            if (i == 0) {
                continue;
            }
            Wall target = core.getLeft(i);
            // Checks if there's space on the ground
            if (!Tag.SLABS.isTagged(target.getType())
                && target.isSolid()
                && target.getType() != Material.GRAVEL
                && target.getUp().getType() == Material.CAVE_AIR)
            {
                if (GenUtils.chance(1, 10)) { // Pebble
                    Directional pebble = (Directional) Material.STONE_BUTTON.createBlockData("[face=floor]");
                    pebble.setFacing(BlockUtils.getDirectBlockFace(rand));
                    target.getUp().setBlockData(pebble);
                }
                else if (GenUtils.chance(1, 10)) { // Mushroom
                    PlantBuilder.build(target.getUp(), PlantBuilder.BROWN_MUSHROOM, PlantBuilder.RED_MUSHROOM);
                }
            }
        }

        // Rails
        if (TConfig.areDecorationsEnabled() && core.isSolid() && rand.nextBoolean()) {
            Rail rail = (Rail) Bukkit.createBlockData(Material.RAIL);
            switch (core.getDirection()) {
                case NORTH, SOUTH -> rail.setShape(Shape.NORTH_SOUTH);
                case EAST, WEST -> rail.setShape(Shape.EAST_WEST);
            }

            // Check if rails are wet.
            if (BlockUtils.isWet(core.getUp().get())) {
                rail.setWaterlogged(true);
            }

            core.getUp().setBlockData(rail);
            // BlockUtils.correctSurroundingRails(core.getUp().get());
            if (GenUtils.chance(rand, 1, 100)) {
                TerraformGeneratorPlugin.logger.info("Minecart with chest at: "
                                                     + core.getX()
                                                     + ", "
                                                     + core.getY()
                                                     + ", "
                                                     + core.getZ());
                IPopulatorDataMinecartSpawner ms = (IPopulatorDataMinecartSpawner) core.get().getPopData();
                ms.spawnMinecartWithChest(core.getX(),
                        core.getY() + 1,
                        core.getZ(),
                        TerraLootTable.ABANDONED_MINESHAFT,
                        rand
                );
            }
        }

        boolean hasSupports = setMineshaftSupport(left, right, ceiling);

        if (hasSupports) {
            return;
        }

        // Now for the stuff that we've put in normal caves
        for (int i = -2; i <= 2; i++) {
            Wall ceil = core.getLeft(i).findCeiling(10);
            Wall floor = core.getLeft(i).findFloor(10);

            // Decorations on the wall
            if (ceil != null && floor != null) {
                for (int ny = 0; ny <= ceil.getY() - floor.getY(); ny++) {
                    Wall[] walls = {
                            floor.getRelative(0, ny, 0).findLeft(10), floor.getRelative(0, ny, 0).findRight(10)
                    };
                    for (Wall target : walls) {
                        if (target != null) {
                            if (target.getType() == Material.STONE) {
                                if (GenUtils.chance(1, 10)) {
                                    target.setType(GenUtils.randChoice(Material.COBBLESTONE,
                                            Material.MOSSY_COBBLESTONE
                                    ));
                                }
                                if (GenUtils.chance(1, 10)) {
                                    BlockUtils.vineUp(target.get(), 2);
                                }
                            }
                        }
                    }
                }
            }

            // Vertical decorations
            if (ceil != null && !Tag.SLABS.isTagged(ceil.getType()) && !Tag.LOGS.isTagged(ceil.getType())) {
                ceil = ceil.getDown();
                if (GenUtils.chance(rand, 1, 10)) {
                    // Stalactites
                    boolean canSpawn = true;
                    for (BlockFace face : BlockUtils.directBlockFaces) {
                        if (Tag.WALLS.isTagged(ceil.getRelative(face).getType())) {
                            canSpawn = false;
                            break;
                        }
                    }
                    if (canSpawn) {
                        ceil.downLPillar(rand,
                                GenUtils.randInt(rand, 1, 3),
                                Material.COBBLESTONE_WALL,
                                Material.MOSSY_COBBLESTONE_WALL
                        );
                    }

                }
                else if (GenUtils.chance(rand, 1, 6)) {
                    // Cobweb
                    if (TConfig.areDecorationsEnabled()) {
                        ceil.setType(Material.COBWEB);
                    }
                }
                else if (GenUtils.chance(rand, 1, 10)) {
                    // Slabbing
                    Slab slab = (Slab) Bukkit.createBlockData(GenUtils.randChoice(
                            Material.COBBLESTONE_SLAB,
                            Material.STONE_SLAB,
                            Material.MOSSY_COBBLESTONE_SLAB
                    ));
                    slab.setType(Type.TOP);
                    ceil.setBlockData(slab);
                }
            }
            if (floor != null && !Tag.SLABS.isTagged(floor.getType()) && !Tag.LOGS.isTagged(floor.getType())) {
                floor = floor.getUp();
                if (GenUtils.chance(rand, 1, 10)) {
                    // Stalagmites
                    boolean canSpawn = true;
                    for (BlockFace face : BlockUtils.directBlockFaces) {
                        if (Tag.WALLS.isTagged(floor.getRelative(face).getType())) {
                            canSpawn = false;
                            break;
                        }
                    }
                    if (canSpawn) {
                        floor.LPillar(GenUtils.randInt(rand, 1, 3),
                                false,
                                rand,
                                Material.COBBLESTONE_WALL,
                                Material.MOSSY_COBBLESTONE_WALL
                        );
                    }

                }
                else if (GenUtils.chance(rand, 1, 10)) {
                    // Slabbing
                    for (BlockFace face : BlockUtils.directBlockFaces) {
                        if (floor.getRelative(face).isSolid()) {
                            Slab slab = (Slab) Bukkit.createBlockData(GenUtils.randChoice(
                                    Material.COBBLESTONE_SLAB,
                                    Material.STONE_SLAB,
                                    Material.MOSSY_COBBLESTONE_SLAB
                            ));
                            slab.setType(Type.BOTTOM);
                            floor.setBlockData(slab);
                            break;
                        }
                    }

                }
                else if (GenUtils.chance(1, 15)) { // Mushroom
                    PlantBuilder.build(floor, PlantBuilder.BROWN_MUSHROOM, PlantBuilder.RED_MUSHROOM);
                }

            }
        }
    }

    public boolean setMineshaftSupport(@Nullable Wall left, @Nullable Wall right, @Nullable Wall ceil) {
        if (!TConfig.areDecorationsEnabled()) {
            return true;
        }

        if (left == null || right == null) {
            return false; // Lol wtf is this situation even
        }

        // Check interval
        if (left.getDirection().getModX() != 0) {
            if (left.getX() % 5 != 0) {
                return false;
            }
        }
        else if (left.getDirection().getModZ() != 0) {
            if (left.getZ() % 5 != 0) {
                return false;
            }
        }

        // Check if the support distance is too small
        left = left.getRight();
        right = right.getLeft();

        // At least distance of 3
        int dist = (int) left.get().toVector().distance(right.get().toVector());
        if (dist >= 3) {
            if (left.LPillar(10, false, rand, Material.BARRIER) != 10) {
                left.LPillar(10, false, rand, getFenceMaterial());
                placeSupportFences(left.getDown());// .downUntilSolid(rand, getFenceMaterial());
            }
            if (right.LPillar(10, false, rand, Material.BARRIER) != 10) {
                right.LPillar(10, false, rand, getFenceMaterial());
                placeSupportFences(right.getDown());// .downUntilSolid(rand, getFenceMaterial());
            }


            // Support
            if (ceil != null) {
                Orientable log = (Orientable) Bukkit.createBlockData(getSupportMaterial());
                if (left.getDirection().getModX() != 0) {
                    log.setAxis(Axis.Z);
                }
                if (left.getDirection().getModZ() != 0) {
                    log.setAxis(Axis.X);
                }
                ceil = left.clone().getRelative(0, ceil.getY() - left.getY(), 0).getLeft();

                Lantern lantern = (Lantern) Bukkit.createBlockData(Material.LANTERN);
                lantern.setHanging(true);

                for (int i = 0; i < dist + 2; i++) {
                    Wall support = ceil.getRight(i);
                    if (!support.isSolid() || support.getType() == getFenceMaterial()) {
                        if (support.getUp().getType() != getSupportMaterial()
                            && support.getDown().getType() != getSupportMaterial())
                        {
                            support.setBlockData(log);

                            // L A M P
                            if (GenUtils.chance(rand, 1, 100)) {
                                support.getDown().get().lsetBlockData(lantern);
                            }

                            // Vine
                            if (GenUtils.chance(rand, 1, 10)) {
                                BlockUtils.vineUp(support.get(), 3);
                            }
                        }
                    }
                }
            }


        }
        return true;
    }

    private void placeSupportFences(@NotNull Wall w) {
        while (!w.isSolid()) {
            if (w.getType() == Material.LAVA) {
                w.setType(Material.COBBLESTONE);
            }
            else {
                w.setType(getFenceMaterial());
            }
            w = w.getDown();
        }
    }

    public Material @NotNull [] getPathMaterial() {
        return new Material[] {
                Material.OAK_PLANKS, Material.OAK_SLAB, Material.OAK_PLANKS, Material.OAK_SLAB, Material.GRAVEL
        };
    }

    public @NotNull Material getFenceMaterial() {
        return Material.OAK_FENCE;
    }

    public @NotNull Material getSupportMaterial() {
        return Material.OAK_LOG;
    }
}
