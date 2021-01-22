package org.terraform.structure.village.plains.house;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Ladder;
import org.bukkit.block.data.type.Slab;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.AbstractMap.SimpleEntry;
import java.util.Random;

public class PlainsVillageLibraryPiece extends PlainsVillageStandardPiece {

    public PlainsVillageLibraryPiece(PlainsVillageHouseVariant variant, int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
        super(variant, widthX, height, widthZ, type, validDirs);
    }

    @Override
    public void postBuildDecoration(Random random, PopulatorDataAbstract data) {
        super.postBuildDecoration(random, data);

        //In the center, place a single lectern
        SimpleBlock core = new SimpleBlock(data, this.getRoom().getX(), this.getRoom().getY() + 1, this.getRoom().getZ());
        new DirectionalBuilder(Material.LECTERN)
                .setFacing(BlockUtils.getDirectBlockFace(random))
                .apply(core);

        //Populate for walled areas
        for(BlockFace face : this.getWalledFaces()) {
            SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, face, 0);
            Wall w = entry.getKey();

            for(int i = 0; i < entry.getValue(); i++) {
                if(i == 0 || i == 4) {
                    w.LPillar(25, random, Material.BOOKSHELF);
                    if(GenUtils.chance(random, 1, 10)) {
                        Ladder ladder = (Ladder) Bukkit.createBlockData(Material.LADDER);
                        ladder.setFacing(w.getDirection());
                        for(int h = 0; h < 25; h++) {
                            if(w.getFront().getRelative(0, h, 0).getType().isSolid())
                                break;
                            w.getFront().getRelative(0, h, 0).setBlockData(ladder);
                        }
                    }
                } else if(i == 1 || i == 3) {
                    new StairBuilder(Material.OAK_STAIRS)
                            .setFacing(w.getDirection().getOppositeFace())
                            .apply(w);
                    new StairBuilder(Material.STONE_BRICK_STAIRS, Material.MOSSY_STONE_BRICK_STAIRS)
                            .setFacing(w.getDirection().getOppositeFace())
                            .setHalf(Half.TOP)
                            .apply(w.getRelative(0, 2, 0));
                    w.getRelative(0, 3, 0).LPillar(25, random, Material.BOOKSHELF);
                } else {
                    if(w.getRear().getType() != Material.OAK_DOOR)
                        new SlabBuilder(Material.OAK_SLAB)
                                .apply(w);

                    new SlabBuilder(Material.STONE_BRICK_SLAB, Material.MOSSY_STONE_BRICK_SLAB)
                            .setType(Slab.Type.TOP)
                            .apply(w.getRelative(0, 2, 0));

                    w.getRelative(0, 3, 0).LPillar(25, random, Material.BOOKSHELF);
                }
                w = w.getLeft();
            }
        }
    }

}
