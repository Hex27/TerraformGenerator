package org.terraform.structure.pillager.mansion;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleLocation;
import org.terraform.data.Wall;
import org.terraform.main.config.TConfig;
import org.terraform.structure.room.jigsaw.JigsawStructurePiece;
import org.terraform.structure.room.jigsaw.JigsawType;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.blockdata.SlabBuilder;
import org.terraform.utils.blockdata.StairBuilder;

import java.util.*;
import java.util.Map.Entry;

public abstract class MansionStandardRoomPiece extends JigsawStructurePiece {

    public static int spawnedGuards = 0;
    public @NotNull HashMap<BlockFace, MansionStandardRoomPiece> adjacentPieces = new HashMap<>();
    public @NotNull HashMap<BlockFace, MansionInternalWallState> internalWalls = new HashMap<>();
    // Mansion standard pieces decorate themselves with a special populator.
    // If it is null, it will not do anything.
    private @Nullable MansionRoomPopulator roomPopulator = null;
    private boolean isPopulating = false;

    public MansionStandardRoomPiece(int widthX, int height, int widthZ, JigsawType type, BlockFace[] validDirs) {
        super(widthX, height, widthZ, type, validDirs);
    }

    public void setupInternalAttributes(@NotNull PopulatorDataAbstract data,
                                        @NotNull HashMap<SimpleLocation, JigsawStructurePiece> pieces)
    {
        for (BlockFace face : BlockUtils.directBlockFaces) {
            SimpleLocation otherLoc = this.getRoom()
                                          .getSimpleLocation()
                                          .getRelative(face, MansionJigsawBuilder.groundFloorRoomWidth);
            if (!pieces.containsKey(otherLoc)) {
                // This is likely to be a window. However, do checks to ensure that this
                // isn't the main entrance or a balcony entrance.
                SimpleBlock center = this.getRoom().getCenterSimpleBlock(data).getUp();
                if (center.getRelative(face, 5).isSolid()) {
                    this.internalWalls.put(face, MansionInternalWallState.WINDOW);
                }
                else {
                    this.internalWalls.put(face, MansionInternalWallState.EXIT);
                }
                continue;
            }
            this.adjacentPieces.put(face, (MansionStandardRoomPiece) pieces.get(otherLoc));
            this.internalWalls.put(face, MansionInternalWallState.SOLID);
        }
    }

    public void buildWalls(Random random, @NotNull PopulatorDataAbstract data) {
        for (BlockFace face : this.internalWalls.keySet()) {
            if (internalWalls.get(face) == MansionInternalWallState.WINDOW
                || internalWalls.get(face) == MansionInternalWallState.EXIT)
            {
                continue;
            }
            Entry<Wall, Integer> entry = this.getRoom().getWall(data, face, 0);
            Wall w = entry.getKey();
            Wall center = null;
            for (int i = 0; i < entry.getValue(); i++) {
                w.Pillar(this.getRoom().getHeight(), Material.DARK_OAK_PLANKS);

                if (i == entry.getValue() / 2
                    && this.internalWalls.get(face) == MansionInternalWallState.ROOM_ENTRANCE)
                {
                    center = w.clone();
                }

                w = w.getLeft();
            }
            if (center != null) {

                center.Pillar(5, Material.AIR);
                center.getLeft().Pillar(5, Material.AIR);
                center.getRight().Pillar(5, Material.AIR);
                center.getLeft(2).Pillar(5, Material.AIR);
                center.getRight(2).Pillar(5, Material.AIR);

                new SlabBuilder(Material.DARK_OAK_SLAB).setType(Type.TOP).apply(center.getUp(5));

                new StairBuilder(Material.DARK_OAK_STAIRS).setHalf(Half.TOP)
                                                          .setFacing(BlockUtils.getLeft(center.getDirection()))
                                                          .apply(center.getUp(4).getLeft(2))
                                                          .setFacing(BlockUtils.getRight(center.getDirection()))
                                                          .apply(center.getUp(4).getRight(2));

                center.getLeft(3).Pillar(this.getRoom().getHeight(), Material.DARK_OAK_LOG);
                center.getRight(3).Pillar(this.getRoom().getHeight(), Material.DARK_OAK_LOG);
            }
        }
    }

    public void decorateInternalRoom(Random random, PopulatorDataAbstract data) {
        /*
         * if(roomPopulator == null)
         * TerraformGeneratorPlugin.logger.info("Null room at " +
         * this.getRoom().getCenterSimpleBlock(data)); else
         * TerraformGeneratorPlugin.logger.info(roomPopulator.getClass().getSimpleName()
         * + " invoked at " + this.getRoom().getCenterSimpleBlock(data));
         */
        if (roomPopulator != null && this.isPopulating) {
            roomPopulator.decorateRoom(data, random);
        }
    }

    public boolean areInternalWallsFullyBlocked() {
        for (BlockFace face : this.internalWalls.keySet()) {
            if (this.internalWalls.get(face) == MansionInternalWallState.ROOM_ENTRANCE) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @Nullable JigsawStructurePiece getInstance(@NotNull Random rand, int depth) {
        MansionStandardRoomPiece clone = (MansionStandardRoomPiece) super.getInstance(rand, depth);
        if (clone == null) {
            return null;
        }
        clone.adjacentPieces = new HashMap<>();
        clone.internalWalls = new HashMap<>();
        return clone;
    }

    public @NotNull Collection<BlockFace> getShuffledInternalWalls() {
        ArrayList<BlockFace> shuffled = new ArrayList<>(internalWalls.keySet());
        Collections.shuffle(shuffled);
        return shuffled;
    }

    public @Nullable MansionRoomPopulator getRoomPopulator() {
        return roomPopulator;
    }

    public void setRoomPopulator(MansionRoomPopulator roomPopulator) {
        setRoomPopulator(roomPopulator, true);
    }

    public void setRoomPopulator(MansionRoomPopulator roomPopulator, boolean isPopulating) {
        // TerraformGeneratorPlugin.logger.info("Setting " + roomPopulator.getClass().getSimpleName() + " at " + this.getRoom().getSimpleLocation());
        this.roomPopulator = roomPopulator;
        this.isPopulating = isPopulating;
    }

    /**
     * For special wall decorations depending on the room populator.
     */
    public void decorateWalls(Random random, @NotNull PopulatorDataAbstract data) {
        // UNLIKE ROOM POPULATOR, this will run even if isPopuating is false.
        // This is because the other cells must decorate their walls.
        if (this.roomPopulator == null) {
            return;
        }

        for (BlockFace face : BlockUtils.directBlockFaces) {
            Wall target;
            if (!this.internalWalls.containsKey(face)) {
                continue;
            }
            switch (this.internalWalls.get(face)) {
                case EXIT:
                    target = new Wall(this.getRoom().getCenterSimpleBlock(data).getUp(), face.getOppositeFace());
                    this.roomPopulator.decorateExit(random, target.getRear(4));
                    break;
                case ROOM_ENTRANCE:
                    target = new Wall(this.getRoom().getCenterSimpleBlock(data).getUp(), face.getOppositeFace());
                    this.roomPopulator.decorateEntrance(random, target.getRear(3));
                    break;
                case SOLID:
                    target = new Wall(this.getRoom().getCenterSimpleBlock(data).getUp(), face.getOppositeFace());
                    this.roomPopulator.decorateWall(random, target.getRear(3));
                    break;
                case WINDOW:
                    target = new Wall(this.getRoom().getCenterSimpleBlock(data).getUp(), face.getOppositeFace());
                    this.roomPopulator.decorateWindow(random, target.getRear(4));
                    break;
            }
        }
    }

    public boolean isPopulating() {
        return isPopulating;
    }

    public void spawnGuards(@NotNull Random rand, @NotNull PopulatorDataAbstract data) {
        if (this.roomPopulator == null) {
            return;
        }

        EntityType type = EntityType.VINDICATOR;
        int[] spawnLoc = this.roomPopulator.getSpawnLocation();

        // Always one evoker if the area is 3x3 or 2x2.
        // Second floor grand stairway will also spawn the evoker downstairs.
        if (this.isPopulating && (this.roomPopulator.getSize().equals(new MansionRoomSize(3, 3))
                                  || this.roomPopulator.getSize().equals(new MansionRoomSize(2, 2))))
        {
            type = EntityType.EVOKER;
        }

        // 1x1 rooms don't spawn as often
        if (this.roomPopulator.getSize().equals(new MansionRoomSize(1, 1))) {
            if (GenUtils.chance(rand, 4, 5)) {
                return;
            }
        }

        SimpleBlock target = new SimpleBlock(data, spawnLoc[0], spawnLoc[1], spawnLoc[2]);
        int limit = 5;
        BlockFace dir = BlockUtils.getDirectBlockFace(rand);
        while (limit > 0 && ((target.getType() != Material.AIR && target.getType() != Material.RED_CARPET)
                             || (target.getUp().getType() != Material.AIR))) {
            target = target.getRelative(dir).getUp();
            limit--;
        }

        if (limit > 0) {
            // target = new Wall(target).findFloor(20).getUp().get();
            target.addEntity(type);
            spawnedGuards++;
            if (!this.roomPopulator.getSize().equals(new MansionRoomSize(1, 1))) {
                for (int i = 0; i < TConfig.c.STRUCTURES_MANSION_SPAWNAGGRESSION; i++) {
                    if (rand.nextBoolean()) {
                        target.addEntity(EntityType.VINDICATOR);
                        spawnedGuards++;
                    }
                }
            }
            // new Wall(target).findFloor(20).getUp().get().addEntity(EntityType.ARMOR_STAND);
        }
    }
}
