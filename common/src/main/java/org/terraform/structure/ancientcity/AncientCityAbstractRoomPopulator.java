package org.terraform.structure.ancientcity;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.structure.room.CarvedRoom;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomLayoutGenerator;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.structure.room.path.PathState;
import org.terraform.utils.GenUtils;
import org.terraform.utils.StairwayBuilder;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.NoiseCacheHandler;
import org.terraform.utils.noise.NoiseCacheHandler.NoiseCacheEntry;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;

public abstract class AncientCityAbstractRoomPopulator extends RoomPopulatorAbstract {

    protected final RoomLayoutGenerator gen;
    final TerraformWorld tw;
    protected int shrunkenWidth = 0;
    protected @NotNull CubeRoom effectiveRoom = null;
    protected HashSet<SimpleBlock> containsPaths = new HashSet<>();
    protected boolean doCarve = true;

    public AncientCityAbstractRoomPopulator(TerraformWorld tw,
                                            RoomLayoutGenerator gen,
                                            Random rand,
                                            boolean forceSpawn,
                                            boolean unique)
    {
        super(rand, forceSpawn, unique);
        this.tw = tw;
        this.gen = gen;
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {

        shrunkenWidth = GenUtils.randInt(this.rand, 2, 4);

        // This variable is named depression, but can also represent elevation.
        int depression = shrunkenWidth;

        // Sometimes the room will be higher than the path's Y.
        if (rand.nextBoolean()) {
            depression = depression * -1;
        }

        this.effectiveRoom = new CarvedRoom(new CubeRoom(room.getWidthX() - shrunkenWidth * 2 - 1,
                room.getWidthZ() - shrunkenWidth * 2 - 1,
                room.getHeight(),
                room.getX(),
                room.getY() + depression,
                room.getZ()
        ));

        // Clear out space for the room
        if(doCarve)
            effectiveRoom.fillRoom(data, Material.CAVE_AIR);

        // Room flooring
        int[] lowerCorner = effectiveRoom.getLowerCorner(0);
        int[] upperCorner = effectiveRoom.getUpperCorner(0);
        int y = effectiveRoom.getY();
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                SimpleBlock b = new SimpleBlock(data, x, y, z);

                // Fuzz the sides to give a sense of ruin
                if (x == lowerCorner[0] || x == upperCorner[0] || z == lowerCorner[1] || z == upperCorner[1]) {
                    if (rand.nextBoolean()) {
                        b.lsetType(AncientCityUtils.deepslateBricks);
                    }
                }
                else {
                    b.lsetType(AncientCityUtils.deepslateBricks);
                }

                // every few intervals, place a pillar
                int relX = effectiveRoom.getX() - x;
                int relZ = effectiveRoom.getZ() - z;
                if (relX % 5 == 0 && relZ % 5 == 0 && (effectiveRoom.isPointInside(b.getRelative(BlockFace.NORTH))
                                                       && effectiveRoom.isPointInside(b.getRelative(BlockFace.SOUTH))
                                                       && effectiveRoom.isPointInside(b.getRelative(BlockFace.EAST))
                                                       && effectiveRoom.isPointInside(b.getRelative(BlockFace.WEST))))
                {
                    AncientCityUtils.placeSupportPillar(b.getDown());
                }

            }
        }

        //Stairs cannot go next to each other as they're width 3
        boolean placedStairs = false;
        // Connect the paths to the rooms
        //This is some scuffed code. In essence, PathNodes are placed in spaced intervals
        // of 1 per 5 blocks. So if we check an area 5 times (j), we will find a path node (maybe)
        for (Entry<Wall, Integer> entry : room.getFourWalls(data, 0).entrySet()) {
            Wall w = entry.getKey().getDown();
            for (int i = shrunkenWidth; i < entry.getValue() - shrunkenWidth; i++) {
                if(!placedStairs)
                    for (int j = 1; j <= 7; j++) {
                        SimpleLocation target = w.getRear(j).getAtY(room.getY()).getLoc();
                        if (this.gen.getOrCalculatePathState(tw).nodes.contains(
                                new PathState.PathNode(target, 1, null)))
                        {
                            placedStairs = true;
                            //Room edge is the ledge of the effective room
                            Wall roomEdge = w.getFront(shrunkenWidth+1).getAtY(effectiveRoom.getY()+1);
                            containsPaths.add(roomEdge);
                            containsPaths.add(roomEdge.getLeft());
                            containsPaths.add(roomEdge.getRight());

                            if (depression > 0) {
                                new StairwayBuilder(Material.DEEPSLATE_BRICK_STAIRS)
                                        .setDownTypes(AncientCityUtils.deepslateBricks)
                                        .setStairwayDirection(BlockFace.DOWN)
                                        .setStopAtY(room.getY())
                                        .build(roomEdge.getDown().getRear().flip())
                                        .build(roomEdge.getDown().getRear().getLeft().flip())
                                        .build(roomEdge.getDown().getRear().getRight().flip());
                            }
                            else {
                                new StairwayBuilder(Material.DEEPSLATE_BRICK_STAIRS)
                                        .setDownTypes(AncientCityUtils.deepslateBricks)
                                        .setStairwayDirection(BlockFace.UP)
                                        .setUpwardsCarveUntilNotSolid(false)
                                        .setStopAtY(room.getY())
                                        .build(roomEdge.getRear().flip())
                                        .build(roomEdge.getRear().getLeft().flip())
                                        .build(roomEdge.getRear().getRight().flip());
                            }
                            //Close potential holes between the stairs and the platform
                            //This is exactly where the effective room's wall is
                            Wall conn = roomEdge.getUp(-depression-1).getRear(Math.abs(depression)+1);
                            for(int conni = 0; conni <= 5; conni++)
                            {
                                boolean wasBlocked = conn.getRear(conni).lsetType(Material.GRAY_WOOL);
                                wasBlocked &= conn.getRear(conni).getLeft().lsetType(Material.GRAY_WOOL);
                                wasBlocked &= conn.getRear(conni).getRight().lsetType(Material.GRAY_WOOL);
                                //Ignore the corner 2's placement.
                                conn.getRear(conni).getRight(2).lsetType(AncientCityUtils.deepslateBricks);
                                conn.getRear(conni).getLeft(2).lsetType(AncientCityUtils.deepslateBricks);
                                if(wasBlocked) break;
                            }

                            break; //BREAK if you see a staircase.
                        }
                    }
                else
                    placedStairs = false;
                w = w.getLeft();
            }
        }
    }

    public void sculkUp(TerraformWorld tw, @NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        FastNoise circleNoise = NoiseCacheHandler.getNoise(tw, NoiseCacheEntry.BIOME_CAVECLUSTER_CIRCLENOISE, world -> {
            FastNoise n = new FastNoise((int) (world.getSeed() * 11));
            n.SetNoiseType(FastNoise.NoiseType.Simplex);
            n.SetFrequency(0.09f);

            return n;
        });
        for (int i = 0; i <= ((room.getWidthX() * room.getWidthZ()) / 150); i++) {
            // Generates 3d coords, but we will discard the y coords.
            // We will separately generate y coords later.
            int[] coords = room.randomCoords(rand);
            int y = rand.nextInt(5);
            SimpleBlock target = new SimpleBlock(data, coords[0], room.getY() + y, coords[2]);
            AncientCityUtils.spreadSculk(circleNoise, rand, 5, target);
        }
    }
}
