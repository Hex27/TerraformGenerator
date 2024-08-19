package org.terraform.structure.room.jigsaw;

import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

/**
 * This class has nothing to do with the new system
 */
public abstract class JigsawStructurePiece implements Cloneable {
    protected final JigsawType type;
    protected CubeRoom room;
    protected HashMap<BlockFace, Boolean> validDirections = new HashMap<>();
    protected ArrayList<BlockFace> walledFaces = new ArrayList<>();
    protected JigsawStructurePiece[] allowedPieces;
    protected int depth = 0;
    protected BlockFace rotation = BlockFace.NORTH;
    protected boolean unique = false;
    protected int elevation = 0; // elevation of 0 is ground level.

    public JigsawStructurePiece(int widthX,
                                int height,
                                int widthZ,
                                JigsawType type,
                                boolean unique,
                                BlockFace @NotNull ... validDirs)
    {
        this.room = new CubeRoom(widthX, widthZ, height, 0, 0, 0);
        this.type = type;
        this.unique = unique;
        for (BlockFace face : validDirs) {
            validDirections.put(face, false);
        }
    }

    public JigsawStructurePiece(int widthX, int height, int widthZ, JigsawType type, BlockFace @NotNull ... validDirs) {
        this.room = new CubeRoom(widthX, widthZ, height, 0, 0, 0);
        this.type = type;
        for (BlockFace face : validDirs) {
            validDirections.put(face, false);
        }
    }

    public void postBuildDecoration(Random random, PopulatorDataAbstract data) {
    }

    @SuppressWarnings("unchecked")
    public @Nullable JigsawStructurePiece getInstance(@NotNull Random rand, int depth) {
        JigsawStructurePiece clone;
        try {
            clone = (JigsawStructurePiece) this.clone();
            clone.room = new CubeRoom(room.getWidthX(), room.getWidthZ(), room.getHeight(), 0, 0, 0);
            clone.validDirections = (HashMap<BlockFace, Boolean>) validDirections.clone();
            for (BlockFace face : validDirections.keySet()) {
                clone.validDirections.put(face, false);
            }
            // TerraformGeneratorPlugin.logger.info("CREATOR-validDirsSize: " + validDirections.size());
            clone.walledFaces = new ArrayList<>();
            clone.setRotation(BlockUtils.getDirectBlockFace(rand));
            clone.elevation = 0;
            clone.setDepth(depth);
            return clone;
        }
        catch (CloneNotSupportedException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }
        return null;
    }

    public abstract void build(PopulatorDataAbstract data, Random rand);

    public @Nullable BlockFace getNextUnpopulatedBlockFace() {
        for (Entry<BlockFace, Boolean> entry : validDirections.entrySet()) {
            if (!entry.getValue()) {
                return entry.getKey();
            }
        }

        TerraformGeneratorPlugin.logger.error("Tried to get unpopulated block face when there aren't any left!");
        return null;
    }

    public void setPopulated(BlockFace face) {
        if (this.type == JigsawType.END) {
            return;
        }
        if (!validDirections.containsKey(face)) {
            TerraformGeneratorPlugin.logger.error("Tried to set an invalid blockface as populated for a jigsaw piece.");
        }

        validDirections.put(face, true);
    }

    public boolean hasUnpopulatedDirections() {
        if (this.type == JigsawType.END) {
            return false;
        }
        for (Boolean populated : validDirections.values()) {
            if (!populated) {
                return true;
            }
        }
        return false;
    }

    public CubeRoom getRoom() {
        return room;
    }

    public HashMap<BlockFace, Boolean> getValidDirections() {
        if (type == JigsawType.END) {
            return new HashMap<>();
        }
        return validDirections;
    }

    public JigsawStructurePiece[] getAllowedPieces() {
        return allowedPieces;
    }

    public JigsawType getType() {
        return type;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getElevation() {
        return elevation;
    }

    public void setElevation(int elevation) {
        this.elevation = elevation;
    }

    public BlockFace getRotation() {
        return rotation;
    }

    public void setRotation(BlockFace rotation) {
        this.rotation = rotation;
    }

    /**
     * @return If the structure piece has at least 1 walled face, return
     * a larger room with bigger widthX and widthZ (same height)
     */
    public CubeRoom getExtendedRoom(int extraSize) {
        if (this.walledFaces.isEmpty()) {
            return this.room;
        }
        else {
            return new CubeRoom(this.room.getWidthX() + extraSize * 2,
                    this.room.getWidthZ() + extraSize * 2,
                    this.room.getHeight(),
                    this.room.getX(),
                    this.room.getY(),
                    this.room.getZ()
            );
        }
    }

    @Override
    public @NotNull String toString() {
        StringBuilder directions = new StringBuilder();
        for (BlockFace face : this.validDirections.keySet()) {
            directions.append(face).append(",");
        }
        return this.getClass().getSimpleName()
               + "::"
               + room.getX()
               + ","
               + room.getY()
               + ","
               + room.getZ()
               + "::"
               + this.hasUnpopulatedDirections()
               + "::"
               + directions;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public ArrayList<BlockFace> getWalledFaces() {
        return walledFaces;
    }

    public void setWalledFaces(ArrayList<BlockFace> walledFaces) {
        this.walledFaces = walledFaces;
    }
}
