package org.terraform.structure.village.plains.house;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.FaceAttachable.AttachedFace;
import org.bukkit.block.data.type.Switch;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.structure.village.plains.PlainsVillagePopulator;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.ChestBuilder;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class PlainsVillageKitchenPiece extends PlainsVillageStandardPiece {

    public PlainsVillageKitchenPiece(PlainsVillagePopulator plainsVillagePopulator,
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
    public void postBuildDecoration(@NotNull Random random, @NotNull PopulatorDataAbstract data) {
        super.postBuildDecoration(random, data);

        // No walls :V
        if (this.getWalledFaces().isEmpty()) {
            return;
        }

        // Pick a random walled face to be the primary wall, where all the stuff goes.
        BlockFace primaryWall = this.getWalledFaces().get(random.nextInt(this.getWalledFaces().size()));
        SimpleBlock core = new SimpleBlock(
                data,
                this.getRoom().getX(),
                this.getRoom().getY() + 1,
                this.getRoom().getZ()
        );
        int numUtilities = 5;
        if (core.getRelative(primaryWall, 3).getType() == plainsVillagePopulator.woodDoor) {
            numUtilities--;
        }

        SimpleEntry<Wall, Integer> entry = this.getRoom().getWall(data, primaryWall, 0);
        Wall w = entry.getKey();
        ArrayList<Material> utilities = Lists.newArrayList(Material.SMOKER);
        for (int i = 0; i < numUtilities; i++) {
            utilities.add(GenUtils.randChoice(random, Material.HOPPER, Material.FURNACE, Material.CRAFTING_TABLE));
        }
        Collections.shuffle(utilities);
        for (int i = 0; i < entry.getValue(); i++) {
            if (w.getRear().getType() != plainsVillagePopulator.woodDoor) {
                numUtilities--;
                Material mat = utilities.get(numUtilities);
                switch (mat) {
                    case HOPPER -> {
                        w.setType(mat);
                        if (w.getRear().getUp().getType() != Material.GLASS_PANE) {
                            Switch lever = (Switch) Bukkit.createBlockData(Material.LEVER);
                            lever.setPowered(true);
                            lever.setAttachedFace(AttachedFace.WALL);
                            lever.setFacing(w.getDirection());
                            w.getUp().setBlockData(lever);
                        }
                    } // Furnace and smoker handled the same way
                    case FURNACE, SMOKER -> {
                        new DirectionalBuilder(mat).setFacing(w.getDirection()).apply(w);
                        w.getUp().setType(plainsVillagePopulator.woodPressurePlate);
                        new StairBuilder(Material.BRICK_STAIRS).setFacing(w.getDirection().getOppositeFace())
                                                               .setHalf(Half.TOP)
                                                               .apply(w.getUp(2));
                        Wall chimneyWall = w.getUp(3);
                        boolean hitCeiling = false;
                        int chimneyHeight = 0;
                        while (chimneyHeight < 4) {
                            if (chimneyWall.isSolid()) {
                                hitCeiling = true;
                            }
                            else if (hitCeiling) {
                                chimneyHeight++;
                                if (GenUtils.chance(random, chimneyHeight, 3)) {
                                    break;
                                }
                            }
                            chimneyWall.setType(Material.BRICKS);

                            chimneyWall = chimneyWall.getUp();
                        }
                        chimneyWall.setType(Material.BRICK_WALL);
                    }
                    case CRAFTING_TABLE -> w.setType(mat);
                    default -> {
                    }
                }
            }
            w = w.getLeft();
        }

        // Other walls can be decorated with random and loot
        // Populate for walled areas
        for (BlockFace face : this.getWalledFaces()) {
            if (face == primaryWall) {
                continue;
            }
            entry = this.getRoom().getWall(data, face, 0);
            w = entry.getKey();

            for (int i = 0; i < entry.getValue(); i++) {
                if (w.getRear().getType() != plainsVillagePopulator.woodDoor && !w.isSolid()) {
                    int decor = random.nextInt(5);
                    switch (decor) {
                        case 0: // Counter
                            new StairBuilder(
                                    Material.STONE_BRICK_STAIRS,
                                    Material.POLISHED_ANDESITE_STAIRS,
                                    plainsVillagePopulator.woodStairs
                            ).setFacing(w.getDirection().getOppositeFace()).setHalf(Half.TOP).apply(w);
                            break;
                        case 1: // Solid counter or other random solid blocks
                            w.setType(Material.SMOOTH_STONE,
                                    Material.POLISHED_ANDESITE,
                                    Material.PUMPKIN,
                                    Material.DRIED_KELP_BLOCK,
                                    Material.MELON
                            );
                            break;
                        case 2: // Random loot
                            new ChestBuilder(Material.CHEST).setFacing(w.getDirection())
                                                            .setLootTable(
                                                                    TerraLootTable.VILLAGE_BUTCHER,
                                                                    TerraLootTable.VILLAGE_PLAINS_HOUSE
                                                            );
                        default: // Do nothing
                            break;
                    }
                }
                w = w.getLeft();
            }
        }

    }

    @Override
    public void build(@NotNull PopulatorDataAbstract data, @NotNull Random rand) {
        // this.getRoom().fillRoom(data, new Material[] {Material.BLUE_STAINED_GLASS});
        super.build(data, rand);
    }

}
