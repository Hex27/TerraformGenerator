package org.terraform.structure.village.plains.house;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Lantern;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.Version;

import java.util.AbstractMap.SimpleEntry;
import java.util.Random;

public class PlainsVillageStandardPiece extends JigsawStructurePiece {

    PlainsVillageHouseVariant variant;
    PlainsVillagePopulator plainsVillagePopulator;
    public PlainsVillageStandardPiece(PlainsVillagePopulator plainsVillagePopulator, PlainsVillageHouseVariant variant, int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
        super(widthX, height, widthZ, type, validDirs);
        this.variant = variant;
        this.plainsVillagePopulator = plainsVillagePopulator;
    }

    @Override
    public void build(PopulatorDataAbstract data, Random rand) {
        int[] lowerCorner = this.getRoom().getLowerCorner(0);
        int[] upperCorner = this.getRoom().getUpperCorner(0);

        //Place flooring.
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++)
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                data.setType(x, this.getRoom().getY(), z,
                        GenUtils.randMaterial(
                                Material.STONE_BRICKS,
                                Material.STONE_BRICKS,
                                Material.STONE_BRICKS,
                                Material.MOSSY_STONE_BRICKS,
                                Material.CRACKED_STONE_BRICKS
                        ));
                new Wall(new SimpleBlock(data, x, this.getRoom().getY() - 1, z))
                        .downUntilSolid(rand,
                                Material.STONE_BRICKS,
                                Material.STONE_BRICKS,
                                Material.STONE_BRICKS,
                                Material.MOSSY_STONE_BRICKS,
                                Material.CRACKED_STONE_BRICKS
                        );
            }
    }

    @Override
    public void postBuildDecoration(Random random, PopulatorDataAbstract data) {
        int[] lowerCorner = this.getRoom().getLowerCorner(0);
        int[] upperCorner = this.getRoom().getUpperCorner(0);
        //Raise ceiling.
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++)
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                data.setType(x, this.getRoom().getY() + 4, z, Material.AIR);
            }

        //Fix weird walling for standard roofs.
        for (BlockFace face : this.getWalledFaces()) {
            SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, face, -1);
            Wall w = entry.getKey().getRelative(0, 2, 0);
            for (int i = 0; i < entry.getValue(); i++) {
                Material type = w.getType();
                if (w.getRelative(0, 1, 0).getType() != plainsVillagePopulator.woodLog) ;
                w.getRelative(0, 1, 0).setType(type);

                w = w.getLeft();
            }
        }

        //Place lanterns (At least one per room)
        //for(int i = 0; i < GenUtils.randInt(random, 1, 4); i++) {
        //	int[] coords = room.randomCoords(random,1);
        //	genLanterns(data,coords[0],coords[2]);
        //}
        genLanterns(data, this.getRoom().getX(), this.getRoom().getZ());
    }

    private void genLanterns(PopulatorDataAbstract data, int x, int z) {
        Wall w = new Wall(new SimpleBlock(data, x, this.getRoom().getY() + 1, z));
        w = w.findCeiling(25);
        if (w == null) {
            //Bruh wtf
            return;
        }

        w = w.getRelative(0, -1, 0);
        int space = w.getY() - room.getY() - 3;
        if (space <= 0) {
            return; //Ceiling too low.
        }
        int units = GenUtils.randInt(1, space);
        for (int i = 0; i < units; i++) {
            if (i == units - 1) {
                Lantern lantern = (Lantern) Bukkit.createBlockData(Material.LANTERN);
                lantern.setHanging(true);
                w.setBlockData(lantern);
            } else {
                w.setType(Material.CHAIN);
                w = w.getRelative(0, -1, 0);
            }
        }
    }
}
