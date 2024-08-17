package org.terraform.structure.pillager.mansion.secondfloor;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.pillager.mansion.MansionInternalWallState;
import org.terraform.structure.pillager.mansion.MansionRoomPopulator;
import org.terraform.structure.pillager.mansion.MansionRoomSchematicParser;
import org.terraform.structure.pillager.mansion.MansionRoomSize;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.GenUtils;

public class MansionSecondFloorLoungePopulator extends MansionRoomPopulator {

	public MansionSecondFloorLoungePopulator(CubeRoom room, HashMap<BlockFace, MansionInternalWallState> internalWalls) {
		super(room, internalWalls);
	}

	//Refers to the kitchen room width, not the width of one room cell.
	private static final int roomWidthX = 6;
	private static final int roomWidthZ = 15;
	@Override
	public void decorateRoom(@NotNull PopulatorDataAbstract data, @NotNull Random random) {
		int[] lowerBounds = this.getRoom().getLowerCorner(1);
		BlockFace randomFace = new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH}[random.nextInt(2)];
		TerraformGeneratorPlugin.logger.info("Lounge at " + this.getRoom().getSimpleLocation() + " picking face: " + randomFace);
		try {
			if(randomFace == BlockFace.NORTH) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0], this.getRoom().getY(), lowerBounds[1]);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-lounge", target);
		        schema.setFace(randomFace);
		        schema.parser = new MansionLoungeSchematicParser(random, data);
		        schema.apply();
			}else if(randomFace == BlockFace.SOUTH) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0]+roomWidthX, this.getRoom().getY(), lowerBounds[1]+roomWidthZ);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-lounge", target);
		        schema.setFace(randomFace);
		        schema.parser = new MansionLoungeSchematicParser(random, data);
		        schema.apply();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private class MansionLoungeSchematicParser extends MansionRoomSchematicParser
	{
		private Material terracottaType;
		public MansionLoungeSchematicParser(@NotNull Random rand, PopulatorDataAbstract pop) {
			super(rand, pop);
			terracottaType = GenUtils.randMaterial(rand,
            		Material.WHITE_GLAZED_TERRACOTTA,
            		Material.ORANGE_GLAZED_TERRACOTTA,
            		Material.MAGENTA_GLAZED_TERRACOTTA,
            		Material.LIGHT_BLUE_GLAZED_TERRACOTTA,
            		Material.YELLOW_GLAZED_TERRACOTTA,
            		Material.LIME_GLAZED_TERRACOTTA,
            		Material.PINK_GLAZED_TERRACOTTA,
            		Material.GRAY_GLAZED_TERRACOTTA,
            		Material.LIGHT_GRAY_GLAZED_TERRACOTTA,
            		Material.CYAN_GLAZED_TERRACOTTA,
            		Material.PURPLE_GLAZED_TERRACOTTA,
            		Material.BLUE_GLAZED_TERRACOTTA,
            		Material.BROWN_GLAZED_TERRACOTTA,
            		Material.GREEN_GLAZED_TERRACOTTA,
            		Material.RED_GLAZED_TERRACOTTA,
            		Material.BLACK_GLAZED_TERRACOTTA
            		);
		}
		
	    @Override
	    public void applyData(@NotNull SimpleBlock block, @NotNull BlockData data) {
	        if (data.getMaterial() == Material.BLACK_GLAZED_TERRACOTTA) {
	            data = Bukkit.createBlockData(
	                    data.getAsString().replaceAll(
	                            "black_glazed_terracotta",
	                            terracottaType
	                                    .toString().toLowerCase(Locale.ENGLISH)
	                    )
	            );
	            super.applyData(block, data);
	        } else {
	            super.applyData(block, data);
	        }
	    }
	}

	@Override
	public void decorateExit(Random rand, @NotNull Wall w) {
		w.getRelative(0,6,0).setType(Material.DARK_OAK_PLANKS);
	}
	
	@Override
	public void decorateWindow(Random rand, @NotNull Wall w) {
		for(int i = 0; i <= 3; i++) {
			w.getLeft(i).setType(Material.POLISHED_DIORITE);
			if(!w.getLeft(i).getFront().getType().isSolid()
					|| w.getLeft(i).getFront().getType() == Material.POLISHED_ANDESITE_STAIRS)
				w.getLeft(i).getFront().setType(Material.POLISHED_ANDESITE);
			
			w.getRight(i).setType(Material.POLISHED_DIORITE);
			if(!w.getRight(i).getFront().getType().isSolid()
					|| w.getRight(i).getFront().getType() == Material.POLISHED_ANDESITE_STAIRS)
				w.getRight(i).getFront().setType(Material.POLISHED_ANDESITE);
		}
		
		if(!w.getRight(4).getFront().getType().isSolid()
					|| w.getRight(4).getFront().getType() == Material.POLISHED_ANDESITE_STAIRS)
		{
			w.getRight(4).Pillar(6, Material.DARK_OAK_LOG);
		}
		if(!w.getLeft(4).getFront().getType().isSolid()
				|| w.getLeft(4).getFront().getType() == Material.POLISHED_ANDESITE_STAIRS)
	{
		w.getLeft(4).Pillar(6, Material.DARK_OAK_LOG);
	}
	}
	
	@Override
	public void decorateWall(Random rand, @NotNull Wall w) {
		for(int i = 0; i <= 3; i++) {
			w.getLeft(i).setType(Material.POLISHED_ANDESITE);
			
			w.getRight(i).setType(Material.POLISHED_ANDESITE);
		}
	}

	@Override
	public @NotNull MansionRoomSize getSize() {
		return new MansionRoomSize(1,2);
	}
}
