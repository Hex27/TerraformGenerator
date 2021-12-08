package org.terraform.structure.village.plains.temple;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.StairwayBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.AbstractMap.SimpleEntry;
import java.util.Random;

public class PlainsVillageTempleEntrancePiece extends JigsawStructurePiece {

	PlainsVillagePopulator plainsVillagePopulator;
    public PlainsVillageTempleEntrancePiece(PlainsVillagePopulator plainsVillagePopulator, int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
        super(widthX, height, widthZ, type, validDirs);
        this.plainsVillagePopulator = plainsVillagePopulator;
    }

    @Override
    public void build(PopulatorDataAbstract data, Random rand) {
        Material[] stoneBricks = {Material.STONE_BRICKS, Material.STONE_BRICKS, Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS};

        SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
        Wall w = entry.getKey().getRelative(0, -1, 0);

        //Wall
        for (int i = 0; i < entry.getValue(); i++) {
            w.getRelative(0, -1, 0).downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
            w.Pillar(5, rand, stoneBricks);

            w = w.getLeft();
        }

        //Carve Doorway
        Wall core = new Wall(new SimpleBlock(data, this.getRoom().getX(), this.getRoom().getY() + 1, this.getRoom().getZ()), this.getRotation());
        core = core.getRear(2);
        BlockUtils.placeDoor(data, plainsVillagePopulator.woodDoor, core.getX(), core.getY(), core.getZ(), core.getDirection());

        //Stairway down
//        BlockUtils.angledStairwayUntilSolid(core.getFront().getRelative(0, -1, 0).get(), core.getDirection(),
//                new Material[]{
//                        Material.COBBLESTONE, Material.MOSSY_COBBLESTONE
//                },
//                Material.COBBLESTONE_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS);

        if(core.getFront().getType().isSolid()) {
	        new StairwayBuilder(Material.COBBLESTONE_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS)
	        .setAngled(true)
	        .setStopAtWater(true)
	        .setStairwayDirection(BlockFace.UP)
	        .build(core.getFront(3));
	        core.getFront().Pillar(2, rand, Material.AIR);
	        core.getFront(2).Pillar(3, rand, Material.AIR);
        }else
	        new StairwayBuilder(Material.COBBLESTONE_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS)
	        .setAngled(true)
	        .setStopAtWater(true)
	        .build(core.getFront().getRelative(0, -1, 0));

        //Decorate Doorway with some details
        new StairBuilder(Material.STONE_BRICK_STAIRS)
                .setHalf(Half.TOP)
                .setFacing(core.getDirection().getOppositeFace())
                .apply(core.getFront().getRelative(0, 2, 0));
        core.getFront().getRelative(0, 3, 0).setType(Material.CHISELED_STONE_BRICKS);

        Wall doorAdj = core.getFront().getRight();
        new StairBuilder(Material.STONE_BRICK_STAIRS)
                .setFacing(doorAdj.getDirection().getOppositeFace())
                .apply(doorAdj.getRelative(0, 1, 0));
        doorAdj.downUntilSolid(rand, stoneBricks);

        doorAdj = core.getFront().getLeft();
        new StairBuilder(Material.STONE_BRICK_STAIRS)
                .setFacing(doorAdj.getDirection().getOppositeFace())
                .apply(doorAdj.getRelative(0, 1, 0));
        doorAdj.downUntilSolid(rand, stoneBricks);


    }

}
