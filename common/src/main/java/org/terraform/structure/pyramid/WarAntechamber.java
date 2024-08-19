package org.terraform.structure.pyramid;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Stairs;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BannerUtils;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Map.Entry;
import java.util.Random;


public class WarAntechamber extends Antechamber {

    public WarAntechamber(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
        // TODO Auto-generated constructor stub
    }

    /***
     * This antechamber will contain war banners and artifacts.
     * It represents the Pharoah's triumphs in battle.
     */
    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        super.populate(data, room);
        // Decorate the walls with various banners
        for (Entry<Wall, Integer> entry : room.getFourWalls(data, 1).entrySet()) {
            Wall w = entry.getKey().getUp(2);
            for (int i = 0; i < entry.getValue(); i++) {

                if (w.getRear().isSolid() && !w.isSolid() && GenUtils.chance(rand, 3, 10)) {
                    BannerUtils.generateBanner(rand, w.get(), w.getDirection(), true);
                }

                w = w.getLeft();
            }

        }

        // Pillar in the center.
        Wall w = new Wall(new SimpleBlock(data, room.getX(), room.getY() + 1, room.getZ()));
        w.LPillar(room.getHeight(), rand, Material.CHISELED_SANDSTONE);

        // Stair base and ceiling
        for (BlockFace face : BlockUtils.directBlockFaces) {
            Stairs stair = (Stairs) Bukkit.createBlockData(Material.SANDSTONE_STAIRS);
            stair.setFacing(face.getOppositeFace());
            w.getRelative(face).setBlockData(stair);


            stair = (Stairs) Bukkit.createBlockData(Material.SANDSTONE_STAIRS);
            stair.setFacing(face.getOppositeFace());
            stair.setHalf(Half.TOP);
            w.getRelative(face).getRelative(0, room.getHeight() - 2, 0).setBlockData(stair);
        }

        // Central Precious Block
        w.getRelative(0, room.getHeight() / 2 - 1, 0).setType(GenUtils.randChoice(
                rand,
                Material.GOLD_BLOCK,
                Material.LAPIS_BLOCK,
                Material.LAPIS_BLOCK,
                Material.EMERALD_BLOCK,
                Material.IRON_BLOCK
        ));

    }


    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getWidthX() >= 6 && room.getWidthZ() >= 6;
    }
}
