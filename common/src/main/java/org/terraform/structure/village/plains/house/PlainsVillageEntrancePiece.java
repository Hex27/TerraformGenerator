package org.terraform.structure.village.plains.house;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.Slab;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.StairwayBuilder;
import org.terraform.utils.blockdata.SlabBuilder;

import java.util.AbstractMap.SimpleEntry;
import java.util.Random;

public class PlainsVillageEntrancePiece extends JigsawStructurePiece {

    final PlainsVillageHouseVariant var;
    final PlainsVillagePopulator plainsVillagePopulator;

    public PlainsVillageEntrancePiece(PlainsVillagePopulator plainsVillagePopulator,
                                      PlainsVillageHouseVariant var,
                                      int widthX,
                                      int height,
                                      int widthZ,
                                      JigsawType type,
                                      BlockFace[] validDirs)
    {
        super(widthX, height, widthZ, type, validDirs);
        this.var = var;
        this.plainsVillagePopulator = plainsVillagePopulator;

    }

    @Override
    public void build(@NotNull PopulatorDataAbstract data, @NotNull Random rand) {

        SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, getRotation().getOppositeFace(), 0);
        Wall w = entry.getKey().getDown();
        for (int i = 0; i < entry.getValue(); i++) {
            w.getDown().downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
            w.Pillar(2, rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);

            if (this.var == PlainsVillageHouseVariant.CLAY) {
                w.getUp(2).Pillar(2, rand, Material.WHITE_TERRACOTTA);
            }
            else {
                w.getUp(2).Pillar(2, rand, plainsVillagePopulator.woodPlank);
            }

            w = w.getLeft();
        }

        // The door
        w = w.getRight(3).getUp();
        BlockUtils.placeDoor(data,
                plainsVillagePopulator.woodDoor,
                w.getX(),
                w.getY(),
                w.getZ(),
                w.getDirection().getOppositeFace()
        );


        if (w.getFront().isSolid()) {
            new StairwayBuilder(Material.COBBLESTONE_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS).setAngled(true)
                                                                                               .setStopAtWater(true)
                                                                                               .setStairwayDirection(
                                                                                                       BlockFace.UP)
                                                                                               .build(w.getFront(4));
            w.getFront().Pillar(2, new Random(), Material.AIR);
            w.getFront(2).Pillar(2, new Random(), Material.AIR);
            w.getFront(3).Pillar(3, new Random(), Material.AIR);
        }
        else {
            new StairwayBuilder(Material.COBBLESTONE_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS).setAngled(true)
                                                                                               .setStopAtWater(true)
                                                                                               .build(w.getFront()
                                                                                                       .getDown());
        }

        // Decorations depending on variant
        if (this.var == PlainsVillageHouseVariant.COBBLESTONE) {
            w = w.getFront();

            // Logs at the front
            w.getLeft().Pillar(2, rand, plainsVillagePopulator.woodLog);
            w.getRight().Pillar(2, rand, plainsVillagePopulator.woodLog);
            w.getDown().downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
            w.getLeft().getDown().downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
            w.getRight().getDown().downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);

            Orientable log = (Orientable) Bukkit.createBlockData(plainsVillagePopulator.woodLog);
            if (w.getDirection().getModZ() != 0) {
                log.setAxis(Axis.X);
            }
            else {
                log.setAxis(Axis.Z);
            }

            w.getUp(2).setBlockData(log);
            w.getRight().getUp(2).setBlockData(log);
            w.getLeft().getUp(2).setBlockData(log);

            w = w.getFront();
            w.getLeft().getDown().downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
            w.getRight().getDown().downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);


            new SlabBuilder(Material.COBBLESTONE_SLAB, Material.MOSSY_COBBLESTONE_SLAB).setType(Slab.Type.TOP)
                                                                                       .apply(w.getUp(2));

            new SlabBuilder(Material.COBBLESTONE_SLAB, Material.MOSSY_COBBLESTONE_SLAB).setType(Slab.Type.BOTTOM)
                                                                                       .apply(w.getUp(2).getLeft())
                                                                                       .apply(w.getUp(2).getRight());

            w.getLeft().Pillar(2, rand, Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL);
            w.getRight().Pillar(2, rand, Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL);
            w.getLeft().CorrectMultipleFacing(2);
            w.getRight().CorrectMultipleFacing(2);

        }
        else if (this.var == PlainsVillageHouseVariant.CLAY) {
            w.getLeft().getUp().setType(plainsVillagePopulator.woodLog);
            w.getRight().getUp().setType(plainsVillagePopulator.woodLog);
            w = w.getFront();
            w.getLeft().getDown().downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
            w.getRight().getDown().downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);

            Orientable log = (Orientable) Bukkit.createBlockData(plainsVillagePopulator.woodLog);
            if (w.getDirection().getModZ() != 0) {
                log.setAxis(Axis.Z);
            }
            else {
                log.setAxis(Axis.X);
            }

            w.getLeft().setType(plainsVillagePopulator.woodLog);
            w.getLeft().getUp().setType(Material.STONE_BRICK_WALL, Material.MOSSY_STONE_BRICK_WALL);
            w.getLeft().getUp(2).setBlockData(log);
            w.getLeft().getUp().CorrectMultipleFacing(1);

            w.getRight().setType(plainsVillagePopulator.woodLog);
            w.getRight().getUp().setType(Material.STONE_BRICK_WALL, Material.MOSSY_STONE_BRICK_WALL);
            w.getRight().getUp(2).setBlockData(log);
            w.getRight().getUp().CorrectMultipleFacing(1);

            w.getUp(2).setType(BlockUtils.stoneBrickSlabs);

            w = w.getFront();
            w.getLeft().getDown().downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
            w.getRight().getDown().downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);


            w.getLeft().setType(Material.STONE_BRICK_WALL, Material.MOSSY_STONE_BRICK_WALL);
            w.getLeft().CorrectMultipleFacing(1);
            w.getRight().setType(Material.STONE_BRICK_WALL, Material.MOSSY_STONE_BRICK_WALL);
            w.getRight().CorrectMultipleFacing(1);
        }
        else if (this.var == PlainsVillageHouseVariant.WOODEN) {
            Orientable log = (Orientable) Bukkit.createBlockData(plainsVillagePopulator.woodLog);
            if (w.getDirection().getModZ() != 0) {
                log.setAxis(Axis.Z);
            }
            else {
                log.setAxis(Axis.X);
            }

            // w.getUp(2).setBlockData(log);

            w = w.getFront();
            w.getLeft().getDown().downUntilSolid(rand, plainsVillagePopulator.woodLog);
            w.getRight().getDown().downUntilSolid(rand, plainsVillagePopulator.woodLog);


            w.getUp(2).setBlockData(log);

            w.getLeft().setBlockData(log);
            w.getLeft().getUp().setType(Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL);
            w.getLeft().getUp(2).setType(Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL);
            w.getLeft().getUp().CorrectMultipleFacing(2);

            w.getRight().setBlockData(log);
            w.getRight().getUp().setType(Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL);
            w.getRight().getUp(2).setType(Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL);
            w.getRight().getUp().CorrectMultipleFacing(2);

            w = w.getFront();
            w.getLeft().getDown().downUntilSolid(rand, plainsVillagePopulator.woodLog);
            w.getRight().getDown().downUntilSolid(rand, plainsVillagePopulator.woodLog);


            w.getLeft().setType(Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL);
            w.getLeft().CorrectMultipleFacing(1);
            w.getRight().setType(Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL);
            w.getRight().CorrectMultipleFacing(1);
        }
    }

}
