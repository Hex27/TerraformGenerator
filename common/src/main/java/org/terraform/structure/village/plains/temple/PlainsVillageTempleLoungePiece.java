package org.terraform.structure.village.plains.temple;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.main.config.TConfig;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.Random;

public class PlainsVillageTempleLoungePiece extends PlainsVillageTempleStandardPiece {

    private static final Material[] stairTypes = {
            Material.POLISHED_GRANITE_STAIRS, Material.POLISHED_ANDESITE_STAIRS, Material.POLISHED_DIORITE_STAIRS
    };

    public PlainsVillageTempleLoungePiece(PlainsVillagePopulator plainsVillagePopulator,
                                          int widthX,
                                          int height,
                                          int widthZ,
                                          JigsawType type,
                                          BlockFace[] validDirs)
    {
        super(plainsVillagePopulator, widthX, height, widthZ, type, validDirs);
    }

    @Override
    public void postBuildDecoration(@NotNull Random random, @NotNull PopulatorDataAbstract data) {
        super.postBuildDecoration(random, data);

        Material stairType = stairTypes[random.nextInt(stairTypes.length)];

        SimpleBlock core = new SimpleBlock(
                data,
                this.getRoom().getX(),
                this.getRoom().getY() + 1,
                this.getRoom().getZ()
        );

        for (BlockFace face : BlockUtils.getRandomBlockfaceAxis(random)) {

            new StairBuilder(stairType).setFacing(face)
                                       .apply(core.getRelative(face).getRelative(BlockUtils.getAdjacentFaces(face)[0]))
                                       .apply(core.getRelative(face).getRelative(BlockUtils.getAdjacentFaces(face)[1]));

        }
        if (TConfig.areDecorationsEnabled()) {
            core.setType(plainsVillagePopulator.woodLog, Material.CRAFTING_TABLE, plainsVillagePopulator.woodPlank);
            if (!TConfig.arePlantsEnabled() || random.nextBoolean()) {
                core.getUp().setType(Material.LANTERN);
            }
            else {
                BlockUtils.pickPottedPlant().build(core.getUp());
            }
        }
    }

}
