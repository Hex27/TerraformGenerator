package org.terraform.structure.room.jigsaw;

import org.bukkit.block.BlockFace;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.room.CubeRoom;
import org.terraform.utils.BlockUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

public abstract class JigsawStructurePiece implements Cloneable {
    protected CubeRoom room;
    protected HashMap<BlockFace, Boolean> validDirections = new HashMap<BlockFace, Boolean>();
    protected ArrayList<BlockFace> walledFaces = new ArrayList<>();
    protected JigsawStructurePiece[] allowedPieces;
    protected JigsawType type;
    protected int depth = 0;
    protected BlockFace rotation = BlockFace.NORTH;
    protected boolean unique = false;
    protected int elevation = 0; //elevation of 0 is ground level.

    public JigsawStructurePiece(int widthX, int height, int widthZ, JigsawType type, boolean unique, BlockFace... validDirs) {
        this.room = new CubeRoom(widthX, widthZ, height, 0, 0, 0);
        this.type = type;
        this.unique = unique;
        for(BlockFace face : validDirs)
            validDirections.put(face, false);
        //TerraformGeneratorPlugin.logger.info("CONSTRUCTOR-validDirsSize: " + validDirections.size());
    }

    public JigsawStructurePiece(int widthX, int height, int widthZ, JigsawType type, BlockFace... validDirs) {
        this.room = new CubeRoom(widthX, widthZ, height, 0, 0, 0);
        this.type = type;
        for(BlockFace face : validDirs)
            validDirections.put(face, false);
        //TerraformGeneratorPlugin.logger.info("CONSTRUCTOR-validDirsSize: " + validDirections.size());
    }

    public void postBuildDecoration(Random random, PopulatorDataAbstract data) {
    }

    ;

    @SuppressWarnings("unchecked")
    public JigsawStructurePiece getInstance(Random rand, int depth) {
        JigsawStructurePiece clone;
        try {
            clone = (JigsawStructurePiece) this.clone();
            clone.room = new CubeRoom(room.getWidthX(), room.getWidthZ(), room.getHeight(), 0, 0, 0);
            clone.validDirections = (HashMap<BlockFace, Boolean>) validDirections.clone();
            for(BlockFace face : validDirections.keySet()) {
                clone.validDirections.put(face, false);
            }
            //TerraformGeneratorPlugin.logger.info("CREATOR-validDirsSize: " + validDirections.size());
            clone.walledFaces = new ArrayList<>();
            clone.setRotation(BlockUtils.getDirectBlockFace(rand));
            clone.elevation = 0;
            clone.setDepth(depth);
            return clone;
        } catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public abstract void build(PopulatorDataAbstract data, Random rand);

    public BlockFace getNextUnpopulatedBlockFace() {
        for(Entry<BlockFace, Boolean> entry : validDirections.entrySet()) {
            if(!entry.getValue())
                return entry.getKey();
        }

        TerraformGeneratorPlugin.logger.error("Tried to get unpopulated block face when there aren't any left!");
        return null;
    }

    public void setPopulated(BlockFace face) {
        if(this.type == JigsawType.END) return;
        if(!validDirections.containsKey(face))
            TerraformGeneratorPlugin.logger.error("Tried to set an invalid blockface as populated for a jigsaw piece.");

        validDirections.put(face, true);
    }

    public boolean hasUnpopulatedDirections() {
        if(this.type == JigsawType.END) return false;
        for(Boolean populated : validDirections.values()) {
            if(!populated) return true;
        }
        return false;
    }

    public CubeRoom getRoom() {
        return room;
    }

    public HashMap<BlockFace, Boolean> getValidDirections() {
        if(type == JigsawType.END) return new HashMap<BlockFace, Boolean>();
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

    @Override
    public String toString() {
        String directions = "";
        for(BlockFace face : this.validDirections.keySet()) {
            directions += face + ",";
        }
        return this.getClass().getSimpleName()
                + "::" + room.getX() + "," + room.getY() + "," + room.getZ()
                + "::" + this.hasUnpopulatedDirections() + "::" + directions;
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
