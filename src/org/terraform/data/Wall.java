package org.terraform.data;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

public class Wall {
	
	private SimpleBlock block;
	private BlockFace direction;
	
	public Wall(SimpleBlock block, BlockFace dir){
		this.block = block;
		this.direction = dir;
	}
	
	public Wall clone(){
		return new Wall(block,direction);
	}
	
	public Wall getLeft(){
		return new Wall(block.getRelative(BlockUtils.getAdjacentFaces(direction)[0]), direction);
	}
	
	public Wall getRight(){
		return new Wall(block.getRelative(BlockUtils.getAdjacentFaces(direction)[1]), direction);
	}
	
	public SimpleBlock get(){
		return block;
	}
	
	public void setBlockData(BlockData d){
		this.block.setBlockData(d);
	}
	
	public Material getType(){
		return block.getType();
	}
	
	public void setType(Material type){
		block.setType(type);
	}

	public void Pillar(int height, Random rand, Material... types){
		for(int i = 0; i < height; i++){
			block.getRelative(0,i,0).setType(GenUtils.randMaterial(rand, types));
		}
	}
	
	public void LPillar(int height, Random rand, Material... types){
		for(int i = 0; i < height; i++){
			if(block.getRelative(0,i,0).getType().isSolid()) break;
			block.getRelative(0,i,0).setType(GenUtils.randMaterial(rand, types));
		}
	}
	
	public void RPillar(int height, Random rand, Material... types){
		for(int i = 0; i < height; i++){
			if(!block.getRelative(0,i,0).getType().isSolid())
				block.getRelative(0,i,0).setType(GenUtils.randMaterial(rand, types));
		}
	}
	
	public Wall getRear(){
		return new Wall(block.getRelative(direction.getOppositeFace()), direction);
	}
	
	public Wall getFront(){
		return new Wall(block.getRelative(direction), direction);
	}

	public BlockFace getDirection() {
		return direction;
	}
	
	public Wall getRelative(int x, int y, int z){
		return new Wall(block.getRelative(x,y,z), direction);
	}

}
