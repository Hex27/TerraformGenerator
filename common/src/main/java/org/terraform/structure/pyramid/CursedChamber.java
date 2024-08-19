package org.terraform.structure.pyramid;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Random;

public class CursedChamber extends RoomPopulatorAbstract {

    public CursedChamber(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {
        int[] lowerCorner = room.getLowerCorner(3);
        int[] upperCorner = room.getUpperCorner(3);

        // Skulls on the walls
        for (Entry<Wall, Integer> entry : room.getFourWalls(data, 1).entrySet()) {
            Wall w = entry.getKey();
            for (int i = 0; i < entry.getValue(); i++) {
                for (int h = 0; h < room.getHeight() - 2; h++) {
                    if (w.getRelative(0, h, 0).getRear().isSolid() && GenUtils.chance(rand, 1, 5)) {
                        Directional head = (Directional) Bukkit.createBlockData(Material.SKELETON_WALL_SKULL);
                        head.setFacing(w.getDirection());
                        w.getRelative(0, h, 0).setBlockData(head);
                    }
                }

                w = w.getLeft();
            }
        }

        ArrayList<Integer> availableX = new ArrayList<>();
        ArrayList<Integer> availableZ = new ArrayList<>();
        // X-width is wide enough, spawn 2 pillars along x axis
        if (room.getWidthX() > 10) {
            availableX.add(lowerCorner[0]);
            availableX.add(upperCorner[0]);
        }
        else { // Spawn only one.
            availableX.add(room.getX());
        }

        // Z-width is wide enough, spawn 2 pillars along z axis
        if (room.getWidthZ() > 10) {
            availableZ.add(lowerCorner[1]);
            availableZ.add(upperCorner[1]);
        }
        else { // Spawn only one.
            availableZ.add(room.getZ());
        }

        // Build the pillars
        for (int nx : availableX) {
            for (int nz : availableZ) {
                spawnSkullPillar(new Wall(new SimpleBlock(data, nx, room.getY() + 1, nz)), room);
            }
        }

        // Ceiling erosions
        for (int i = 0; i < GenUtils.randInt(3, 10); i++) {
            int[] loc = room.randomCoords(rand, 1);
            if (data.getType(loc[0], room.getY() + room.getHeight() + 1, loc[2]) == Material.SAND) {
                data.setType(loc[0], room.getY() + room.getHeight() + 1, loc[2], Material.SANDSTONE);
            }
            BlockUtils.dropDownBlock(new SimpleBlock(data, loc[0], room.getY() + room.getHeight(), loc[2]));
        }

        // Skeletons
        for (int i = 0; i < GenUtils.randInt(rand, 1, 4); i++) {
            data.addEntity(room.getX() - room.getWidthX() / 2 + 1, room.getY() + 1, room.getZ(), EntityType.SKELETON);
        }
    }

    public void spawnSkullPillar(@NotNull Wall w, @NotNull CubeRoom room) {
        // Skull Pillar
        w.LPillar(room.getHeight() - 1, rand, Material.SANDSTONE, Material.CHISELED_SANDSTONE);

        // Stair base and ceiling
        for (BlockFace face : BlockUtils.directBlockFaces) {
            Stairs stair = (Stairs) Bukkit.createBlockData(GenUtils.randChoice(
                    Material.SANDSTONE_STAIRS,
                    Material.STONE_STAIRS,
                    Material.COBBLESTONE_STAIRS
            ));
            stair.setFacing(face.getOppositeFace());
            w.getRelative(face).setBlockData(stair);


            stair = (Stairs) Bukkit.createBlockData(GenUtils.randChoice(
                    Material.SANDSTONE_STAIRS,
                    Material.STONE_STAIRS,
                    Material.COBBLESTONE_STAIRS
            ));
            stair.setFacing(face.getOppositeFace());
            stair.setHalf(Half.TOP);
            w.getRelative(face).getRelative(0, room.getHeight() - 2, 0).setBlockData(stair);
        }

        // Attach skulls to the pillars
        for (int h = 1; h < room.getHeight() - 3; h++) {
            Wall target = w.getRelative(0, h, 0);
            for (BlockFace face : BlockUtils.directBlockFaces) {
                if (GenUtils.chance(4, 5)) {
                    continue;
                }
                Directional head = (Directional) Bukkit.createBlockData(Material.SKELETON_WALL_SKULL);
                head.setFacing(face);
                target.getRelative(face).setBlockData(head);
            }
        }

        // Corrupt Ceiling
        for (int nz = -2; nz <= 2; nz++) {
            for (int nx = -2; nx <= 2; nx++) {
                w.getRelative(nx, room.getHeight() - 1, nz)
                 .setType(GenUtils.randChoice(Material.ANDESITE, Material.COBBLESTONE, Material.SANDSTONE));
            }
        }
    }

    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        // 13, as that's the size for crypt rooms. Don't compete with crypt rooms.
        return room.getWidthX() > 6 && room.getWidthZ() > 6;
        // 6, as the pad for pillars is 3.
    }
}
