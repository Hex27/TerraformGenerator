package org.terraform.structure.village.plains;

import org.bukkit.Material;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.Map.Entry;
import java.util.Random;

public class PlainsVillageAnimalPenPopulator extends PlainsVillageAbstractRoomPopulator {

    private static final EntityType[] farmAnimals = {
            EntityType.PIG, EntityType.SHEEP, EntityType.COW, EntityType.HORSE, EntityType.CHICKEN
    };

    private final PlainsVillagePopulator plainsVillagePopulator;

    public PlainsVillageAnimalPenPopulator(PlainsVillagePopulator plainsVillagePopulator,
                                           Random rand,
                                           boolean forceSpawn,
                                           boolean unique)
    {
        super(rand, forceSpawn, unique);
        this.plainsVillagePopulator = plainsVillagePopulator;
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {

        // If terrain is adverse,
        // just forget it, animal pens don't fare well in hilly terrain.
        if (super.doesAreaFailTolerance(data, room)) {
            return;
        }

        int roomY = super.calculateRoomY(data, room);


        // For animal farms, they look increasingly stupid when tilted.
        // Just give up and place a platform underneath.
        // super.placeFixerPlatform(roomY, data, room);
        // Maybe this isn't needed as it no longer places when terrain is adverse

        SimpleBlock jobBlock = null;
        boolean spawnedWater = false;
        // Place fence
        for (Entry<Wall, Integer> entry : room.getFourWalls(data, 2).entrySet()) {
            Wall w = entry.getKey();
            for (int i = 0; i < entry.getValue(); i++) {
                Wall target = w.getAtY(roomY).findNearestAirPocket(15);
                if (target != null) {
                    if (target.getDown().getType() != Material.COBBLESTONE_SLAB
                        && target.getDown().getType() != plainsVillagePopulator.woodFence)
                    {
                        int wallHeight = 3;
                        if (target.getY() < roomY) {
                            wallHeight = 2 + (roomY - target.getY());
                        }

                        if (i % 2 == 0) {
                            target.Pillar(wallHeight, plainsVillagePopulator.woodLog);
                            target.getUp(wallHeight).setType(Material.COBBLESTONE_SLAB);
                            target.getDown(2).getRight().CorrectMultipleFacing(wallHeight + 2);
                            target.getDown(2).getLeft().CorrectMultipleFacing(wallHeight + 2);
                        }
                        else {
                            target.Pillar(wallHeight, plainsVillagePopulator.woodFence);
                            target.CorrectMultipleFacing(wallHeight);
                        }

                    }
                    if (w.getDirection() == ((DirectionalCubeRoom) room).getDirection().getOppositeFace()
                        && i == entry.getValue() / 2)
                    {
                        jobBlock = target.getRear();
                    }
                }
                w = w.getLeft();
            }

        }

        // Decorations
        int[] lowerCorner = room.getLowerCorner(3);
        int[] upperCorner = room.getUpperCorner(3);

        // Change the floor
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {

                int highest;
                //                if(!areaFailedTolerance) {
                highest = GenUtils.getHighestGroundOrSeaLevel(data, x, z);
                //                }
                //                else
                //                	highest = roomY;

                if (Math.abs(highest - roomY) > TConfig.c.STRUCTURES_PLAINSVILLAGE_HEIGHT_TOLERANCE) {
                    continue;
                }

                BlockUtils.setDownUntilSolid(x, highest, z, data, Material.DIRT);

                if (rand.nextBoolean()) {
                    data.setType(x,
                            highest,
                            z,
                            GenUtils.randChoice(Material.PODZOL, Material.COARSE_DIRT, Material.GRASS_BLOCK)
                    );
                }
                else if (rand.nextBoolean()) {
                    if (!data.getType(x, highest + 1, z).isSolid()) {
                        PlantBuilder.TALL_GRASS.build(data, x, highest + 1, z);
                    }
                }
            }
        }


        lowerCorner = room.getLowerCorner(5);
        upperCorner = room.getUpperCorner(5);
        // Place objects
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {

                if (GenUtils.chance(rand, 1, 70)) {
                    if (!spawnedWater && rand.nextBoolean()) { // Water
                        spawnedWater = true;
                        Wall core = new Wall(new SimpleBlock(data, x, 0, z), BlockUtils.getDirectBlockFace(rand));

                        //    		            if(!areaFailedTolerance) {
                        core = core.getGroundOrSeaLevel().getUp();
                        //    		            }
                        //    		            else
                        //    		            	core.getAtY(roomY+1);

                        if (Math.abs(core.getY() - roomY)
                            > TConfig.c.STRUCTURES_PLAINSVILLAGE_HEIGHT_TOLERANCE)
                        {
                            continue;
                        }

                        new StairBuilder(Material.COBBLESTONE_STAIRS).setHalf(Half.TOP)
                                                                     .setFacing(core.getDirection())
                                                                     .apply(core.getRear())
                                                                     .setFacing(core.getDirection().getOppositeFace())
                                                                     .apply(core.getFront(2))
                                                                     .setFacing(BlockUtils.getRight(core.getDirection()))
                                                                     .apply(core.getFront().getLeft())
                                                                     .apply(core.getLeft())
                                                                     .setFacing(BlockUtils.getLeft(core.getDirection()))
                                                                     .apply(core.getFront().getRight())
                                                                     .apply(core.getRight());

                        new SlabBuilder(Material.COBBLESTONE_SLAB).setWaterlogged(true)
                                                                  .apply(core)
                                                                  .apply(core.getFront());

                        core.getDown().downUntilSolid(new Random(), Material.DIRT);
                        core.getFront().getDown().downUntilSolid(new Random(), Material.DIRT);
                        break;
                    }
                    else { // Haybales
                        SimpleBlock core = new SimpleBlock(data, x, roomY, z).findAirPocket(15);
                        if (core == null) {
                            continue;
                        }

                        BlockUtils.replaceUpperSphere(x + 7 * z + 17 * 17,
                                1.5f,
                                2.5f,
                                1.5f,
                                core,
                                false,
                                Material.HAY_BLOCK
                        );
                        break;
                    }
                }
            }
        }

        // Spawn animals
        EntityType animal = farmAnimals[rand.nextInt(farmAnimals.length)];
        // Spawn animals
        int[] coords = new int[] {room.getX(), 0, room.getZ()};

        int highest;
        //            if(!areaFailedTolerance) {
        highest = GenUtils.getTrueHighestBlock(data, coords[0], coords[2]);
        //            }
        //            else
        //    	highest = roomY;

        int threshold = 0;
        while (data.getType(coords[0], highest + 1, coords[2]).isSolid() && threshold < 6) {
            threshold++;
            highest++;
        }
        if (threshold < 6) {
            if (Math.abs(highest - roomY) <= TConfig.c.STRUCTURES_PLAINSVILLAGE_HEIGHT_TOLERANCE) {
                for (int i = 0; i < GenUtils.randInt(3, 7); i++) {
                    data.addEntity(coords[0], highest + 1, coords[2], animal);
                }
            }
        }

        if (jobBlock != null) {
            switch (animal) {
                case PIG:
                case CHICKEN:
                    new DirectionalBuilder(Material.SMOKER).setFacing(((DirectionalCubeRoom) room).getDirection())
                                                           .apply(jobBlock);
                    break;
                case SHEEP:
                    jobBlock.setType(Material.LOOM);
                    break;
                case COW:
                case HORSE:
                    jobBlock.setType(Material.CAULDRON);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getWidthX() >= 15 && (room.getWidthX() < 18 || room.getWidthZ() < 18);
    }
}
