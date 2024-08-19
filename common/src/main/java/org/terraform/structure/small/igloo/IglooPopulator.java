package org.terraform.structure.small.igloo;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Lantern;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeClimate;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.structure.MultiMegaChunkStructurePopulator;
import org.terraform.utils.*;
import org.terraform.utils.SphereBuilder.SphereType;
import org.terraform.utils.blockdata.*;

import java.util.EnumSet;
import java.util.Random;

public class IglooPopulator extends MultiMegaChunkStructurePopulator {

    @Override
    public void populate(@NotNull TerraformWorld tw, @NotNull PopulatorDataAbstract data) {
        if (!isEnabled()) {
            return;
        }

        Random random = this.getHashedRandom(tw, data.getChunkX(), data.getChunkZ());
        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());
        for (int[] coords : getCoordsFromMegaChunk(tw, mc)) {
            int x = coords[0];
            int z = coords[1];
            if (x >> 4 != data.getChunkX() || z >> 4 != data.getChunkZ()) {
                continue;
            }
            int height = GenUtils.getHighestGround(data, x, z);

            spawnIgloo(tw, random, data, x, height + 1, z);
        }
    }

    public void spawnIgloo(TerraformWorld tw,
                           @NotNull Random random,
                           @NotNull PopulatorDataAbstract data,
                           int x,
                           int y,
                           int z)
    {
        Wall core = new Wall(data, x, y, z, BlockUtils.getDirectBlockFace(random));

        int size = GenUtils.randInt(random, 4, 7);
        TerraformGeneratorPlugin.logger.info("Placing igloo of size " + size);
        new CylinderBuilder(random, core.getDown(), Material.SPRUCE_PLANKS).setHardReplace(false)
                                                                           .setRX(size * 1.5f)
                                                                           .setRY(0.5f)
                                                                           .setRZ(size * 1.5f)
                                                                           .setMinRadius(1f)
                                                                           .setSingleBlockY(true)
                                                                           .build();

        //
        new SphereBuilder(random, core, Material.SNOW_BLOCK).setSphereType(SphereType.UPPER_SEMISPHERE)
                                                            .setRadius(size)
                                                            .setSmooth(true)
                                                            .build();
        new SphereBuilder(random, core, Material.AIR).setSphereType(SphereType.UPPER_SEMISPHERE)
                                                     .setRadius(size - 1)
                                                     .setSmooth(true)
                                                     .setHardReplace(true)
                                                     .build();

        spawnSpire(core.getRelative(size - 1, 0, size - 1));
        spawnSpire(core.getRelative(-size + 1, 0, size - 1));
        spawnSpire(core.getRelative(-size + 1, 0, -size + 1));
        spawnSpire(core.getRelative(size - 1, 0, -size + 1));

        core.getUp(size + 1).setType(Material.SPRUCE_SLAB);

        // Side Decorations
        spawnTrapdoorDecors(new Wall(core.getUp(size), BlockFace.NORTH), size);
        spawnTrapdoorDecors(new Wall(core.getUp(size), BlockFace.SOUTH), size);
        spawnTrapdoorDecors(new Wall(core.getUp(size), BlockFace.EAST), size);
        spawnTrapdoorDecors(new Wall(core.getUp(size), BlockFace.WEST), size);

        // Entrance
        core.getFront(size + 1).getUp().setType(Material.AIR);
        core.getFront(size + 1).setType(Material.AIR);
        core.getFront(size).setType(Material.AIR);
        core.getFront(size).getUp().setType(Material.AIR);

        core.getFront(size - 1).setType(Material.AIR);
        core.getFront(size - 1).getUp().setType(Material.AIR);
        BlockUtils.placeDoor(data, Material.SPRUCE_DOOR, core.getFront(size - 1));

        Wall entranceCore = core.getFront(size);
        entranceCore.getLeft().Pillar(2, Material.SPRUCE_LOG);
        entranceCore.getRight().Pillar(2, Material.SPRUCE_LOG);
        entranceCore.getFront().getLeft().setType(Material.SPRUCE_PLANKS);
        entranceCore.getFront().getRight().setType(Material.SPRUCE_PLANKS);

        new OrientableBuilder(Material.SPRUCE_LOG).setAxis(BlockUtils.getAxisFromBlockFace(core.getDirection()))
                                                  .apply(entranceCore.getUp(2));

        new StairBuilder(Material.SPRUCE_STAIRS).setFacing(core.getDirection().getOppositeFace())
                                                .apply(entranceCore.getFront().getLeft().getUp())
                                                .apply(entranceCore.getFront().getRight().getUp())
                                                .setFacing(BlockUtils.getLeft(core.getDirection()))
                                                .apply(entranceCore.getRight().getUp(2))
                                                .setFacing(BlockUtils.getRight(core.getDirection()))
                                                .apply(entranceCore.getLeft().getUp(2));

        new TrapdoorBuilder(Material.SPRUCE_TRAPDOOR).setOpen(true)
                                                     .setFacing(BlockUtils.getLeft(core.getDirection()))
                                                     .apply(entranceCore.getLeft(2))
                                                     .apply(entranceCore.getLeft(2).getUp())
                                                     .setFacing(BlockUtils.getRight(core.getDirection()))
                                                     .apply(entranceCore.getRight(2))
                                                     .apply(entranceCore.getRight(2).getUp());

        // Stairway out the entrance.

        if (entranceCore.getFront(2).isSolid()) {
            new StairwayBuilder(Material.COBBLESTONE_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS).setAngled(true)
                                                                                               .setStopAtWater(true)
                                                                                               .setStairwayDirection(
                                                                                                       BlockFace.UP)
                                                                                               .build(entranceCore.getFront(
                                                                                                       4));
            entranceCore.getFront(2).Pillar(2, new Random(), Material.AIR);
            entranceCore.getFront(3).Pillar(2, new Random(), Material.AIR);
        }
        else {
            new StairwayBuilder(Material.COBBLESTONE_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS).setAngled(true)
                                                                                               .setStopAtWater(true)
                                                                                               .build(entranceCore.getFront(
                                                                                                       2).getDown());
        }

        // By this point, the entire exterior of the igloo has been placed.
        // The below area handles interior placement

        // Pick a random corner to place a chimney
        int offset = size / 2;
        BlockFace offsetDir = BlockUtils.xzDiagonalPlaneBlockFaces[random.nextInt(BlockUtils.xzDiagonalPlaneBlockFaces.length)];
        SimpleBlock chimneyCore = core.getRelative(offsetDir, offset);
        chimneyCore.getDown().setType(Material.HAY_BLOCK);
        chimneyCore.setType(Material.CAMPFIRE);

        for (BlockFace face : BlockUtils.xzPlaneBlockFaces) {
            if (face.getModX() == offsetDir.getModX() || face.getModZ() == offsetDir.getModZ()) {
                for (int depth = 1; depth <= 2; depth++) {
                    if (chimneyCore.getRelative(face, depth).distanceSquared(core) < size * size) {
                        chimneyCore.getRelative(face, depth)
                                   .LPillar(size, new Random(), Material.STONE, Material.COBBLESTONE);
                    }
                }
            }
        }

        // dril chimney
        chimneyCore.getUp().Pillar(size + 10, Material.AIR);

        // Create the actual chimney (drill air and make the chimney out of trapdoor)
        for (BlockFace face : BlockUtils.directBlockFaces) {
            for (int ry = size + 1; ry > 0; ry--) {
                SimpleBlock target = chimneyCore.getUp(ry).getRelative(face);
                if (target.getDown().getType() == Material.SNOW_BLOCK) {
                    new StairBuilder(Material.COBBLESTONE_STAIRS).setFacing(face.getOppositeFace()).apply(target);
                    break;
                }
                else {
                    new TrapdoorBuilder(Material.SPRUCE_TRAPDOOR).setFacing(face).setOpen(true).apply(target);
                }
            }
        }

        // On all four corners except the entrance, place some stuff
        for (BlockFace face : BlockUtils.directBlockFaces) {
            if (face == core.getDirection()) {
                continue;
            }

            // find the igloo wall. It will be spruce log, so get the one in front of it.
            Wall wall = new Wall(core, face.getOppositeFace());

            int threshold = size + 1;
            boolean found = false;
            while (threshold >= 0) {
                if (wall.getType() == Material.SPRUCE_LOG) {
                    found = true;
                    break;
                }
                wall = wall.getRear();
                threshold--;
            }
            // Only operate on the wall if a wall is found.
            if (found) {
                wall = wall.getFront(2);
                for (BlockFace side : BlockUtils.getAdjacentFaces(wall.getDirection())) {
                    Wall decoCore = wall.getRelative(side);
                    // That's the chimney
                    if (BlockUtils.isStoneLike(decoCore.getType()) || BlockUtils.isStoneLike(decoCore.getRelative(side)
                                                                                                     .getType()))
                    {
                        continue;
                    }

                    switch (random.nextInt(6)) {
                        case 0: // Bed
                        case 1:
                            BlockUtils.placeBed(decoCore, BlockUtils.pickBed(), decoCore.getDirection());
                            decoCore.getRelative(side).lsetType(Material.SPRUCE_LOG);
                            decoCore.getRelative(side).getUp().lsetType(Material.POTTED_SPRUCE_SAPLING);
                            break;
                        case 2:
                        case 3: // Solid interactable blocks and tables
                            for (int i = 0; i < 5; i++) {
                                if (decoCore.getRelative(side, i).isSolid()) {
                                    break;
                                }

                                switch (random.nextInt(3)) {
                                    case 0: // Directional deco
                                        new DirectionalBuilder(
                                                Material.FURNACE,
                                                Material.BLAST_FURNACE,
                                                Material.SMOKER,
                                                Material.ANVIL
                                        ).setFacing(decoCore.getDirection()).apply(decoCore.getRelative(side, i));
                                        break;
                                    case 1: // Static deco
                                        decoCore.getRelative(side, i)
                                                .setType(Material.CRAFTING_TABLE, Material.FLETCHING_TABLE);
                                        break;
                                    default: // Table
                                        new SlabBuilder(
                                                Material.SPRUCE_SLAB,
                                                Material.DIORITE_SLAB,
                                                Material.ANDESITE_SLAB,
                                                Material.COBBLESTONE_SLAB
                                        ).setType(Slab.Type.TOP).apply(decoCore.getRelative(side, i));
                                        break;

                                }

                                // Place stuff on top of whatever was placed.
                                decoCore.getRelative(side, i).getUp().setType(Material.TURTLE_EGG,
                                        Material.AIR,
                                        Material.AIR,
                                        Material.AIR,
                                        Material.AIR,
                                        Material.AIR,
                                        Material.AIR,
                                        Material.TORCH,
                                        Material.TORCH,
                                        Material.LANTERN,
                                        Material.LANTERN,
                                        Material.POTTED_SPRUCE_SAPLING,
                                        Material.POTTED_POPPY,
                                        Material.POTTED_FERN
                                );
                            }
                            break;
                        default: // Barrels of stuff
                            for (int i = 0; i < 5; i++) {
                                if (decoCore.getRelative(side, i).isSolid()) {
                                    break;
                                }
                                if (random.nextBoolean()) {
                                    continue;
                                }
                                new BarrelBuilder().setFacing(BlockUtils.getSixBlockFace(random))
                                                   .setLootTable(TerraLootTable.IGLOO_CHEST)
                                                   .apply(decoCore.getRelative(side, i));
                            }
                            break;
                    }
                }
            }
        }

        // Place carpet on the ground in the middle.
        core.setType(Material.RED_CARPET);

        // Pick a color and set the larger radius.
        Material carpet = BlockUtils.pickCarpet();
        for (BlockFace face : BlockUtils.xzPlaneBlockFaces) {
            core.getRelative(face).lsetType(carpet);
        }
        if (size > 5) {
            for (BlockFace face : BlockUtils.directBlockFaces) {
                core.getRelative(face, 2).setType(carpet);
            }
        }

        // Spawn a villager.
        core.getUp().addEntity(EntityType.VILLAGER);
    }

    private void spawnTrapdoorDecors(@NotNull Wall w, int size) {
        int lowest = 9999;
        for (int i = 1; i < size; i++) {
            Wall target = w.getFront(i);
            if (i <= 2) {
                target.setType(Material.SNOW_BLOCK);
                if (i == 1) {
                    target.getLeft().setType(Material.SNOW_BLOCK);
                    target.getRight().setType(Material.SNOW_BLOCK);
                }
                new TrapdoorBuilder(Material.SPRUCE_TRAPDOOR).setFacing(w.getDirection()).apply(target.getUp());
                continue;
            }

            target = target.getDown(i - 3);
            if (!target.isSolid()) {
                new StairBuilder(Material.SPRUCE_STAIRS).setFacing(target.getDirection().getOppositeFace())
                                                        .apply(target);
                target.getDown().lsetType(Material.SNOW_BLOCK);
            }
            if (target.getY() < lowest) {
                lowest = target.getY();
            }
        }

        int y = w.getDown(size).getY();

        while (y <= lowest) {
            Wall target = w.getFront(size).getAtY(y);
            new TrapdoorBuilder(Material.SPRUCE_TRAPDOOR).setOpen(true).setFacing(w.getDirection()).lapply(target);
            if (target.getType() == Material.SNOW_BLOCK) {
                target.setType(Material.SPRUCE_LOG);
                target.getUp().setType(Material.SPRUCE_LOG);

                Lantern lantern = (Lantern) Bukkit.createBlockData(Material.LANTERN);
                lantern.setHanging(true);
                target.getUp().getRear().setBlockData(lantern);

                new StairBuilder(Material.SPRUCE_STAIRS).setFacing(target.getDirection().getOppositeFace())
                                                        .apply(target.getUp(2))
                                                        .apply(target.getFront());
            }
            y++;
        }

    }

    private void spawnSpire(@NotNull SimpleBlock block) {
        block.Pillar(3, Material.SPRUCE_LOG);
        block.getUp(3).setType(Material.COBBLESTONE_WALL);
        block.getUp(4).setType(Material.SPRUCE_FENCE);
        for (BlockFace face : BlockUtils.directBlockFaces) {
            new StairBuilder(Material.SPRUCE_STAIRS).setFacing(face.getOppositeFace()).lapply(block.getRelative(face));

            new TrapdoorBuilder(Material.SPRUCE_TRAPDOOR).setFacing(face)
                                                         .setOpen(true)
                                                         .apply(block.getUp(2).getRelative(face));
        }

    }

    @Override
    public int[][] getCoordsFromMegaChunk(@NotNull TerraformWorld tw, @NotNull MegaChunk mc) {
        int num = TConfig.c.STRUCTURES_IGLOO_COUNT_PER_MEGACHUNK;
        int[][] coords = new int[num][2];
        for (int i = 0; i < num; i++) {
            coords[i] = mc.getRandomCenterChunkBlockCoords(tw.getHashedRand(mc.getX(), mc.getZ(), 992722 * (1 + i)));
        }
        return coords;

    }

    public int[] getNearestFeature(@NotNull TerraformWorld tw, int rawX, int rawZ) {
        MegaChunk mc = new MegaChunk(rawX, 0, rawZ);

        double minDistanceSquared = Integer.MAX_VALUE;
        int[] min = null;
        for (int nx = -1; nx <= 1; nx++) {
            for (int nz = -1; nz <= 1; nz++) {
                for (int[] loc : getCoordsFromMegaChunk(tw, mc)) {
                    double distSqr = Math.pow(loc[0] - rawX, 2) + Math.pow(loc[1] - rawZ, 2);
                    if (distSqr < minDistanceSquared) {
                        minDistanceSquared = distSqr;
                        min = loc;
                    }
                }
            }
        }
        return min;
    }

    private boolean rollSpawnRatio(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 976123),
                (int) (TConfig.c.STRUCTURES_IGLOO_SPAWNRATIO * 10000),
                10000
        );
    }

    @Override
    public boolean canSpawn(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        if (!isEnabled()) {
            return false;
        }

        MegaChunk mc = new MegaChunk(chunkX, chunkZ);
        for (int[] coords : getCoordsFromMegaChunk(tw, mc)) {
            if (coords[0] >> 4 == chunkX && coords[1] >> 4 == chunkZ) {
                EnumSet<BiomeBank> biomes = GenUtils.getBiomesInChunk(tw, chunkX, chunkZ);
                double suitable = 0;
                double notsuitable = 0;
                for (BiomeBank b : biomes) {
                    if ((b.getClimate() != BiomeClimate.SNOWY) || b.getType() != BiomeType.FLAT) {
                        notsuitable++;
                    }
                    else {
                        suitable++;
                    }
                }

                return (suitable / (suitable + notsuitable)) > 0.5 && rollSpawnRatio(tw, chunkX, chunkZ);
            }
        }
        return false;
    }

    @Override
    public @NotNull Random getHashedRandom(@NotNull TerraformWorld world, int chunkX, int chunkZ) {
        return world.getHashedRand(823641811, chunkX, chunkZ);
    }

    @Override
    public boolean isEnabled() {
        return TConfig.areStructuresEnabled() && TConfig.c.STRUCTURES_IGLOO_ENABLED;
    }

    @Override
    public int getChunkBufferDistance() {
        return 1;
    }
}
