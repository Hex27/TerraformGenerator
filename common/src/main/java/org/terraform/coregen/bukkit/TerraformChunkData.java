package org.terraform.coregen.bukkit;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.ChunkGenerator.ChunkData;
import org.bukkit.material.MaterialData;
import org.terraform.coregen.populatordata.IPopulatorDataBaseHeightAccess;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataICAAbstract;
import org.terraform.main.TerraformGeneratorPlugin;

public class TerraformChunkData implements ChunkData {

	private PopulatorDataAbstract popData;
	public TerraformChunkData(PopulatorDataAbstract popData)
	{
		this.popData = popData;
	}
	
	private static boolean debug = true;
	public int getBaseHeight(int x, int z) {
		int height = -64;
		if(popData instanceof IPopulatorDataBaseHeightAccess) {
			height = ((IPopulatorDataBaseHeightAccess) popData).getBaseHeight(x + (popData.getChunkX()*16), z + (popData.getChunkZ()*16));
		}
		return height;
	}
	
	@Override
	public Biome getBiome(int x, int y, int z) {
		return popData.getBiome(x + (popData.getChunkX()*16), z + (popData.getChunkZ()*16));
	}

	@Override
	public BlockData getBlockData(int x, int y, int z) {
		return popData.getBlockData(x + (popData.getChunkX()*16), y, z + (popData.getChunkZ()*16));
	}

	@Override
	public byte getData(int x, int y, int z) {
		throw new UnsupportedOperationException("getData was called on TerraformChunkData!");
	}

	@Override
	public int getMaxHeight() {
		return popData.getTerraformWorld().maxY;
	}

	@Override
	public int getMinHeight() {
		return popData.getTerraformWorld().minY;
	}

	@Override
	public Material getType(int x, int y, int z) {
		return popData.getType(x + (popData.getChunkX()*16), y, z + (popData.getChunkZ()*16));
	}

	@Override
	public MaterialData getTypeAndData(int x, int y, int z) {
		throw new UnsupportedOperationException("getTypeAndData was called on TerraformChunkData with MaterialData!");
	}

	@Override
	public void setBlock(int x, int y, int z, Material arg3) {
		//TerraformGeneratorPlugin.logger.info("Called setBlock at " + x + "," + y + "," + z);
		popData.setType(x + (popData.getChunkX()*16), y, z + (popData.getChunkZ()*16), arg3);
	}

	@Override
	public void setBlock(int x, int y, int z, MaterialData arg3) {
		throw new UnsupportedOperationException("setBlock was called on TerraformChunkData with MaterialData!");
	}

	@Override
	public void setBlock(int x, int y, int z, BlockData arg3) {
		popData.setBlockData(x + (popData.getChunkX()*16), y, z + (popData.getChunkZ()*16), arg3);
	}

	@Override
	public void setRegion(int x, int y, int z,int x2, int y2, int z2, Material arg6) {
		throw new UnsupportedOperationException("setRegion was called on TerraformChunkData!");
	}

	@Override
	public void setRegion(int x, int y, int z, int x2, int y2, int z2, MaterialData arg6) {
		throw new UnsupportedOperationException("setRegion was called on TerraformChunkData with MaterialData!");
	}

	@Override
	public void setRegion(int x, int y, int z, int x2, int y2, int z2, BlockData arg6) {
		throw new UnsupportedOperationException("setRegion was called on TerraformChunkData!");
	}

}
