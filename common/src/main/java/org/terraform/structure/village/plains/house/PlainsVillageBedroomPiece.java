package org.terraform.structure.village.plains.house;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.small_items.DecorationsBuilder;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.ChestBuilder;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.AbstractMap.SimpleEntry;
import java.util.Random;

public class PlainsVillageBedroomPiece extends PlainsVillageStandardPiece {

    public PlainsVillageBedroomPiece(PlainsVillagePopulator plainsVillagePopulator,
                                     PlainsVillageHouseVariant variant,
                                     int widthX,
                                     int height,
                                     int widthZ,
                                     JigsawType type,
                                     BlockFace[] validDirs)
    {
        super(plainsVillagePopulator, variant, widthX, height, widthZ, type, validDirs);
    }

    @Override
    public void build(@NotNull PopulatorDataAbstract data, @NotNull Random rand) {
        super.build(data, rand);
    }

    @Override
    public void postBuildDecoration(@NotNull Random random, @NotNull PopulatorDataAbstract data) {
        super.postBuildDecoration(random, data);

        // No walls :V
        if (this.getWalledFaces().isEmpty()) {
            // Place a dining table or smt, idk

            SimpleBlock core = new SimpleBlock(
                    data,
                    this.getRoom().getX(),
                    this.getRoom().getY() + 1,
                    this.getRoom().getZ()
            );
            core.setType(Material.SMOOTH_STONE);
            for (BlockFace face : BlockUtils.xzPlaneBlockFaces) {
                new SlabBuilder(Material.SMOOTH_STONE_SLAB).setType(Slab.Type.TOP).apply(core.getRelative(face));
            }

            for (BlockFace face : BlockUtils.directBlockFaces) {
                new StairBuilder(Material.POLISHED_ANDESITE_STAIRS).setFacing(face).apply(core.getRelative(face, 2));
            }

            BlockUtils.pickPottedPlant().build(core.getUp());
            return;
        }

        int placedBeds = 0;
        // Populate for walled areas
        for (BlockFace face : this.getWalledFaces()) {
            SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, face, 0);
            Wall w = entry.getKey();

            // First pass, place beds
            for (int i = 0; i < entry.getValue(); i++) {
                if (!w.getFront().isSolid()
                    && placedBeds < 2
                    && w.getRear().getType() != plainsVillagePopulator.woodDoor)
                {
                    if ((GenUtils.chance(random, 2, 5) && placedBeds == 0) || (GenUtils.chance(random, 1, 10)
                                                                               && placedBeds == 1))
                    {
                        BlockUtils.placeBed(w.get(), BlockUtils.pickBed(), w.getDirection());
                        placedBeds++;
                        // Spawn a villager on the bed.
                        data.addEntity(w.getX(), w.getY() + 1, w.getZ(), EntityType.VILLAGER);
                    }
                }
                w = w.getLeft();
            }

            // Second pass, decorate with misc things
            w = entry.getKey();

            for (int i = 0; i < entry.getValue(); i++) {
                // Don't place stuff in front of doors
                if (w.getRear().getType() != plainsVillagePopulator.woodDoor) {
                    if (!Tag.BEDS.isTagged(w.getType())) { // don't replace beds
                        if (Tag.BEDS.isTagged(w.getRight().getType()) || Tag.BEDS.isTagged(w.getLeft().getType())) {
                            // If next to bed,

                            if (random.nextBoolean()) {
                                // Place Night stand
                                new StairBuilder(
                                        Material.STONE_BRICK_STAIRS,
                                        Material.POLISHED_ANDESITE_STAIRS
                                ).setFacing(w.getDirection().getOppositeFace()).setHalf(Half.TOP).apply(w);
                                BlockUtils.pickPottedPlant().build(w.getUp());
                            }
                            else {
                                // Place Crafting Table
                                DecorationsBuilder.CRAFTING_TABLE.build(w);
                            }

                        }
                        else { // Not next to a bed

                            if (GenUtils.chance(random, 1, 10)) {
                                // Chest
                                new ChestBuilder(Material.CHEST).setFacing(w.getDirection())
                                                                .setLootTable(TerraLootTable.VILLAGE_PLAINS_HOUSE)
                                                                .apply(w);
                            }
                            else if (GenUtils.chance(random, 1, 5)) {
                                // Study table, if there's enough space
                                if (!w.getFront().isSolid()) {
                                    new SlabBuilder(
                                            Material.SMOOTH_STONE_SLAB,
                                            Material.POLISHED_ANDESITE_SLAB
                                    ).setType(Slab.Type.TOP).apply(w);
                                    new StairBuilder(Material.POLISHED_ANDESITE_STAIRS).setFacing(w.getDirection())
                                                                                       .apply(w.getFront());
                                }
                            }
                        }
                    }
                }

                w = w.getLeft();
            }
        }
    }

}
