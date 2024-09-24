package org.terraform.structure.pyramid;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.FaceAttachable.AttachedFace;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.RedstoneWallTorch;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.block.data.type.RedstoneWire.Connection;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Map.Entry;
import java.util.Random;

public class CryptRoom extends RoomPopulatorAbstract {

    public CryptRoom(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(@NotNull PopulatorDataAbstract data, @NotNull CubeRoom room) {

        // Crypt entry
        BlockFace face = BlockUtils.getDirectBlockFace(rand);

        // Create the Crypt itself
        for (Entry<Wall, Integer> entry : room.getFourWalls(data, 3).entrySet()) {
            Wall w = entry.getKey();
            for (int i = 0; i < entry.getValue(); i++) {

                w.Pillar(
                        room.getHeight(),
                        rand,
                        Material.SANDSTONE,
                        Material.CUT_SANDSTONE,
                        Material.CHISELED_SANDSTONE
                );

                if (w.getDirection() == face) {
                    // Thicker entryway
                    w.getFront()
                     .Pillar(room.getHeight(),
                             rand,
                             Material.SANDSTONE,
                             Material.CUT_SANDSTONE,
                             Material.CHISELED_SANDSTONE
                     );


                    if (i == 1) { // Redstone wiring
                        RedstoneWire wire = (RedstoneWire) Bukkit.createBlockData(Material.REDSTONE_WIRE);
                        wire.setFace(face, Connection.SIDE);
                        wire.setFace(face.getOppositeFace(), Connection.SIDE);
                        w.getFront().setBlockData(wire);

                        wire = (RedstoneWire) Bukkit.createBlockData(Material.REDSTONE_WIRE);
                        wire.setFace(BlockUtils.getAdjacentFaces(face)[0], Connection.SIDE);
                        wire.setFace(face.getOppositeFace(), Connection.UP);
                        w.getFront(2).getDown().setBlockData(wire);

                        wire = (RedstoneWire) Bukkit.createBlockData(Material.REDSTONE_WIRE);
                        wire.setFace(BlockUtils.getAdjacentFaces(face)[0], Connection.SIDE);
                        wire.setFace(BlockUtils.getAdjacentFaces(face)[1].getOppositeFace(), Connection.SIDE);
                        w.getFront().getUp(3).setBlockData(wire);

                        RedstoneWallTorch rTorch = (RedstoneWallTorch) Bukkit.createBlockData(Material.REDSTONE_WALL_TORCH);
                        rTorch.setFacing(face);
                        w.getFront().getUp().setBlockData(rTorch);

                        Switch lever = (Switch) Bukkit.createBlockData(Material.LEVER);
                        lever.setAttachedFace(AttachedFace.WALL);
                        lever.setFacing(face.getOppositeFace());
                        lever.setPowered(false);
                        w.getRear().getUp().setBlockData(lever);

                        // Cover
                        w.getFront(3)
                         .Pillar(room.getHeight(),
                                 rand,
                                 Material.SANDSTONE,
                                 Material.CUT_SANDSTONE,
                                 Material.CHISELED_SANDSTONE
                         );

                    }
                    else if (i == 2) { // Redstone wiring
                        RedstoneWire wire = (RedstoneWire) Bukkit.createBlockData(Material.REDSTONE_WIRE);
                        wire.setFace(BlockUtils.getAdjacentFaces(face)[1], Connection.SIDE);
                        wire.setFace(face.getOppositeFace(), Connection.SIDE);
                        w.getFront(2).getDown().setBlockData(wire);

                        wire = (RedstoneWire) Bukkit.createBlockData(Material.REDSTONE_WIRE);
                        wire.setFace(face, Connection.SIDE);
                        wire.setFace(face.getOppositeFace(), Connection.SIDE);
                        w.getFront().getDown().setBlockData(wire);

                        wire = (RedstoneWire) Bukkit.createBlockData(Material.REDSTONE_WIRE);
                        wire.setFace(BlockUtils.getAdjacentFaces(face)[0], Connection.SIDE);
                        wire.setFace(BlockUtils.getAdjacentFaces(face)[1].getOppositeFace(), Connection.SIDE);
                        w.getFront().getUp(3).setBlockData(wire);

                        // Cover
                        w.getFront(3)
                         .Pillar(room.getHeight(),
                                 rand,
                                 Material.SANDSTONE,
                                 Material.CUT_SANDSTONE,
                                 Material.CHISELED_SANDSTONE
                         );
                        w.getFront(2)
                         .Pillar(room.getHeight(),
                                 rand,
                                 Material.SANDSTONE,
                                 Material.CUT_SANDSTONE,
                                 Material.CHISELED_SANDSTONE
                         );

                    }
                    else if (i == 3) { // Entry holes & pistons
                        // Drill hole
                        w.Pillar(2, rand, Material.AIR);
                        w.getFront().Pillar(2, rand, Material.AIR);

                        // Pistons
                        Piston faceDown = (Piston) Bukkit.createBlockData(Material.STICKY_PISTON);
                        faceDown.setFacing(BlockFace.DOWN);
                        w.getFront().getUp(3).setBlockData(faceDown);

                        Piston faceUp = (Piston) Bukkit.createBlockData(Material.STICKY_PISTON);
                        faceUp.setFacing(BlockFace.UP);
                        w.getFront().getDown(2).setBlockData(faceUp);
                        w.getUp(2)
                         .getFront(2)
                         .Pillar(room.getHeight() - 2,
                                 rand,
                                 Material.SANDSTONE,
                                 Material.CUT_SANDSTONE,
                                 Material.CHISELED_SANDSTONE
                         );

                    }
                    else { // even thicker layer
                        w.getFront(2)
                         .Pillar(room.getHeight(),
                                 rand,
                                 Material.SANDSTONE,
                                 Material.CUT_SANDSTONE,
                                 Material.CHISELED_SANDSTONE
                         );
                    }
                }
                else if (w.getDirection() == face.getOppositeFace()) {
                    // Create treasure chest inside the crypt
                    if (GenUtils.chance(rand, 1, 10)) {
                        SimpleBlock pos = w.getFront().get();
                        Directional chest = (Directional) Bukkit.createBlockData(Material.CHEST);
                        chest.setFacing(face.getOppositeFace());
                        pos.setBlockData(chest);
                        data.lootTableChest(pos.getX(), pos.getY(), pos.getZ(), TerraLootTable.DESERT_PYRAMID);
                    }
                }

                w = w.getLeft();
            }
        }

        // Persistent Trap Zombies
        for (int i = 0; i < GenUtils.randInt(rand, 1, 4); i++) {
            data.addEntity(room.getX(), room.getY(), room.getZ(), EntityType.HUSK);
        }
    }


    @Override
    public boolean canPopulate(@NotNull CubeRoom room) {
        return room.getWidthX() >= 11 && room.getWidthZ() >= 11;
    }
}
