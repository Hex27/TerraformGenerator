package org.terraform.structure.pillager.mansion.ground;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab.Type;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.pillager.mansion.MansionInternalWallState;
import org.terraform.structure.pillager.mansion.MansionRoomPopulator;
import org.terraform.structure.pillager.mansion.MansionRoomSize;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.ArmorStandUtils;
import org.terraform.utils.BannerUtils;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.blockdata.OrientableBuilder;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

public class MansionGrandStairwayPopulator extends MansionRoomPopulator {

    public MansionGrandStairwayPopulator(CubeRoom room, HashMap<BlockFace, MansionInternalWallState> internalWalls) {
        super(room, internalWalls);
    }

    @Override
    public void decorateRoom(@NotNull PopulatorDataAbstract data, @NotNull Random random) {
        SimpleBlock target = this.getRoom().getCenterSimpleBlock(data);
        try {
            TerraSchematic schema = TerraSchematic.load("mansion/mansion-stairway", target);
            // schema.parser = new MansionRoomSchematicParser();
            schema.setFace(BlockUtils.getDirectBlockFace(random));
            schema.apply();
        }
        catch (FileNotFoundException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }
    }

    @Override
    public void decorateWall(@NotNull Random rand, @NotNull Wall w) {
        // Arch

        w.getLeft(3).Pillar(7, Material.DARK_OAK_LOG);
        w.getUp(6).setType(Material.DARK_OAK_PLANKS);
        w.getRight(3).Pillar(7, Material.DARK_OAK_LOG);
        new StairBuilder(Material.DARK_OAK_STAIRS).setHalf(Half.TOP)
                                                  .setFacing(BlockUtils.getLeft(w.getDirection()))
                                                  .apply(w.getUp(5).getLeft(2))
                                                  .apply(w.getUp(6).getLeft(2))
                                                  .apply(w.getUp(6).getLeft())
                                                  .setFacing(BlockUtils.getRight(w.getDirection()))
                                                  .apply(w.getUp(5).getRight(2))
                                                  .apply(w.getUp(6).getRight(2))
                                                  .apply(w.getUp(6).getRight());
        int choice = rand.nextInt(2);
        // Armor stands
        if (choice == 0) { // Wall carving
            w.getRear().getUp().Pillar(5, Material.DARK_OAK_LOG);
            new OrientableBuilder(Material.DARK_OAK_LOG).setAxis(BlockUtils.getAxisFromBlockFace(w.getDirection()))
                                                        .apply(w.getRear().getUp(3))
                                                        .apply(w.getRear().getUp(2).getLeft())
                                                        .apply(w.getRear().getUp(2).getRight())
                                                        .apply(w.getRear().getUp(4).getLeft())
                                                        .apply(w.getRear().getUp(4).getRight())
                                                        .setAxis(BlockUtils.getAxisFromBlockFace(BlockUtils.getLeft(w.getDirection())))
                                                        .apply(w.getRear().getUp(3).getLeft())
                                                        .apply(w.getRear().getUp(3).getLeft(2))
                                                        .apply(w.getRear().getUp(3).getRight())
                                                        .apply(w.getRear().getUp(3).getRight(2));
        }
        else {
            BannerUtils.generatePillagerBanner(w.getUp(4).get(), w.getDirection(), true);

            new SlabBuilder(Material.POLISHED_ANDESITE_SLAB).setType(Type.TOP)
                                                            .apply(w)
                                                            .apply(w.getLeft())
                                                            .apply(w.getLeft(2))
                                                            .apply(w.getRight())
                                                            .apply(w.getRight(2));

            ArmorStandUtils.placeArmorStand(w.getUp(2).get(), w.getDirection(), rand);
            ArmorStandUtils.placeArmorStand(w.getUp(2).getLeft(2).get(), w.getDirection(), rand);
            ArmorStandUtils.placeArmorStand(w.getUp(2).getRight(2).get(), w.getDirection(), rand);
        }
    }

    @Override
    public void decorateWindow(Random rand, @NotNull Wall w) {

        Entry<Wall, Integer> entry = this.getRoom()
                                         .getWall(w.get().getPopData(), w.getDirection().getOppositeFace(), 0);
        w = entry.getKey();
        for (int i = 0; i < entry.getValue(); i++) {
            if (w.getFront().getUp(6).getType() == Material.DARK_OAK_SLAB) {
                w.getRear().Pillar(7, Material.DARK_OAK_PLANKS);
                w.Pillar(7, Material.DARK_OAK_LOG);
                new StairBuilder(Material.DARK_OAK_STAIRS).setFacing(w.getDirection().getOppositeFace())
                                                          .apply(w.getFront());
            }
            w = w.getLeft();
        }
    }

    @Override
    public @NotNull MansionRoomSize getSize() {
        return new MansionRoomSize(3, 3);
    }

}
