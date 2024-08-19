package org.terraform.structure.monument;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.NaturalSpawnType;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class MonumentPopulator extends SingleMegaChunkStructurePopulator {
    public static void arch(@NotNull Wall w,
                            @NotNull MonumentDesign design,
                            @NotNull Random random,
                            int archHalfLength,
                            int height)
    {
        Wall arch = w.getRelative(0, height, 0);
        BlockFace left = BlockUtils.getAdjacentFaces(w.getDirection())[1];
        BlockFace right = BlockUtils.getAdjacentFaces(w.getDirection())[0];

        Stairs ls = (Stairs) Bukkit.createBlockData(design.stairs());
        ls.setWaterlogged(true);
        ls.setFacing(left);

        Stairs rs = (Stairs) Bukkit.createBlockData(design.stairs());
        rs.setWaterlogged(true);
        rs.setFacing(right);

        // Top straight line
        for (int i = 0; i < archHalfLength - 1; i++) {
            if (i <= 1) {
                Slab slab = (Slab) Bukkit.createBlockData(design.slab());
                arch.getLeft(i).setBlockData(slab);
                arch.getRight(i).setBlockData(slab);
            }
            arch.getLeft(i).setType(design.mat(random));
            arch.getRight(i).setType(design.mat(random));
        }

        // Top decor
        arch.getUp().setType(Material.SEA_LANTERN);
        arch.getUp(2).setType(design.slab());
        arch.getUp().getLeft(1).setType(design.mat(random));
        arch.getUp().getRight(1).setType(design.mat(random));
        arch.getUp().getLeft(2).setBlockData(ls);
        arch.getUp().getRight(2).setBlockData(rs);

        // Bending sides
        arch.getLeft(archHalfLength - 2).setBlockData(ls);
        arch.getDown().getLeft(archHalfLength).setBlockData(ls);

        arch.getRight(archHalfLength - 2).setBlockData(rs);
        arch.getDown().getRight(archHalfLength).setBlockData(rs);

        arch.getLeft(archHalfLength - 1).setType(design.slab());
        arch.getRight(archHalfLength - 1).setType(design.slab());
        arch.getLeft(archHalfLength - 1).getDown().setType(Material.SEA_LANTERN);
        arch.getRight(archHalfLength - 1).getDown().setType(Material.SEA_LANTERN);

        // Vertical area
        arch.getLeft(archHalfLength).getDown(2).downUntilSolid(random, design.tileSet);
        arch.getRight(archHalfLength).getDown(2).downUntilSolid(random, design.tileSet);
    }

    /**
     * Create a small platform.
     */
    private static void lightPlatform(@NotNull PopulatorDataAbstract data, int x, int y, int z) {
        for (int nx = -2; nx <= 2; nx++) {
            for (int nz = -2; nz <= 2; nz++) {
                data.setType(x + nx, y, z + nz, Material.PRISMARINE_BRICKS);
            }
        }
    }

    private static void vegetateNearby(@NotNull Random rand,
                                       @NotNull PopulatorDataAbstract data,
                                       int range,
                                       int x,
                                       int z)
    {
        int i = 25;
        for (int nx = x - range / 2 - i; nx <= x + range / 2 + i; nx++) {
            for (int nz = z - range / 2 - i; nz <= z + range / 2 + i; nz++) {
                if (GenUtils.chance(rand, 2, 5)) {
                    int y = GenUtils.getTrueHighestBlock(data, nx, nz);
                    // Don't place on weird blocks
                    if (data.getType(nx, y, nz).toString().contains("SLAB")
                        || data.getType(nx, y, nz)
                               .toString()
                               .contains("STAIR")
                        || data.getType(nx, y, nz).toString().contains("WALL"))
                    {
                        continue;
                    }
                    if (y < TerraformGenerator.seaLevel) {
                        if (GenUtils.chance(rand, 9, 10)) {
                            CoralGenerator.generateKelpGrowth(data, nx, y + 1, nz);
                        }
                        else {
                            CoralGenerator.generateSeaPickles(data, nx, y + 1, nz);
                        }
                    }
                }
            }
        }
    }

    private static void setupGuardianSpawns(@NotNull PopulatorDataAbstract data, int range, int x, int y, int z) {
        int i = -5;
        ArrayList<Integer> done = new ArrayList<>();
        for (int nx = x - range / 2 - i; nx <= x + range / 2 + i; nx++) {
            for (int nz = z - range / 2 - i; nz <= z + range / 2 + i; nz++) {
                int chunkX = nx >> 4;
                int chunkZ = nz >> 4;
                int hash = Objects.hash(chunkX, chunkZ);

                if (done.contains(hash)) {
                    continue;
                }

                done.add(hash);
                TerraformGeneratorPlugin.injector.getICAData(((PopulatorDataPostGen) data).getWorld()
                                                                                          .getChunkAt(chunkX, chunkZ))
                                                 .registerNaturalSpawns(NaturalSpawnType.GUARDIAN,
                                                         x - range / 2,
                                                         y,
                                                         z - range / 2,
                                                         x + range / 2,
                                                         TerraformGenerator.seaLevel,
                                                         z + range / 2
                                                 );
            }
        }

    }

    @Override
    public boolean canSpawn(@NotNull TerraformWorld tw, int chunkX, int chunkZ, @NotNull BiomeBank biome) {
        if (!isEnabled()) {
            return false;
        }

        if (biome.getType() != BiomeType.DEEP_OCEANIC || biome == BiomeBank.MUSHROOM_ISLANDS) {
            return false;
        }
        return rollSpawnRatio(tw, chunkX, chunkZ);
    }

    private boolean rollSpawnRatio(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 92992),
                (int) (TConfig.c.STRUCTURES_MONUMENT_SPAWNRATIO * 10000),
                10000
        );
    }

    @Override
    public void populate(@NotNull TerraformWorld tw, @NotNull PopulatorDataAbstract data) {
        if (!isEnabled()) {
            return;
        }

        int[] coords = new MegaChunk(data.getChunkX(), data.getChunkZ()).getCenterBiomeSectionBlockCoords();
        int x = coords[0];
        int z = coords[1];
        int y = GenUtils.getHighestGround(data, x, z);

        spawnMonument(tw, tw.getHashedRand(x, y, z, 9299724), data, x, y, z);
    }

    public void spawnMonument(@NotNull TerraformWorld tw,
                              @NotNull Random random,
                              @NotNull PopulatorDataAbstract data,
                              int x,
                              int y,
                              int z)
    {
        TerraformGeneratorPlugin.logger.info("Spawning Monument at: " + x + "," + z);
        MonumentDesign design = MonumentDesign.values()[random.nextInt(MonumentDesign.values().length)];
        int numRooms = 1000;
        int range = 50;
        spawnMonumentBase(tw, design, random, data, x, y, z, range);

        Random hashedRand = tw.getHashedRand(x, y, z);

        RoomLayoutGenerator gen = new RoomLayoutGenerator(
                hashedRand,
                RoomLayout.RANDOM_BRUTEFORCE,
                numRooms,
                x,
                y,
                z,
                range
        );
        gen.setPathPopulator(new MonumentPathPopulator(design, tw.getHashedRand(x, y, z, 77)));
        gen.setRoomMaxX(15);
        gen.setRoomMaxZ(15);
        gen.setRoomMinX(10);
        gen.setRoomMinZ(10);
        gen.setRoomMaxHeight(22);
        gen.setRoomMinHeight(9);
        gen.registerRoomPopulator(new TreasureRoomPopulator(random, design, true, true));
        gen.registerRoomPopulator(new LevelledElderRoomPopulator(random, design, true, true));
        gen.registerRoomPopulator(new LevelledElderRoomPopulator(random, design, true, true));
        gen.registerRoomPopulator(new MiniRoomNetworkPopulator(random, design, false, false));
        gen.registerRoomPopulator(new CoralRoomPopulator(random, design, false, false));
        gen.registerRoomPopulator(new FishCageRoomPopulator(random, design, false, false));
        gen.registerRoomPopulator(new HollowPillarRoomPopulator(random, design, false, false));
        gen.registerRoomPopulator(new LanternPillarRoomPopulator(random, design, false, false));
        gen.calculateRoomPlacement(false);
        gen.fill(data, tw, Material.PRISMARINE_BRICKS, Material.PRISMARINE_BRICKS, Material.PRISMARINE);

        carveBaseHallways(tw, random, data, x, y, z, range);
        spawnMonumentEntrance(tw, design, random, data, x, y, z, range);
        vegetateNearby(random, data, range, x, z);
        setupGuardianSpawns(data, range, x, y, z);
    }

    private void entranceSegment(@NotNull Wall w, @NotNull Random random, MonumentDesign design) {
        // Entrance hole
        for (int i = 0; i < 12; i++) {
            w.getRear(i).Pillar(6, random, Material.WATER);
        }
        Stairs stair = (Stairs) Bukkit.createBlockData(Material.PRISMARINE_BRICK_STAIRS);
        stair.setWaterlogged(true);
        stair.setHalf(Half.TOP);
        stair.setFacing(w.getDirection().getOppositeFace());
        w.getRear(11).getUp(5).setBlockData(stair);
        w.getFront().Pillar(6, random, Material.WATER);
    }

    /**
     * Carve a monument entrance.
     */
    public void spawnMonumentEntrance(TerraformWorld tw,
                                      @NotNull MonumentDesign design,
                                      @NotNull Random random,
                                      @NotNull PopulatorDataAbstract data,
                                      int x,
                                      int y,
                                      int z,
                                      int range)
    {
        range += 38;
        BlockFace dir = BlockUtils.getDirectBlockFace(random);
        SimpleBlock base = new SimpleBlock(data, x, y + 1, z);
        for (int i = 0; i < range / 2; i++) {
            base = base.getRelative(dir);
        }
        Wall w = new Wall(base, dir);
        Wall leftClone = w.clone();
        Wall rightClone = w.clone();
        int halfLength = 4 + random.nextInt(3);
        for (int i = 0; i < halfLength; i++) {
            entranceSegment(leftClone, random, design);
            entranceSegment(rightClone, random, design);

            rightClone = rightClone.getRight();
            leftClone = leftClone.getLeft();
        }

        // Build entrance archs.
        for (int i = 0; i < 12; i += 3) {
            arch(w.getRear(i), design, random, halfLength + 2, 10);
        }

    }

    /**
     * Spawns a pyramid-ish base
     */
    public void spawnMonumentBase(TerraformWorld tw,
                                  @NotNull MonumentDesign design,
                                  @NotNull Random random,
                                  @NotNull PopulatorDataAbstract data,
                                  int x,
                                  int y,
                                  int z,
                                  int range)
    {
        range += 30;
        for (int i = 6; i >= 0; i--) {
            for (int nx = x - range / 2 - i; nx <= x + range / 2 + i; nx++) {
                for (int nz = z - range / 2 - i; nz <= z + range / 2 + i; nz++) {

                    // Spires on the corners
                    if (i % 2 == 0) {
                        if (nx == x - range / 2 - i || nx == x + range / 2 + i) {
                            if (nz == z - range / 2 - i || nz == z + range / 2 + i) {
                                design.spire(
                                        new Wall(new SimpleBlock(data, nx, y + (6 - i) + 1, nz), BlockFace.NORTH),
                                        random
                                );
                            }
                        }
                    }

                    data.setType(
                            nx,
                            y + (6 - i),
                            nz,
                            GenUtils.randChoice(random,
                                    Material.PRISMARINE_BRICKS,
                                    Material.PRISMARINE_BRICKS,
                                    Material.PRISMARINE
                            )
                    );
                }
            }
        }

        // Spawn large lamps
        int pad = 5;
        lightPlatform(data, x - range / 2 + pad, y + 7, z - range / 2 + pad);
        design.spawnLargeLight(data, x - range / 2 + pad, y + 8, z - range / 2 + pad);

        lightPlatform(data, x + range / 2 - pad, y + 7, z - range / 2 + pad);
        design.spawnLargeLight(data, x + range / 2 - pad, y + 8, z - range / 2 + pad);

        lightPlatform(data, x + range / 2 - pad, y + 7, z + range / 2 - pad);
        design.spawnLargeLight(data, x + range / 2 - pad, y + 8, z + range / 2 - pad);

        lightPlatform(data, x - range / 2 + pad, y + 7, z + range / 2 - pad);
        design.spawnLargeLight(data, x - range / 2 + pad, y + 8, z + range / 2 - pad);
    }

    /**
     * Carves a main hallway in the monument
     */
    public void carveBaseHallways(TerraformWorld tw,
                                  Random random,
                                  @NotNull PopulatorDataAbstract data,
                                  int x,
                                  int y,
                                  int z,
                                  int range)
    {
        range += 29;
        for (int ny = y + 1; ny <= y + 4; ny++) {
            for (int nx = x - range / 2; nx <= x + range / 2; nx++) {
                for (int nz = z - range / 2; nz <= z + range / 2; nz++) {

                    // Don't touch the middle
                    if (nx > x + 5 - range / 2
                        && nx < x - 5 + range / 2
                        && nz > z + 5 - range / 2
                        && nz < z - 5 + range / 2)
                    {
                        continue;
                    }
                    data.setType(nx, ny, nz, Material.WATER);
                }
            }
        }

        // Light the floor in the hallway
        for (int nx = x - range / 2 + 3; nx <= x + range / 2 - 3; nx += 2) {
            data.setType(nx, y, z - range / 2 + 3, Material.SEA_LANTERN);
            data.setType(nx, y, z + range / 2 - 3, Material.SEA_LANTERN);
        }

        for (int nz = z - range / 2 + 3; nz <= z + range / 2 - 3; nz += 2) {
            data.setType(x - range / 2 + 3, y, nz, Material.SEA_LANTERN);
            data.setType(x + range / 2 - 3, y, nz, Material.SEA_LANTERN);
        }
    }

    @Override
    public @NotNull Random getHashedRandom(@NotNull TerraformWorld world, int chunkX, int chunkZ) {
        return world.getHashedRand(888271981, chunkX, chunkZ);
    }

    @Override
    public int getChunkBufferDistance() {
        return 4;
    }

    @Override
    public boolean isEnabled() {
        return TConfig.areStructuresEnabled() && TConfig.c.STRUCTURES_MONUMENT_ENABLED;
    }
}
