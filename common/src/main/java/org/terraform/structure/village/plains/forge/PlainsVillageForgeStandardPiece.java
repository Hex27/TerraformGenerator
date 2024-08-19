package org.terraform.structure.village.plains.forge;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class PlainsVillageForgeStandardPiece extends PlainsVillageForgePiece {

    public PlainsVillageForgeStandardPiece(PlainsVillagePopulator plainsVillagePopulator,
                                           int widthX,
                                           int height,
                                           int widthZ,
                                           JigsawType type,
                                           boolean unique,
                                           BlockFace[] validDirs)
    {
        super(plainsVillagePopulator, widthX, height, widthZ, type, unique, validDirs);
    }

    public PlainsVillageForgeStandardPiece(PlainsVillagePopulator plainsVillagePopulator,
                                           int widthX,
                                           int height,
                                           int widthZ,
                                           JigsawType type,
                                           BlockFace[] validDirs)
    {
        super(plainsVillagePopulator, widthX, height, widthZ, type, validDirs);
    }

    @Override
    public void build(@NotNull PopulatorDataAbstract data, @NotNull Random rand) {
        int[] lowerCorner = this.getRoom().getLowerCorner(0);
        int[] upperCorner = this.getRoom().getUpperCorner(0);

        // Place flooring.
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                data.setType(x, this.getRoom().getY(), z, GenUtils.randChoice(
                        Material.STONE_BRICKS,
                        Material.STONE_BRICKS,
                        Material.STONE_BRICKS,
                        Material.STONE_BRICKS,
                        Material.STONE_BRICKS,
                        Material.STONE_BRICKS,
                        Material.CRACKED_STONE_BRICKS
                ));
                new Wall(new SimpleBlock(data, x, this.getRoom().getY() - 1, z)).downUntilSolid(
                        rand,
                        Material.STONE_BRICKS,
                        Material.STONE_BRICKS,
                        Material.STONE_BRICKS,
                        Material.STONE_BRICKS,
                        Material.STONE_BRICKS,
                        Material.STONE_BRICKS,
                        Material.CRACKED_STONE_BRICKS
                );
            }
        }
    }


}
