package org.terraform.structure.village.plains;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.block.data.type.Lantern;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfigOption;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.AgeableBuilder;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.blockdata.TrapdoorBuilder;
import org.terraform.utils.version.OneOneSixBlockHandler;
import org.terraform.utils.version.Version;

import java.util.Map.Entry;
import java.util.Random;

public class PlainsVillageCropFarmPopulator extends PlainsVillageAbstractRoomPopulator {

	private static final Material[] crops = {
            Material.WHEAT,
            Material.CARROTS,
            Material.POTATOES,
            Material.BEETROOTS,
            Material.PUMPKIN_STEM,
            Material.MELON_STEM
    };

	private PlainsVillagePopulator plainsVillagePopulator;
    public PlainsVillageCropFarmPopulator(PlainsVillagePopulator plainsVillagePopulator, Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
        this.plainsVillagePopulator = plainsVillagePopulator;
    }

    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
    	int roomY = super.calculateRoomY(data, room);
    	boolean areaFailedTolerance = super.doesAreaFailTolerance(data, room);
    	
    	//If terrain is adverse, don't bother.
    	if(areaFailedTolerance)
    		return;
    	
    	super.populate(data, room);
    	
        BlockFace dir = ((DirectionalCubeRoom) room).getDirection();
        boolean hasScareCrow = false;
        int pad = GenUtils.randInt(1, 3);

        for (Entry<Wall, Integer> entry : room.getFourWalls(data, pad).entrySet()) {
            Wall w;
            
//            if(!areaFailedTolerance) {
        	w = entry.getKey().getGroundOrSeaLevel().getRelative(0, 1, 0);
//            }
//            else
        	//w = entry.getKey().getAtY(roomY).getRelative(0, 1, 0);
            
            for (int i = 0; i < entry.getValue(); i++) {
            	//Added height tolerance check. Don't place anything on areas that deviate too far off.
            	if(Math.abs(w.getY()-roomY) <= TConfigOption.STRUCTURES_PLAINSVILLAGE_HEIGHT_TOLERANCE.getInt())
	                if (w.getDirection().getOppositeFace() == dir) { //Entrance
	
	                    if (i <= 1 || i >= entry.getValue() - 1) {
	                        w.setType(plainsVillagePopulator.woodLog);
	                        w.getRelative(0,-1,0).downUntilSolid(rand, plainsVillagePopulator.woodLog);
	                        if (i == 1 || i == entry.getValue() - 1) {
	                            new TrapdoorBuilder(plainsVillagePopulator.woodTrapdoor)
	                                    .setFacing(dir)
	                                    .setOpen(true)
	                                    .setHalf(Half.BOTTOM)
	                                    .apply(w.getRear());
	
	                            w.getRelative(0, 1, 0).setType(plainsVillagePopulator.woodLeaves);
	                        }
	                    } else if (i == 2 || i == entry.getValue() - 2) {
	                        w.setType(plainsVillagePopulator.woodFence);
	                        
	                        w.getRelative(0,-1,0).downUntilSolid(rand, plainsVillagePopulator.woodLog);
	                        
	                        w.CorrectMultipleFacing(1);
	                        w.getRelative(0, 1, 0).setType(Material.TORCH);
	
	                    } else if (i == entry.getValue() / 2)
	                        w.setType(Material.COMPOSTER);
	
	
	                } else { //Farm Walls
	                    w.downUntilSolid(rand, plainsVillagePopulator.woodLog);
	                    if (i % 3 == 0) {
	                        w.getRelative(0, 1, 0).setType(plainsVillagePopulator.woodLeaves);
	                    } else {
	                        w.getRelative(0, 1, 0).setType(plainsVillagePopulator.woodFence);
	                        w.getRelative(0, 1, 0).CorrectMultipleFacing(1);
	
	                        //Chance to spawn overhanging lamp
	                        if (i > 1 && i < entry.getValue()-2 && GenUtils.chance(rand, 1, 13)) {
	                            int lampHeight = GenUtils.randInt(rand, 4, 6);
	                            w.getRelative(0, 2, 0).Pillar(lampHeight, rand, plainsVillagePopulator.woodFence);
	
	                            Wall lampWall = w.getRelative(0, 1 + lampHeight, 0).getFront();
	
	                            for (int j = 0; j < GenUtils.randInt(rand, 1, 2); j++) {
	                                lampWall.setType(plainsVillagePopulator.woodFence);
	                                lampWall.CorrectMultipleFacing(1);
	                                lampWall = lampWall.getFront();
	                            }
	                            lampWall = lampWall.getRear().getRelative(0, -1, 0);
	
	                            Material chain = Material.IRON_BARS;
	                            if (Version.isAtLeast(16.0))
	                                chain = OneOneSixBlockHandler.getChainMaterial();
	
	                            for (int j = 0; j < GenUtils.randInt(rand, 0, 1); j++) {
	                                lampWall.setType(chain);
	                                lampWall = lampWall.getRelative(0, -1, 0);
	                            }
	                            Lantern lantern = (Lantern) Bukkit.createBlockData(Material.LANTERN);
	                            lantern.setHanging(true);
	                            lampWall.setBlockData(lantern);
	                        }
	                    }
	                }


//                if(!areaFailedTolerance)
                w = w.getLeft().getGroundOrSeaLevel().getRelative(0, 1, 0);
//                else
                //w = w.getLeft().getAtY(roomY).getRelative(0, 1, 0);
            }
        }

        pad++;

        //Then go place the crops themselves.
        int[] lowerCorner = room.getLowerCorner(pad);
        int[] upperCorner = room.getUpperCorner(pad);
        Material crop = crops[rand.nextInt(crops.length)];

        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                int height;

//                if(!areaFailedTolerance) {
            	height = GenUtils.getHighestGroundOrSeaLevel(data, x, z);
                	
            	//Forget populating areas that are too far up/down
            	if(Math.abs(height-roomY) > TConfigOption.STRUCTURES_PLAINSVILLAGE_HEIGHT_TOLERANCE.getInt())
            		continue;
//                } else
//                	height = roomY;
                
                BlockUtils.setDownUntilSolid(x, height-1, z, data, Material.DIRT);
                
                if (x % 4 == 0 && z % 4 == 0) { //Water
                	for(BlockFace face:BlockUtils.directBlockFaces) {
                		data.setType(x+face.getModX(), height, z+face.getModZ(), Material.FARMLAND);
                		BlockUtils.setDownUntilSolid(x+face.getModX(), height-1, z+face.getModZ(), data, Material.DIRT);
                	}
                    data.setType(x, height, z, Material.WATER);
                } else if ((crop != Material.PUMPKIN_STEM && crop != Material.MELON_STEM)
                        || GenUtils.chance(rand, 1, 3)) {

                    if (GenUtils.chance(rand, 1, 30) && !hasScareCrow) { //Scarecrows

                        //Ensure enough space
                        if (x > lowerCorner[0] + 1 && x < upperCorner[0] - 1 && z > lowerCorner[1] + 1 && z < upperCorner[1] - 1) {
                            hasScareCrow = true;
                            setScareCrow(data, x, height + 1, z);
                        }
                    } else { //Farmlands
                        Farmland land = (Farmland) Bukkit.createBlockData(Material.FARMLAND);
                        land.setMoisture(7);
                        data.setBlockData(x, height, z, land);
                        new AgeableBuilder(crop)
                                .setRandomAge(rand)
                                .apply(data, x, height + 1, z);
                    }


                } else if (GenUtils.chance(rand, 1, 3)) {
                    data.setType(x, height, z, Material.DIRT);
                    Material block;
                    Material stem;
                    if (crop == Material.PUMPKIN_STEM) {
                        block = Material.PUMPKIN;
                        stem = Material.ATTACHED_PUMPKIN_STEM;
                    } else {
                        block = Material.MELON;
                        stem = Material.ATTACHED_MELON_STEM;
                    }

                    SimpleBlock target = new SimpleBlock(data, x, height + 1, z);
                    for (BlockFace near : BlockUtils.directBlockFaces) {
                        if (target.getRelative(near).getBlockData() instanceof Ageable) {
                            target.setType(block);
                            new DirectionalBuilder(stem)
                                    .setFacing(near.getOppositeFace())
                                    .apply(target.getRelative(near));
                            break;
                        }
                    }
                } else {
                    data.setType(x, height, z, Material.COARSE_DIRT);
                }

            }
        }

    }

    private void setScareCrow(PopulatorDataAbstract data, int x, int y, int z) {
        BlockFace facing = BlockUtils.getDirectBlockFace(rand);
        Wall w = new Wall(new SimpleBlock(data, x, y, z), facing);
        w.setType(Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
        w.getRelative(0, 1, 0).setType(plainsVillagePopulator.woodFence);
        w.getRelative(0, 2, 0).setType(plainsVillagePopulator.woodFence);
        w.getLeft().getRelative(0, 2, 0).setType(plainsVillagePopulator.woodFence);
        w.getRight().getRelative(0, 2, 0).setType(plainsVillagePopulator.woodFence);
        w.getRelative(0, 2, 0).CorrectMultipleFacing(1);

        new DirectionalBuilder(Material.CARVED_PUMPKIN, Material.JACK_O_LANTERN)
                .setFacing(facing)
                .apply(w.getRelative(0, 3, 0));

    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return room.getWidthX() >= 15 && (room.getWidthX() < 18 || room.getWidthZ() < 18);
    }
}
