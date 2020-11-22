package org.terraform.structure.pyramid;

import java.util.Random;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;

public class EnchantmentAntechamber extends Antechamber {

    public EnchantmentAntechamber(Random rand, boolean forceSpawn, boolean unique) {
		super(rand, forceSpawn, unique);
		// TODO Auto-generated constructor stub
	}

	/***
     * Contains a variety of Pyramid item loot
     */
    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
    	super.populate(data, room);
    	
    	//Enchantment Table
    	data.setType(room.getX(), room.getY()+1, room.getZ(), Material.ENCHANTING_TABLE);
    	
    	SimpleBlock core = new SimpleBlock(data,room.getX(), room.getY()+1, room.getZ());
    	//Place shelves and Lecterns
    	HashMap<Wall,Integer> tableWalls = new HashMap<Wall,Integer>(){{
    		put(new Wall(core.getRelative(2,0,-2),BlockFace.SOUTH),5);
    		put(new Wall(core.getRelative(-2,0,2),BlockFace.NORTH),5);
    		put(new Wall(core.getRelative(2,0,2),BlockFace.WEST),5);
    		put(new Wall(core.getRelative(-2,0,-2),BlockFace.EAST),5);
    	}};
    	
    	for(Entry<Wall,Integer> entry:tableWalls.entrySet()) {
    		Wall w = entry.getKey();
    		for(int i = 0; i < entry.getValue(); i++) {
    			
    			if(i % 2 == 0) {
    				int h = 1;
    				if(i == 2) h = 2;
    				
    				w.LPillar(h, rand, Material.BOOKSHELF);
    				w.getRelative(0,room.getHeight()-2,0).downLPillar(rand, h, Material.BOOKSHELF);
    				w.RPillar(room.getHeight(), rand, Material.SANDSTONE_WALL);
    			}else {
    				Directional decor = (Directional) Bukkit.createBlockData(Material.LECTERN);
    				decor.setFacing(w.getDirection());
    				w.setBlockData(decor);
    			}
    			
    			w = w.getLeft();
    		}
    		
    	}
    }
    
    @Override
    public boolean canPopulate(CubeRoom room) {
        return room.getWidthX() > 7 && room.getWidthZ() > 7;
    }
	
}
