package org.terraform.structure.pyramid;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Stairs;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TConfigOption;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayout;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Random;

public class PyramidPopulator extends SingleMegaChunkStructurePopulator {

    @Override
    public boolean canSpawn(TerraformWorld tw, int chunkX, int chunkZ, ArrayList<BiomeBank> biomes) {

        MegaChunk mc = new MegaChunk(chunkX, chunkZ);
        int[] coords = getCoordsFromMegaChunk(tw, mc);

        for (BiomeBank biome : biomes) {
            if (biome != BiomeBank.DESERT)
                return false;
        }
        return coords[0] >> 4 == chunkX && coords[1] >> 4 == chunkZ &&
                rollSpawnRatio(tw, chunkX, chunkZ);
    }

    private boolean rollSpawnRatio(TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 92992),
                (int) (TConfigOption.STRUCTURES_MONUMENT_SPAWNRATIO
                        .getDouble() * 1000),
                1000);
    }

    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {

        int[] coords = getCoordsFromMegaChunk(tw, new MegaChunk(data.getChunkX(), data.getChunkZ()));
        int x = coords[0];
        int z = coords[1];
        int y = GenUtils.getHighestGround(data, x, z);

        spawnPyramid(tw, tw.getHashedRand(x, y, z, 9299724), data, x, y, z);
    }

    public void spawnPyramid(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z) {
        TerraformGeneratorPlugin.logger.info("Spawning Pyramid at: " + x + "," + z);
        int numRooms = 1000;
        int range = 70;
        spawnPyramidBase(data, x, y, z);

        Random hashedRand = tw.getHashedRand(x, y, z);

        //Level 0 - Labyrinth Layer (Dark, full of traps). Also the entry level.
        RoomLayoutGenerator level0 = new RoomLayoutGenerator(hashedRand, RoomLayout.RANDOM_BRUTEFORCE, numRooms, x, y - 8, z, range);
//		private int roomMaxHeight = 7;
//		private int roomMinHeight = 5;
//		private int roomMaxX = 15;
//		private int roomMinX = 10;
//		private int roomMaxZ = 15;
//		private int roomMinZ = 10;
        //Add pyramid entrance at the pyramid corner
        CubeRoom entranceRoom = new CubeRoom(9, 9, 8 + 5, x, y - 8, z + 5 + range / 2);
        MainEntrancePopulator entrancePopulator = new MainEntrancePopulator(hashedRand, false, false, BlockFace.NORTH);
        entranceRoom.setRoomPopulator(entrancePopulator);
        level0.getRooms().add(entranceRoom);
        level0.registerRoomPopulator(new HuskTombPopulator(random, false, true));
        level0.registerRoomPopulator(new CryptRoom(random, false, false));
        level0.registerRoomPopulator(new GuardianChamberPopulator(random, false, false));
        level0.registerRoomPopulator(new TrapChestChamberPopulator(random, false, false));
        level0.setPathPopulator(new PyramidDungeonPathPopulator(tw.getHashedRand(x, y - 8, z, 2233)));

        range -= 20;

        //Level 1
        RoomLayoutGenerator level1 = new RoomLayoutGenerator(hashedRand, RoomLayout.RANDOM_BRUTEFORCE, numRooms, x, y, z, range);
        level1.registerRoomPopulator(new HuskTombPopulator(random, false, true));
        level1.registerRoomPopulator(new TerracottaRoom(random, false, false));
        level1.setPathPopulator(new PyramidPathPopulator(tw.getHashedRand(x, y, z, 2233)));

        //Placeholder room to prevent stairways spawning in the middle.
        CubeRoom placeholder = new CubeRoom(20, 20, 15, x, y, z);
        level1.getRooms().add(placeholder);
        //Stairways (Level 0 to 1)
        for (int i = 0; i < 4; i++) {
            CubeRoom stairway = level0.forceAddRoom(5, 5, 10);
            stairway.setRoomPopulator(new PyramidStairwayRoomPopulator(random, false, false));
            CubeRoom stairwayTop = new CubeRoom(5, 5, 5, stairway.getX(), y, stairway.getZ());
            stairwayTop.setRoomPopulator(new PyramidStairwayTopPopulator(random, false, false));
            level1.getRooms().add(stairwayTop);
        }

        range -= 20;

        //Level 2
        RoomLayoutGenerator level2 = new RoomLayoutGenerator(hashedRand, RoomLayout.RANDOM_BRUTEFORCE, numRooms, x, y + 8, z, range);
        level2.setRoomMaxX(10);
        level2.setRoomMinX(7);
        level2.setRoomMaxZ(10);
        level2.setRoomMinZ(7);
        level2.setRoomMaxHeight(6);
        level2.registerRoomPopulator(new TerracottaRoom(random, false, false));
        level2.setPathPopulator(new PyramidPathPopulator(tw.getHashedRand(x, y + 8, z, 2253)));

        //Tomb room. 
        CubeRoom tomb = new CubeRoom(20, 20, 20, x, y + 8, z);
        tomb.setRoomPopulator(new PharoahsTombPopulator(tw.getHashedRand(x, y + 8, z, 1121), true, true));
        level2.getRooms().add(tomb);

        //Stairways (Level 1 to 2)
        for (int i = 0; i < 3; i++) {
            CubeRoom stairway = level1.forceAddRoom(5, 5, 10);
            stairway.setRoomPopulator(new PyramidStairwayRoomPopulator(random, false, false));
            CubeRoom stairwayTop = new CubeRoom(5, 5, 5, stairway.getX(), y + 8, stairway.getZ());
            stairwayTop.setRoomPopulator(new PyramidStairwayTopPopulator(random, false, false));
            level2.getRooms().add(stairwayTop);
        }

        //Remove tombroom placeholder to allow other rooms to spawn there.
        level1.getRooms().remove(placeholder);

        range -= 15;

        //Fill Rooms
        level0.generate(false);
        level0.fill(data, tw, Material.SANDSTONE, Material.CUT_SANDSTONE);

        //ALL LEVEL 0 FLOORING IS STONE/COBBLE/ANDESITE. 
        //Facilitates traps as there is no sandstone pressure plate
        ArrayList<Material> toReplace = new ArrayList<Material>() {{
            add(Material.SANDSTONE);
            add(Material.CUT_SANDSTONE);
        }};
        for (int nx = -50; nx <= 50; nx++) {
            for (int nz = -50; nz <= 50; nz++) {
                if (toReplace.contains(data.getType(x + nx, y - 8, z + nz)))
                    data.setType(x + nx, y - 8, z + nz, GenUtils.randMaterial(Material.STONE, Material.STONE, Material.STONE, Material.COBBLESTONE, Material.ANDESITE));

                //Dither
                if (random.nextBoolean()) {
                    if (toReplace.contains(data.getType(x + nx, y - 7, z + nz)))
                        data.setType(x + nx, y - 7, z + nz, GenUtils.randMaterial(Material.STONE, Material.STONE, Material.STONE, Material.COBBLESTONE, Material.ANDESITE));
                    if (random.nextBoolean()) {
                        if (toReplace.contains(data.getType(x + nx, y - 6, z + nz)))
                            data.setType(x + nx, y - 6, z + nz, GenUtils.randMaterial(Material.STONE, Material.STONE, Material.STONE, Material.COBBLESTONE, Material.ANDESITE));

                    }
                }

            }
        }

        level1.generate(false);
        level1.fill(data, tw, Material.SANDSTONE, Material.CUT_SANDSTONE);

        level2.generate(false);
        level2.fill(data, tw, Material.SANDSTONE, Material.CUT_SANDSTONE);
    }

    public void spawnPyramidBase(PopulatorDataAbstract data, int x, int y, int z) {
        for (int height = 0; height < 40; height++) {
            int radius = 40 - height;
            for (int nx = -radius; nx <= +radius; nx++) {
                for (int nz = -radius; nz <= +radius; nz++) {
                    data.setType(x + nx, y + height, z + nz, GenUtils.randMaterial(Material.SANDSTONE, Material.SMOOTH_SANDSTONE));

                    //Corners have special decorations
                    if (Math.abs(nx) == radius && Math.abs(nz) == radius) {
                        if (height < 40)
                            data.setType(x + nx, y + height + 1, z + nz, Material.SANDSTONE_WALL);

                        if (height == 38)
                            data.setType(x + nx, y + height + 2, z + nz, Material.CAMPFIRE);
                    } else if (GenUtils.chance(1, 20)) {
                        //Side Decorations (Stairs)
                        BlockFace dir = null;

                        if (nx == -radius) {
                            dir = BlockFace.EAST;
                        } else if (nx == radius) {
                            dir = BlockFace.WEST;
                        } else if (nz == -radius) {
                            dir = BlockFace.SOUTH;
                        } else if (nz == radius) {
                            dir = BlockFace.NORTH;
                        }
                        if (dir != null) {
                            Stairs s = (Stairs) Bukkit.createBlockData(GenUtils.randMaterial(Material.SANDSTONE_STAIRS, Material.SMOOTH_SANDSTONE_STAIRS));
                            s.setFacing(dir);
                            data.setBlockData(x + nx, y + height, z + nz, s);
                        }
                    }
                }
            }
        }

        //Tip
        data.setType(x, y + 40, z, Material.GOLD_BLOCK);
        data.setType(x, y + 41, z, Material.SANDSTONE_WALL);

        //Pyramid Surface Decal
        int elevation = 14;
        for (int height = elevation; height <= elevation + 16; height++) {
            int radius = 40 - height;
            for (int nx : new int[]{-radius, 0, radius}) {
                for (int nz : new int[]{-radius, 0, radius}) {
                    //Pyramid Surface Decal
                    int carveLength = (height - elevation);
                    if (carveLength > 8)
                        carveLength = 16 - carveLength;
                    if (nx != 0 && nz != 0) continue;
                    Wall w = null;
                    if (nx == -radius) {
                        w = new Wall(new SimpleBlock(data, x + nx, y + height, z + nz), BlockFace.WEST);
                    } else if (nx == radius) {
                        w = new Wall(new SimpleBlock(data, x + nx, y + height, z + nz), BlockFace.EAST);
                    } else if (nz == -radius) {
                        w = new Wall(new SimpleBlock(data, x + nx, y + height, z + nz), BlockFace.NORTH);
                    } else if (nz == radius) {
                        w = new Wall(new SimpleBlock(data, x + nx, y + height, z + nz), BlockFace.SOUTH);
                    }
                    if (w != null)
                        for (int i = 0; i <= carveLength; i++) {
                            if (carveLength == 0) {
                                if (height == elevation) {
                                    w.getFront().setType(Material.SANDSTONE_WALL);
                                } else if (height == elevation + 16) {
                                    w.getRear().getRelative(0, 2, 0).setType(Material.SANDSTONE_WALL);
                                }
                            }
                            w.getLeft(i).setType(Material.AIR);
                            w.getLeft(i).getRear().setType(Material.CUT_RED_SANDSTONE);

                            w.getRight(i).setType(Material.AIR);
                            w.getRight(i).getRear().setType(Material.CUT_RED_SANDSTONE);

                            if (i == carveLength) {
                                w.getRight(i + 1).getRelative(0, 1, 0).setType(Material.SANDSTONE_WALL);
                                w.getLeft(i + 1).getRelative(0, 1, 0).setType(Material.SANDSTONE_WALL);
                            }
                        }
                }
            }
        }
    }

    @Override
    public int[] getCoordsFromMegaChunk(TerraformWorld tw, MegaChunk mc) {
        return mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(), 2078891));
    }

    @Override
    public int[] getNearestFeature(TerraformWorld tw, int rawX, int rawZ) {
        MegaChunk mc = new MegaChunk(rawX, 0, rawZ);

        double minDistanceSquared = Integer.MAX_VALUE;
        int[] min = null;
        for (int nx = -1; nx <= 1; nx++) {
            for (int nz = -1; nz <= 1; nz++) {

                int[] loc = getCoordsFromMegaChunk(tw, mc.getRelative(nx, nz));
                double distSqr = Math.pow(loc[0] - rawX, 2) + Math.pow(loc[1] - rawZ, 2);
                if (distSqr < minDistanceSquared && rollSpawnRatio(tw, loc[0] >> 4, loc[1] >> 4)) {
                    minDistanceSquared = distSqr;
                    min = loc;
                }
            }
        }
        return min;
    }


    @Override
    public Random getHashedRandom(TerraformWorld world, int chunkX, int chunkZ) {
        return world.getHashedRand(72917299, chunkX, chunkZ);
    }

    @Override
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return TConfigOption.STRUCTURES_MONUMENT_ENABLED.getBoolean();
    }


}
