package org.terraform.structure.small.ruinedportal;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeClimate;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfig;
import org.terraform.structure.MultiMegaChunkStructurePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CylinderBuilder;
import org.terraform.utils.GenUtils;
import org.terraform.utils.StalactiteBuilder;
import org.terraform.utils.blockdata.ChestBuilder;
import org.terraform.utils.blockdata.StairBuilder;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

public class RuinedPortalPopulator extends MultiMegaChunkStructurePopulator {
    private static final Material[] portalBlocks = new Material[] {Material.OBSIDIAN, Material.CRYING_OBSIDIAN};

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

            spawnRuinedPortal(tw, random, data, x, height + 1, z);
        }
    }

    public void spawnRuinedPortal(@NotNull TerraformWorld tw,
                                  @NotNull Random random,
                                  @NotNull PopulatorDataAbstract data,
                                  int x,
                                  int y,
                                  int z)
    {
        SimpleBlock core = new SimpleBlock(data, x, y, z);
        BiomeBank biome = tw.getBiomeBank(x, z);
        boolean overgrown = biome.getClimate() == BiomeClimate.HUMID_VEGETATION;
        boolean snowy = biome.getClimate() == BiomeClimate.SNOWY;
        int mossiness = 0;

        if (biome.getClimate() == BiomeClimate.HUMID_VEGETATION) {
            mossiness = 2;
        }
        else if (biome.getClimate() == BiomeClimate.DRY_VEGETATION || biome.getClimate() == BiomeClimate.TRANSITION) {
            mossiness = 1;
        }


        spawnRuinedPortal(tw, random, core.getUp(), mossiness, overgrown, snowy);
    }

    /**
     * Underwater checks are done here, as the portal being underwater will
     * override all these vegetation settings.
     */
    public void spawnRuinedPortal(TerraformWorld tw,
                                  @NotNull Random random,
                                  @NotNull SimpleBlock core,
                                  int mossiness,
                                  boolean overgrown,
                                  boolean snowy)
    {
        int horRadius = GenUtils.randInt(random, 2, 4); // ALWAYS EVEN
        int vertHeight = 1 + horRadius * 2; // Can be odd
        BlockFace facing = BlockUtils.getDirectBlockFace(random);
        Wall w = new Wall(core, facing);
        // Material fluid = Material.AIR;
        Material lavaFluid = Material.LAVA;

        // Replace lava when overgrown to prevent forest fires
        if (BlockUtils.isWet(core) || snowy || overgrown) {
            lavaFluid = Material.MAGMA_BLOCK;
        }

        // Nether-rize the land around the portal
        BlockUtils.replaceCircularPatch(random.nextInt(99999),
                horRadius * 2.5f,
                core.getDown(),
                snowy,
                Material.NETHERRACK
        );

        // Flatten land below the portal
        Material[] stoneBricks = BlockUtils.stoneBricks;
        if (mossiness == 0) {
            stoneBricks = new Material[] {Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS};
        }
        if (mossiness > 1) {
            stoneBricks = new Material[] {
                    Material.STONE_BRICKS,
                    Material.CRACKED_STONE_BRICKS,
                    Material.MOSSY_STONE_BRICKS,
                    Material.MOSSY_STONE_BRICKS,
                    Material.MOSSY_STONE_BRICKS
            };
        }
        new CylinderBuilder(random, w.getDown(), stoneBricks).setRadius(horRadius + 1)
                                                             .setRY(1)
                                                             .setHardReplace(false)
                                                             .build();

        // [Side decorations]============================
        Wall effectiveGround = w.getRight(horRadius).findFloor(10);
        int heightCorrection = vertHeight;
        if (effectiveGround != null) {
            if (effectiveGround.getY() >= w.getY() - 2) {
                effectiveGround = effectiveGround.getAtY(w.getY() - 2);
            }
            else {
                heightCorrection = vertHeight + (w.getY() - 2 - effectiveGround.getY());
            }
            new StalactiteBuilder(portalBlocks).setVerticalSpace(Math.round(2.5f * heightCorrection * 1.5f))
                                               .setFacingUp(true)
                                               .setSolidBlockType(portalBlocks)
                                               .setMinRadius(2)
                                               .build(random, effectiveGround);
        }
        effectiveGround = w.getLeft(horRadius + 1).findFloor(10);
        heightCorrection = vertHeight;
        if (effectiveGround != null) {
            if (effectiveGround.getY() >= w.getY() - 2) {
                effectiveGround = effectiveGround.getAtY(w.getY() - 2);
            }
            else {
                heightCorrection = vertHeight + (w.getY() - 2 - effectiveGround.getY());
            }
            new StalactiteBuilder(portalBlocks).setVerticalSpace(Math.round(2.5f * heightCorrection * 1.5f))
                                               .setFacingUp(true)
                                               .setSolidBlockType(portalBlocks)
                                               .setMinRadius(2)
                                               .build(random, effectiveGround);
        }

        // [End side decorations]========================

        // Build the core portal
        for (int left = 0; left < horRadius; left++) {
            w.getLeft(left).setType(portalBlocks);

            // Empty center of portal
            for (int depth = -3; depth <= 3; depth++) // depth is the horizontal depth
            {
                for (int height = 0; height < vertHeight - 2; height++) // Height is vertical height
                {
                    Wall target = w.getFront(depth).getLeft(left).getUp(1 + height);
                    target.setType(getFluid(target));
                }
            }


            w.getUp(vertHeight).getLeft(left).setType(portalBlocks);
            w.getUp(vertHeight + 1)
             .getLeft(left)
             .setType(Material.STONE_BRICK_SLAB, getFluid(w.getUp(vertHeight + 1).getLeft(left)));
        }
        for (int right = 1; right < horRadius - 1; right++) {
            w.getRight(right).setType(portalBlocks);
            for (int depth = -3; depth <= 3; depth++) // depth is the horizontal depth
            {
                for (int height = 0; height < vertHeight - 2; height++) // Height is vertical height
                {
                    Wall target = w.getFront(depth).getRight(right).getUp(1 + height);
                    target.setType(getFluid(target));
                }
            }


            w.getUp(vertHeight).getRight(right).setType(portalBlocks);
            w.getUp(vertHeight + 1)
             .getRight(right)
             .setType(Material.STONE_BRICK_SLAB, getFluid(w.getUp(vertHeight + 1).getRight(right)));
        }

        w.getLeft(horRadius).Pillar(1 + vertHeight, portalBlocks);
        w.getRight(horRadius - 1).Pillar(1 + vertHeight, portalBlocks);

        Wall rightCorner = w.getUp(vertHeight).getRight(horRadius - 1);
        Wall leftCorner = w.getUp(vertHeight).getLeft(horRadius);

        // [Carve out a lava fissure]=================
        FastNoise noise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.STRUCTURE_RUINEDPORTAL_FISSURES, world -> {
            FastNoise n = new FastNoise((int) world.getSeed());
            n.SetNoiseType(NoiseType.PerlinFractal);
            n.SetFrequency(0.035f);
            n.SetFractalOctaves(5);
            return n;
        });

        HashMap<SimpleBlock, Integer> lavaLocs = new HashMap<>();
        int lowestY = 9999;
        for (int relx = -horRadius * 3; relx < horRadius * 3; relx++) {
            for (int relz = -horRadius * 3; relz < horRadius * 3; relz++) {
                double fissureNoise = (3 - 100 * Math.abs(noise.GetNoise(relx + core.getX(), relz + core.getZ())));
                if (fissureNoise > 0) {
                    SimpleBlock target = core.getRelative(relx, 0, relz).getGround();
                    fissureNoise = (1.0 - Math.min(1f, target.distance(core) / (horRadius * 3))) * fissureNoise;
                    lavaLocs.put(target, (int) Math.round(fissureNoise));
                    if (lowestY > target.getY()) {
                        lowestY = target.getY();
                    }
                }
            }
        }

        for (Entry<SimpleBlock, Integer> entry : lavaLocs.entrySet()) {
            SimpleBlock target = entry.getKey();
            int depth = entry.getValue();
            int tempY = target.getY();
            target = target.getAtY(lowestY);
            for (int i = target.getY(); i <= tempY; i++) {
                target.getAtY(i).setType(getFluid(target.getAtY(i)));
            }
            for (int i = 0; i <= depth; i++) {
                target.getDown(i).setType(lavaFluid);
            }
        }
        // [End carve lava fissure]======================

        // [Break one of the corners]==================

        // Break right corner
        if (random.nextBoolean()) {
            for (int i = 0; i < GenUtils.randInt(1, 3); i++) {
                rightCorner.getDown(i).setType(getFluid(rightCorner.getDown(i)));
            }
            BlockUtils.dropDownBlock(rightCorner.getUp(), getFluid(rightCorner.getUp()));

            for (int i = 1; i < GenUtils.randInt(random, 2, horRadius + 2); i++) {
                rightCorner.getLeft(i).setType(getFluid(rightCorner.getLeft(i)));
                BlockUtils.dropDownBlock(rightCorner.getLeft(i).getUp(), getFluid(rightCorner.getLeft(i).getUp()));
            }
            if (overgrown && TConfig.arePlantsEnabled() && leftCorner.getRight().isSolid()) {
                leftCorner.getRight()
                          .getRear()
                          .downLPillar(random, GenUtils.randInt(vertHeight / 2, vertHeight - 1), Material.OAK_LEAVES);
                leftCorner.getRight()
                          .getFront()
                          .downLPillar(random, GenUtils.randInt(vertHeight / 2, vertHeight - 1), Material.OAK_LEAVES);
            }
            // Break left corner
        }
        else {
            for (int i = 0; i < GenUtils.randInt(1, 3); i++) {
                leftCorner.getDown(i).setType(getFluid(leftCorner.getDown(i)));
            }
            BlockUtils.dropDownBlock(leftCorner.getUp(), getFluid(leftCorner.getUp()));
            for (int i = 1; i < GenUtils.randInt(random, 2, horRadius + 2); i++) {
                leftCorner.getRight(i).setType(getFluid(leftCorner.getRight(i)));
                BlockUtils.dropDownBlock(leftCorner.getRight(i).getUp(), getFluid(leftCorner.getRight(i).getUp()));
            }
            if (overgrown && TConfig.arePlantsEnabled() && rightCorner.getLeft().isSolid()) {
                rightCorner.getLeft()
                           .getRear()
                           .downLPillar(random, GenUtils.randInt(vertHeight / 2, vertHeight - 1), Material.OAK_LEAVES);
                rightCorner.getLeft()
                           .getFront()
                           .downLPillar(random, GenUtils.randInt(vertHeight / 2, vertHeight - 1), Material.OAK_LEAVES);
            }
        }

        // Upper decorations
        if (rightCorner.isSolid()) {
            new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(BlockUtils.getLeft(w.getDirection()))
                                                         .apply(rightCorner.getUp());
        }
        if (leftCorner.isSolid()) {
            new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(BlockUtils.getRight(w.getDirection()))
                                                         .apply(leftCorner.getUp());
        }

        // Gold blocks
        if (w.getUp(vertHeight).isSolid()) {
            new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(BlockUtils.getLeft(w.getDirection()))
                                                         .apply(w.getUp(2 + vertHeight));
            w.getUp(1 + vertHeight).setType(Material.GOLD_BLOCK, Material.CHISELED_STONE_BRICKS);
        }

        if (w.getLeft().getUp(vertHeight).isSolid()) {
            new StairBuilder(Material.STONE_BRICK_STAIRS).setFacing(BlockUtils.getRight(w.getDirection()))
                                                         .apply(w.getLeft().getUp(2 + vertHeight));
            w.getLeft().getUp(1 + vertHeight).setType(Material.GOLD_BLOCK, Material.CHISELED_STONE_BRICKS);
        }


        // Random chest
        new ChestBuilder(Material.CHEST).setFacing(w.getDirection())
                                        .setLootTable(TerraLootTable.RUINED_PORTAL)
                                        .apply(w.getFront(GenUtils.randInt(3, (int) (horRadius * 1.5f)))
                                                .getRight(GenUtils.randInt(3, (int) (horRadius * 1.5f)))
                                                .getGround()
                                                .getUp());
    }

    private @NotNull Material getFluid(@NotNull SimpleBlock block) {
        if (BlockUtils.isWet(block)) {
            return Material.WATER;
        }
        for (BlockFace face : BlockUtils.directBlockFaces) {
            if (BlockUtils.isWet(block.getRelative(face))) {
                return Material.WATER;
            }
        }
        return Material.AIR;
    }

    @Override
    public int[][] getCoordsFromMegaChunk(@NotNull TerraformWorld tw, @NotNull MegaChunk mc) {
        int num = TConfig.c.STRUCTURES_RUINEDPORTAL_COUNT_PER_MEGACHUNK;
        int[][] coords = new int[num][2];
        for (int i = 0; i < num; i++) {
            coords[i] = mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(), 4363463 * (1 + i)));
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
                (int) (TConfig.c.STRUCTURES_RUINEDPORTAL_SPAWNRATIO * 10000),
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
                return rollSpawnRatio(tw, chunkX, chunkZ);
            }
        }
        return false;
    }

    @Override
    public @NotNull Random getHashedRandom(@NotNull TerraformWorld world, int chunkX, int chunkZ) {
        return world.getHashedRand(729384234, chunkX, chunkZ);
    }

    @Override
    public boolean isEnabled() {
        return TConfig.areStructuresEnabled() && TConfig.c.STRUCTURES_RUINEDPORTAL_ENABLED;
    }

    @Override
    public int getChunkBufferDistance() {
        return 1;
    }
}
