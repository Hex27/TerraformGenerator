package org.terraform.structure.pillager.mansion.secondfloor;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab.Type;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.pillager.mansion.MansionInternalWallState;
import org.terraform.structure.pillager.mansion.MansionRoomPopulator;
import org.terraform.structure.pillager.mansion.MansionRoomSchematicParser;
import org.terraform.structure.pillager.mansion.MansionRoomSize;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BannerUtils;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.PaintingUtils;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

public class MansionSecondFloorBedroomPopulator extends MansionRoomPopulator {

	public MansionSecondFloorBedroomPopulator(CubeRoom room, HashMap<BlockFace, MansionInternalWallState> internalWalls) {
		super(room, internalWalls);
	}

	//Refers to the bedroom room width, not the width of one room cell.
	private static final int roomWidth = 15;
	@Override
	public void decorateRoom(@NotNull PopulatorDataAbstract data, @NotNull Random random) {
		int[] lowerBounds = this.getRoom().getLowerCorner(1);
		BlockFace randomFace = BlockUtils.getDirectBlockFace(random);
		//TerraformGeneratorPlugin.logger.info("Library picking face: " + randomFace);
		try {
			if(randomFace == BlockFace.NORTH) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0], this.getRoom().getY(), lowerBounds[1]);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-bedroom", target);
		        schema.parser = new MansionRoomSchematicParser(random, data);
				schema.setFace(randomFace);
		        schema.apply();
			}else if(randomFace == BlockFace.SOUTH) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0]+roomWidth, this.getRoom().getY(), lowerBounds[1]+roomWidth);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-bedroom", target);
		        schema.parser = new MansionRoomSchematicParser(random, data);
		        schema.setFace(randomFace);
		        schema.apply();
			}else if(randomFace == BlockFace.EAST) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0]+roomWidth, this.getRoom().getY(), lowerBounds[1]);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-bedroom", target);
		        schema.parser = new MansionRoomSchematicParser(random, data);
		        schema.setFace(randomFace);
		        schema.apply();
			}else if(randomFace == BlockFace.WEST) {
				SimpleBlock target = new SimpleBlock(data, lowerBounds[0], this.getRoom().getY(), lowerBounds[1]+roomWidth);
				TerraSchematic schema = TerraSchematic.load("mansion/mansion-bedroom", target);
				schema.parser = new MansionRoomSchematicParser(random, data);
		        schema.setFace(randomFace);
		        schema.apply();
			}
		} catch (FileNotFoundException e) {	
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void decorateEntrance(Random rand, Wall w) {
		w = w.getRear();
		w.getLeft(2).Pillar(5, Material.DARK_OAK_PLANKS);
		w.getRight(2).Pillar(5, Material.DARK_OAK_PLANKS);
		w.getLeft(2).Pillar(3, Material.DARK_OAK_LOG);
		w.getRight(2).Pillar(3, Material.DARK_OAK_LOG);
		
		w.getLeft().getRelative(0,5,0).downPillar(2, Material.DARK_OAK_PLANKS);
		w.getRight().getRelative(0,5,0).downPillar(2, Material.DARK_OAK_PLANKS);
		w.getRelative(0,5,0).downPillar(2, Material.DARK_OAK_PLANKS);
		new StairBuilder(Material.DARK_OAK_STAIRS)
		.setFacing(w.getDirection()).setHalf(Half.TOP)
		.apply(w.getRelative(0,4,0))
		.setFacing(BlockUtils.getLeft(w.getDirection()))
		.apply(w.getRelative(0,3,0).getLeft())
		.setFacing(BlockUtils.getRight(w.getDirection()))
		.apply(w.getRelative(0,3,0).getRight());
	}
	
	@Override
	public void decorateWall(@NotNull Random rand, @NotNull Wall w) {
		int choice = rand.nextInt(2);
		switch(choice) {
		case 0: //Andesite table with banner and lectern
			w.getLeft(3).Pillar(6, Material.DARK_OAK_LOG);
			w.getRight(3).Pillar(6, Material.DARK_OAK_LOG);
			new StairBuilder(Material.POLISHED_ANDESITE_STAIRS)
			.setHalf(Half.TOP)
			.setFacing(BlockUtils.getLeft(w.getDirection()))
			.apply(w.getLeft(2))
			.apply(w.getLeft(2).getRelative(0,4,0))
			.apply(w.getLeft(2).getRelative(0,5,0))
			.apply(w.getLeft().getRelative(0,5,0))
			.setFacing(BlockUtils.getRight(w.getDirection()))
			.apply(w.getRight(2))
			.apply(w.getRight(2).getRelative(0,4,0))
			.apply(w.getRight(2).getRelative(0,5,0))
			.apply(w.getRight().getRelative(0,5,0));
			
			w.getRelative(0,5,0).setType(Material.POLISHED_ANDESITE);
			BannerUtils.generatePillagerBanner(w.getRelative(0,3,0).get(), w.getDirection(), true);
			
			new DirectionalBuilder(Material.LECTERN)
			.setFacing(w.getDirection()).apply(w);
			
			new SlabBuilder(Material.POLISHED_ANDESITE_SLAB)
			.setType(Type.TOP)
			.apply(w.getLeft())
			.apply(w.getRight());
			break;
		default://Table
			table(rand, w.getLeft(2));
			table(rand, w.getRight(2));
			break;
		}
	}
	
	@Override
	public void decorateWindow(@NotNull Random rand, @NotNull Wall w) {
		int choice = rand.nextInt(2);
		switch(choice) {
		case 0: //Table with flowers
			new StairBuilder(Material.POLISHED_ANDESITE_STAIRS)
			.setHalf(Half.TOP)
			.setFacing(BlockUtils.getLeft(w.getDirection()))
			.apply(w.getLeft(2))
			.setFacing(BlockUtils.getRight(w.getDirection()))
			.apply(w.getRight(2));
			
			new SlabBuilder(Material.POLISHED_ANDESITE_SLAB)
			.setType(Type.TOP)
			.apply(w)
			.apply(w.getLeft())
			.apply(w.getRight());
			
			w.getRelative(0,1,0).setType(BlockUtils.pickPottedPlant());
			w.getLeft().getRelative(0,1,0).setType(BlockUtils.pickPottedPlant());
			w.getRight().getRelative(0,1,0).setType(BlockUtils.pickPottedPlant());
			break;
		default://Utility Block
			w.setType(
					Material.CRAFTING_TABLE, Material.FLETCHING_TABLE, 
					Material.CARTOGRAPHY_TABLE, Material.ENCHANTING_TABLE, 
					Material.BREWING_STAND, Material.ANVIL,
					Material.NOTE_BLOCK, Material.JUKEBOX);
			break;
		}
	};

	private void table(@NotNull Random rand, @NotNull Wall w) {
		w.getLeft().getRear().Pillar(6, Material.DARK_OAK_LOG);
		w.getRight().getRear().Pillar(6, Material.DARK_OAK_LOG);
		
		w.getLeft().setType(Material.STRIPPED_DARK_OAK_LOG);
		w.getRight().setType(Material.STRIPPED_DARK_OAK_LOG);
		new SlabBuilder(Material.DARK_OAK_SLAB)
		.setType(Type.TOP)
		.apply(w);
		
		w.getRelative(0,1,0).setType(Material.BROWN_CARPET);
		w.getLeft().getRelative(0,1,0).setType(Material.BROWN_CARPET);
		w.getRight().getRelative(0,1,0).setType(Material.BROWN_CARPET);
		
		PaintingUtils.placePainting(
				w.getRelative(0,2,0).get(),
				w.getDirection(), 
				PaintingUtils.getArtFromDimensions(rand, 1, 2));
	}
	

	@Override
	public @NotNull MansionRoomSize getSize() {
		return new MansionRoomSize(2,2);
	}
}
