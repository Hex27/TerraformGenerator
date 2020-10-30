package org.terraform.structure.pyramid;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.schematic.SchematicParser;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;

import java.util.Map.Entry;
import java.util.Random;

public class PharoahsTombPopulator extends RoomPopulatorAbstract {

    public PharoahsTombPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
        //4 statues
        SimpleBlock base = new SimpleBlock(data, room.getX(), room.getY() + 1, room.getZ());
        for (BlockFace face : BlockUtils.directBlockFaces) {
            placeStatue(base.getRelative(face, 4), face.getOppositeFace());
        }

        //4 pillars
        for (BlockFace face : BlockUtils.xzDiagonalPlaneBlockFaces) {
            placePillar(new Wall(base.getRelative(face, 6)), room.getHeight() - 1);
        }

        //Classic Pyramid interior floor decoration
        SimpleBlock center = new SimpleBlock(data, room.getX(), room.getY(), room.getZ());
        center.setType(Material.BLUE_TERRACOTTA);
        for (BlockFace face : BlockUtils.xzDiagonalPlaneBlockFaces) {
            center.getRelative(face).setType(Material.ORANGE_TERRACOTTA);
            new Wall(center.getRelative(face).getRelative(face).getRelative(0, 1, 0))
                    .Pillar(room.getHeight(), rand, Material.CUT_SANDSTONE);
        }
        for (BlockFace face : BlockUtils.directBlockFaces)
            center.getRelative(face).getRelative(face)
                    .setType(Material.ORANGE_TERRACOTTA);

        for (Entry<Wall, Integer> entry : room.getFourWalls(data, 0).entrySet()) {
            Wall w = entry.getKey();
            for (int i = 0; i < entry.getValue(); i++) {
                //TODO: 
                w = w.getLeft();
            }
        }

    }

    private void placePillar(Wall base, int height) {

        for (BlockFace face : BlockUtils.xzDiagonalPlaneBlockFaces) {
            base.getRelative(face).Pillar(height, rand, Material.CUT_SANDSTONE, Material.SMOOTH_SANDSTONE);
        }

        for (BlockFace face : BlockUtils.directBlockFaces) {
            base.getRelative(face).Pillar(height, true, rand, Material.CUT_SANDSTONE, Material.CHISELED_SANDSTONE, Material.AIR, Material.AIR, Material.AIR,
                    Material.CHISELED_SANDSTONE);
        }
        base.Pillar(height, rand, Material.CHISELED_RED_SANDSTONE);
    }

    private void placeStatue(SimpleBlock base, BlockFace dir) {
        try {
            World w = ((PopulatorDataPostGen) base.getPopData()).getWorld();
            TerraSchematic schema = TerraSchematic.load("pharoah-statue", new Location(w, base.getX(), base.getY(), base.getZ()));
            schema.parser = new SchematicParser();
            schema.setFace(dir);
            schema.apply();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return true;
    }


}
