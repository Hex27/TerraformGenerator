package org.terraform.structure.village.plains;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.village.VillagePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.WoodUtils;
import org.terraform.utils.WoodUtils.WoodType;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class PlainsVillagePopulator extends VillagePopulator {
    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {
        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());
        int[] coords = mc.getCenterBiomeSectionBlockCoords(); //getCoordsFromMegaChunk(tw, mc);
        int x = coords[0];//data.getChunkX()*16 + random.nextInt(16);
        int z = coords[1];//data.getChunkZ()*16 + random.nextInt(16);
        //Height set to 50 as plains village will settle its own height.
        spawnPlainsVillage(tw, this.getHashedRandom(tw, data.getChunkX(), data.getChunkZ()), data, x, 50, z);
    }
    
    /**
     * 
     * @param data
     * @param x
     * @param y
     * @param z
     * @return new location where farmhouse has enough space to spawn
     */
    private void ensureFarmHouseEntrance(Random rand, DirectionalCubeRoom room, PopulatorDataAbstract data) {
    	int frontSpaceGuarantee = 11;
    	Wall w = new Wall(new SimpleBlock(data,room.getX(),room.getY(),room.getZ()).getGround(), room.getDirection())
    			.getRelative(0,4,0);
    	
    	int elevation = GenUtils.randInt(rand, 2,4);
    	int max = 30;
    	while(max > 0 && !isFrontSpaceClear(w,frontSpaceGuarantee)) {
    		switch(rand.nextInt(2+1)) {
    		case 0: //Move the house forward
        		w = w.getFront().getGround().getRelative(0,elevation,0);
        		break;
    		case 1: //Turn the house
        		w = new Wall(w.get(),BlockUtils.getTurnBlockFace(rand, w.getDirection()));
        		break;
    		case 2: 
    			elevation+=2; //elevate more
    			break;
    		}
    		max--;
    	}
    	
    	if(max == 0) { //Maybe it's a mountain or stuck in the middle of eroded plains
    		TerraformGeneratorPlugin.logger.info("Village at " + w.get().getVector() + " may have a weird spawn.");
    	}
    	
    	room.setX(w.getX());
    	room.setZ(w.getZ());
    	room.setDirection(w.getDirection());
    	((PlainsVillageTownhallPopulator) room.getPop()).setElevation(elevation);
    }
    
    private boolean isFrontSpaceClear(Wall w, int space) {
    	for(int i = 0; i < space; i++) {
    		if(w.getFront(i).getType().isSolid())
    			return false;
    	}
    	return true;
    }
    
    public Material woodSlab = Material.OAK_SLAB;
    public Material woodPlank = Material.OAK_PLANKS;
    public Material woodLog = Material.OAK_LOG;
    public Material woodStrippedLog = Material.STRIPPED_OAK_LOG;
    public Material woodFence = Material.OAK_FENCE;
    public Material woodButton = Material.OAK_BUTTON;
    public Material woodTrapdoor = Material.OAK_TRAPDOOR;
    public Material woodDoor = Material.OAK_DOOR;
    public Material woodStairs = Material.OAK_STAIRS;
    public Material woodLeaves = Material.OAK_LEAVES;
    public Material woodPressurePlate = Material.OAK_PRESSURE_PLATE;
    public String wood = "oak_";

    public void spawnPlainsVillage(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z) {
        
    	
    	BlockFace pathStart = BlockUtils.getDirectBlockFace(random);
        TerraformGeneratorPlugin.logger.info("Spawning plains village at " + x + "," + y + "," + z);
        DirectionalCubeRoom townHall = new DirectionalCubeRoom(pathStart, 24, 24, 24, x, y, z);
        PlainsVillageTownhallPopulator townHallPop = new PlainsVillageTownhallPopulator(tw, random, false, false);
        townHall.setRoomPopulator(townHallPop);

        ensureFarmHouseEntrance(random, townHall, data);
        pathStart = townHall.getDirection();
        
        BiomeBank biome = tw.getBiomeBank(townHall.getX(), townHall.getZ());
        woodSlab = WoodUtils.getWoodForBiome(biome, WoodType.SLAB);
        woodPlank = WoodUtils.getWoodForBiome(biome, WoodType.PLANKS);
        woodLog = WoodUtils.getWoodForBiome(biome, WoodType.LOG);
        woodStairs = WoodUtils.getWoodForBiome(biome, WoodType.STAIRS);
        woodFence = WoodUtils.getWoodForBiome(biome, WoodType.FENCE);
        woodStrippedLog = WoodUtils.getWoodForBiome(biome, WoodType.STRIPPED_LOG);
        woodButton = WoodUtils.getWoodForBiome(biome, WoodType.BUTTON);
        woodTrapdoor = WoodUtils.getWoodForBiome(biome, WoodType.TRAPDOOR);
        woodPressurePlate = WoodUtils.getWoodForBiome(biome, WoodType.PRESSURE_PLATE);
        woodDoor = WoodUtils.getWoodForBiome(biome, WoodType.DOOR);
        woodLeaves = WoodUtils.getWoodForBiome(biome, WoodType.LEAVES);
        wood = woodLeaves.toString().toLowerCase().replace("leaves","");
        
        //Re-get x and z because they change after ensureFarmHouseEntrance.
        //13 because that's the width of the townhall.
        PlainsPathRecursiveSpawner spawner = new PlainsPathRecursiveSpawner(
                new SimpleBlock(data, townHall.getX() + pathStart.getModX() * 13, y, townHall.getZ() + pathStart.getModZ() * 13),
                100, BlockUtils.getAdjacentFaces(pathStart));
        spawner.forceRegisterRoom(townHall);
        spawner.setVillageDensity(0.7);
        spawner.setPathPop(new PlainsVillagePathPopulator(tw, spawner.getRooms().values(), random));
        spawner.registerRoomPopulator(new PlainsVillageStandardHousePopulator(this, random, false, false));
        spawner.registerRoomPopulator(new PlainsVillageTemplePopulator(this, random, false, true));
        spawner.registerRoomPopulator(new PlainsVillageForgePopulator(this, random, false, true));
        spawner.registerRoomPopulator(new PlainsVillageCropFarmPopulator(this, random, false, false));
        spawner.registerRoomPopulator(new PlainsVillageAnimalPenPopulator(this, random, false, false));
        spawner.registerRoomPopulator(new PlainsVillageWellPopulator(this, random, false, false));
        spawner.registerRoomPopulator(new PlainsVillageFountainPopulator(this, random, false, true));
        spawner.registerRoomPopulator(new PlainsVillagePondPopulator(random, false, false));

        spawner.generate(random);
        spawner.build(random);
    }

}
