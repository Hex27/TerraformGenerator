package org.terraform.structure.pyramid;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.BlockFace;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.Stairs;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BannerUtils;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
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
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
        super.populate(data, room);
        //Decorate the walls with various banners
        for (Entry<Wall, Integer> entry : room.getFourWalls(data, 1).entrySet()) {
            Wall w = entry.getKey().getRelative(0, 2, 0);
            for (int i = 0; i < entry.getValue(); i++) {

                if (w.getRear().getType().isSolid() && !w.getType().isSolid()
                        && GenUtils.chance(rand, 3, 10)) {
                    generateBanner(w.get(), w.getDirection(), true);
                }

                w = w.getLeft();
            }

        }

        //Pillar in the center.
        Wall w = new Wall(new SimpleBlock(data, room.getX(), room.getY() + 1, room.getZ()));
        w.LPillar(room.getHeight(), rand, Material.CHISELED_SANDSTONE);

        //Stair base and ceiling
        for (BlockFace face : BlockUtils.directBlockFaces) {
            Stairs stair = (Stairs) Bukkit.createBlockData(Material.SANDSTONE_STAIRS);
            stair.setFacing(face.getOppositeFace());
            w.getRelative(face).setBlockData(stair);


            stair = (Stairs) Bukkit.createBlockData(Material.SANDSTONE_STAIRS);
            stair.setFacing(face.getOppositeFace());
            stair.setHalf(Half.TOP);
            w.getRelative(face).getRelative(0, room.getHeight() - 2, 0).setBlockData(stair);
        }

        //Central Precious Block
        w.getRelative(0, room.getHeight() / 2 - 1, 0).setType(GenUtils.randMaterial(rand,
                Material.GOLD_BLOCK,
                Material.LAPIS_BLOCK,
                Material.LAPIS_BLOCK,
                Material.EMERALD_BLOCK,
                Material.IRON_BLOCK));

    }

    private Banner generateBanner(SimpleBlock base, BlockFace facing, boolean wallBanner) {

        Material type = null;
        if (wallBanner)
            type = BannerUtils.randomWallBannerMaterial(rand);
        else
            BannerUtils.randomBannerMaterial(rand);
        base.setType(type);
        if (!wallBanner) {
            Rotatable bd = ((Rotatable) base.getBlockData());
            bd.setRotation(facing);
            base.setBlockData(bd);
        } else {
            Directional bd = ((Directional) base.getBlockData());
            bd.setFacing(facing);
            base.setBlockData(bd);
        }

        Banner banner = (Banner) ((PopulatorDataPostGen) base.getPopData()).getBlockState(base.getX(), base.getY(), base.getZ());
        ArrayList<Pattern> patterns = new ArrayList<Pattern>();

        for (int i = 1 + rand.nextInt(3); i < 4 + rand.nextInt(3); i++) {
            patterns.add(new Pattern(
                    DyeColor.values()[rand.nextInt(DyeColor.values().length)],
                    PatternType.values()[rand.nextInt(PatternType.values().length)]
            ));
        }
        banner.setPatterns(patterns);
        banner.update();
        return banner;
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return room.getWidthX() >= 6 && room.getWidthZ() >= 6;
    }
}
