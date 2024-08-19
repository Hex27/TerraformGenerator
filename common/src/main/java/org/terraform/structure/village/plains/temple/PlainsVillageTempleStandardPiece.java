package org.terraform.structure.village.plains.temple;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class PlainsVillageTempleStandardPiece extends JigsawStructurePiece {

    final PlainsVillagePopulator plainsVillagePopulator;
    private boolean isTower = false;

    public PlainsVillageTempleStandardPiece(PlainsVillagePopulator plainsVillagePopulator,
                                            int widthX,
                                            int height,
                                            int widthZ,
                                            JigsawType type,
                                            boolean unique,
                                            BlockFace[] validDirs)
    {
        super(widthX, height, widthZ, type, unique, validDirs);
        this.plainsVillagePopulator = plainsVillagePopulator;
    }

    public PlainsVillageTempleStandardPiece(PlainsVillagePopulator plainsVillagePopulator,
                                            int widthX,
                                            int height,
                                            int widthZ,
                                            JigsawType type,
                                            BlockFace[] validDirs)
    {
        super(widthX, height, widthZ, type, validDirs);
        this.plainsVillagePopulator = plainsVillagePopulator;
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

    @Override
    public void postBuildDecoration(Random random, @NotNull PopulatorDataAbstract data) {
        int[] lowerCorner = this.getRoom().getLowerCorner(0);
        int[] upperCorner = this.getRoom().getUpperCorner(0);

        //        int[] choices = {-2, -1, 0, 1, 2};
        //        int[] steps = new int[3];
        //        for (int i = 0; i < 3; i++) steps[i] = choices[random.nextInt(choices.length)];
        //        Material other = BRIGHT_CARPETS.get(random.nextInt(BRIGHT_CARPETS.size()));
        // Place carpets.
        for (int x = lowerCorner[0]; x <= upperCorner[0]; x++) {
            for (int z = lowerCorner[1]; z <= upperCorner[1]; z++) {
                if (x % 2 == 0 && z % 2 == 0) {
                    data.setType(x, getRoom().getY(), z, Material.TORCH);
                }

                if (!data.getType(x, getRoom().getY() + 1, z).isSolid()) {
                    boolean canPlace = true;

                    // VILLAGERS CAN'T WALK THROUGH CARPETS FOR SOME FUCK REASON
                    // Don't place carpet if there's a door next to it.
                    for (BlockFace face : BlockUtils.directBlockFaces) {
                        if (data.getType(x + face.getModX(), getRoom().getY() + 1, z + face.getModZ())
                            == plainsVillagePopulator.woodDoor)
                        {
                            canPlace = false;
                            data.setType(x, getRoom().getY(), z, Material.WHITE_WOOL);
                        }
                    }
                    if (canPlace) {
                        data.setType(x, getRoom().getY() + 1, z, Material.WHITE_CARPET);
                    }
                }
            }
        }

        // Commented out because it's ugly af
        // Random pattern on floor
    }

    public boolean isTower() {
        return isTower;
    }

    public void setTower(boolean isTower) {
        this.isTower = isTower;
    }
}
