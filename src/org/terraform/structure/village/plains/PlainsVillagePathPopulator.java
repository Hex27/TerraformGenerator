package org.terraform.structure.village.plains;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.PathPopulatorAbstract;
import org.terraform.structure.room.PathPopulatorData;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.SlabBuilder;

import java.util.Collection;
import java.util.Random;

public class PlainsVillagePathPopulator extends PathPopulatorAbstract {
	TerraformWorld tw;
	private Random random;
	private Collection<DirectionalCubeRoom> knownRooms;
    public PlainsVillagePathPopulator(TerraformWorld tw, Collection<DirectionalCubeRoom> collection, Random rand) {
        this.tw = tw;
        this.random = rand;
        this.knownRooms = collection;
    }

    @Override
    public void populate(PathPopulatorData ppd) {
    	
    	//Find the ground level to place pathways
    	ppd.base = new SimpleBlock(
    			ppd.base.getPopData(),
    			ppd.base.getX(),
    			GenUtils.getHighestGround(ppd.base.getPopData(), ppd.base.getX(), ppd.base.getZ()),
    			ppd.base.getZ());

    	//Path is on water. Place a solid wooden foundation, and then return.
    	if(ppd.base.getY() < TerraformGenerator.seaLevel) {
    		Wall pathCore = new Wall(ppd.base,ppd.dir).getAtY(TerraformGenerator.seaLevel);
    		new SlabBuilder(Material.OAK_SLAB)
    		.setWaterlogged(true).setType(Type.TOP)
    		.apply(pathCore)
    		.apply(pathCore.getLeft())
    		.apply(pathCore.getRight());
    		
    		pathCore.getRelative(0,-1,0).downLPillar(random, 50, Material.OAK_LOG);
    		
    		return;
    	}
    	
    	if(GenUtils.chance(random, 1, 25)) {
    		BlockFace side = BlockUtils.getTurnBlockFace(random, ppd.dir);
    		SimpleBlock target = new SimpleBlock(
    		    			ppd.base.getPopData(),
    		    			ppd.base.getX()+side.getModX()*3,
    		    			GenUtils.getHighestGround(
    		    					ppd.base.getPopData(), 
    		    					ppd.base.getX()+side.getModX()*3, 
    		    					ppd.base.getZ()+side.getModZ()*3),
    		    			ppd.base.getZ()+side.getModZ()*3);
    		if(target.getType() == Material.GRASS_PATH) return;
    		for(BlockFace face:BlockUtils.xzPlaneBlockFaces) {
    			if(target.getRelative(face).getGround().getRelative(0,1,0).getType().isSolid())
    				return;
    		}
    		
    		for(CubeRoom room:knownRooms) {
    			if(room.isPointInside(target)) 
    				return;
    		}
    		
    		placeLamp(random, target.getRelative(0,1,0));
    	}
    }

    public static void placeLamp(Random rand, SimpleBlock b) {
        b.setType(GenUtils.randMaterial(rand, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS));
        b.getRelative(0, 1, 0).setType(GenUtils.randMaterial(rand, Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL));
        b.getRelative(0, 2, 0).setType(GenUtils.randMaterial(rand, Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL));
        b.getRelative(0, 3, 0).setType(GenUtils.randMaterial(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE));
        b.getRelative(0, 4, 0).setType(Material.CAMPFIRE);
        b.getRelative(0, 5, 0).setType(GenUtils.randMaterial(rand, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS));
        for (BlockFace face : BlockUtils.directBlockFaces) {
            Slab tSlab = (Slab) Bukkit.createBlockData(GenUtils.randMaterial(rand, Material.STONE_BRICK_SLAB, Material.MOSSY_STONE_BRICK_SLAB));
            tSlab.setType(Type.TOP);
            b.getRelative(face).getRelative(0, 3, 0).setBlockData(tSlab);
            b.getRelative(face).getRelative(0, 4, 0).setType(GenUtils.randMaterial(rand, Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL));
            b.getRelative(face).getRelative(0, 5, 0).setType(GenUtils.randMaterial(rand, Material.STONE_BRICK_SLAB, Material.MOSSY_STONE_BRICK_SLAB));
        }
    }
    
    @Override
    public int getPathWidth() {
        return 3;
    }
}
