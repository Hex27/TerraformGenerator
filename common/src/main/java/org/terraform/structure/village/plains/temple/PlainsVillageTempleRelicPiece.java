package org.terraform.structure.village.plains.temple;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.Random;

public class PlainsVillageTempleRelicPiece extends PlainsVillageTempleStandardPiece {

    private static final Material[] stairTypes = {
            Material.POLISHED_GRANITE_STAIRS, Material.POLISHED_ANDESITE_STAIRS, Material.POLISHED_DIORITE_STAIRS
    };
    private static final Material[] slabTypes = {
            Material.POLISHED_GRANITE_SLAB,
            Material.POLISHED_ANDESITE_SLAB,
            Material.POLISHED_DIORITE_SLAB,
            Material.SMOOTH_STONE_SLAB
    };
    private static final Material[] relics = {
            Material.EMERALD_BLOCK,
            Material.WHITE_GLAZED_TERRACOTTA,
            Material.BLACK_GLAZED_TERRACOTTA,
            Material.BLUE_GLAZED_TERRACOTTA,
            Material.BROWN_GLAZED_TERRACOTTA,
            Material.CYAN_GLAZED_TERRACOTTA,
            Material.GRAY_GLAZED_TERRACOTTA,
            Material.GREEN_GLAZED_TERRACOTTA,
            Material.LIGHT_BLUE_GLAZED_TERRACOTTA,
            Material.LIGHT_GRAY_GLAZED_TERRACOTTA,
            Material.LIME_GLAZED_TERRACOTTA,
            Material.MAGENTA_GLAZED_TERRACOTTA,
            Material.ORANGE_GLAZED_TERRACOTTA,
            Material.PINK_GLAZED_TERRACOTTA,
            Material.PURPLE_GLAZED_TERRACOTTA,
            Material.RED_GLAZED_TERRACOTTA,
            Material.YELLOW_GLAZED_TERRACOTTA,
            Material.GOLD_BLOCK
    };

    public PlainsVillageTempleRelicPiece(PlainsVillagePopulator plainsVillagePopulator,
                                         int widthX,
                                         int height,
                                         int widthZ,
                                         JigsawType type,
                                         boolean unique,
                                         BlockFace[] validDirs)
    {
        super(plainsVillagePopulator, widthX, height, widthZ, type, unique, validDirs);
    }

    @Override
    public void postBuildDecoration(@NotNull Random random, @NotNull PopulatorDataAbstract data) {
        super.postBuildDecoration(random, data);

        Material stairType = stairTypes[random.nextInt(stairTypes.length)];
        Material slab = slabTypes[random.nextInt(slabTypes.length)];

        SimpleBlock core = new SimpleBlock(data,
                this.getRoom().getX(),
                this.getRoom().getY() + 1,
                this.getRoom().getZ());
        for (BlockFace face : BlockUtils.directBlockFaces) {
            new StairBuilder(stairType).setFacing(face.getOppositeFace()).apply(core.getRelative(face));
        }
        for (BlockFace face : BlockUtils.xzDiagonalPlaneBlockFaces) {
            core.getRelative(face).setType(slab);
        }
        core.getUp().setType(relics[random.nextInt(relics.length)]);
    }

}
