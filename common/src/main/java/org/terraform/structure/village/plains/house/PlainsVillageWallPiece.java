package org.terraform.structure.village.plains.house;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.TrapDoor;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.AbstractMap.SimpleEntry;
import java.util.Random;

public class PlainsVillageWallPiece extends JigsawStructurePiece {

    final PlainsVillageHouseVariant var;
    final PlainsVillagePopulator plainsVillagePopulator;
    public PlainsVillageWallPiece(PlainsVillagePopulator plainsVillagePopulator, PlainsVillageHouseVariant var, int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
        super(widthX, height, widthZ, type, validDirs);
        this.var = var;
        this.plainsVillagePopulator = plainsVillagePopulator;

    }

    @Override
    public void build(@NotNull PopulatorDataAbstract data, @NotNull Random rand) {

        SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
        Wall w = entry.getKey().getRelative(0, -1, 0);
        for (int i = 0; i < entry.getValue(); i++) {
            w.getRelative(0, -1, 0).downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
            w.Pillar(2, rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);

            if (this.var == PlainsVillageHouseVariant.CLAY)
                w.getRelative(0, 2, 0).Pillar(2, rand, Material.WHITE_TERRACOTTA);
            else
                w.getRelative(0, 2, 0).Pillar(2, rand, plainsVillagePopulator.woodPlank);

            w = w.getLeft();
        }
        w.getRight(2).getRelative(0, 2, 0).setType(plainsVillagePopulator.woodLog);
        w.getRight(3).getRelative(0, 2, 0).setType(Material.GLASS_PANE);
        w.getRight(4).getRelative(0, 2, 0).setType(plainsVillagePopulator.woodLog);
        BlockUtils.correctSurroundingMultifacingData(w.getRight(3).getRelative(0, 2, 0).get());

        w = w.getRight(3).getFront().getRelative(0, 1, 0);

        //Variant Wooden
        Material[] slabType = {plainsVillagePopulator.woodSlab};
        Material[] fenceType = {plainsVillagePopulator.woodFence};
        Material[] baseType = {Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.CRACKED_STONE_BRICKS};
        Material[] stairType = {Material.COBBLESTONE_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS};

        //Variant Cobblestone
        if (var == PlainsVillageHouseVariant.COBBLESTONE) {
            slabType = new Material[]{Material.COBBLESTONE_SLAB, Material.MOSSY_COBBLESTONE_SLAB};
            fenceType = new Material[]{Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL};
            baseType = new Material[]{plainsVillagePopulator.woodLog};
            stairType = new Material[]{plainsVillagePopulator.woodStairs};
        } else if (var == PlainsVillageHouseVariant.CLAY) {
            slabType = BlockUtils.stoneBrickSlabs;
            fenceType = new Material[]{Material.STONE_BRICK_WALL, Material.MOSSY_STONE_BRICK_WALL};
            baseType = new Material[]{plainsVillagePopulator.woodStrippedLog};
            stairType = new Material[]{plainsVillagePopulator.woodStairs};
        }

        new SlabBuilder(slabType)
                .setType(Slab.Type.TOP)
                .apply(w.getRelative(0, 2, 0));

        new SlabBuilder(slabType)
                .setType(Slab.Type.BOTTOM)
                .apply(w.getRelative(0, 2, 0).getLeft())
                .apply(w.getRelative(0, 2, 0).getRight());

        w.getRelative(0, 1, 0).getLeft().setType(fenceType);
        w.getRelative(0, 1, 0).getLeft().CorrectMultipleFacing(1);
        w.getRelative(0, 1, 0).getRight().setType(fenceType);
        w.getRelative(0, 1, 0).getRight().CorrectMultipleFacing(1);

        w.getLeft().setType(baseType);
        w.getRight().setType(baseType);

        if (new Random().nextBoolean()) { //Plants
            w.setType(Material.GRASS_BLOCK);
            TrapDoor trapdoor = (TrapDoor) Bukkit.createBlockData(plainsVillagePopulator.woodTrapdoor);
            trapdoor.setFacing(w.getDirection());
            trapdoor.setOpen(true);
            w.getFront().setBlockData(trapdoor);
            w.getRelative(0, 1, 0).setType(BlockUtils.pickFlower());
        } else { //Stairs
            new StairBuilder(stairType).setFacing(w.getDirection().getOppositeFace())
                    .apply(w);
        }

        w.getLeft().getRelative(0, -1, 0).downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
        w.getRight().getRelative(0, -1, 0).downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
        w.getRelative(0, -1, 0).downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);

    }

}
