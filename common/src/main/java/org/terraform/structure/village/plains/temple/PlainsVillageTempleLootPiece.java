package org.terraform.structure.village.plains.temple;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.ChestBuilder;

import java.util.AbstractMap.SimpleEntry;
import java.util.Random;

public class PlainsVillageTempleLootPiece extends PlainsVillageTempleStandardPiece {

    public PlainsVillageTempleLootPiece(PlainsVillagePopulator plainsVillagePopulator,
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
        for (BlockFace face : this.getWalledFaces()) {
            SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, face, 0);
            Wall w = entry.getKey();
            for (int i = 0; i < entry.getValue(); i++) {
                if (GenUtils.chance(random, 1, 5) && w.getRear().getType() != plainsVillagePopulator.woodDoor) {
                    new ChestBuilder(Material.CHEST).setFacing(w.getDirection())
                                                    .setLootTable(TerraLootTable.VILLAGE_TEMPLE)
                                                    .apply(w);
                }
                w = w.getLeft();
            }
        }

    }

}
