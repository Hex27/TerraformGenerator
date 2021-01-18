package org.terraform.structure.village.plains;

import org.bukkit.block.BlockFace;
import org.terraform.structure.room.CubeRoom;

public class DirectionalCubeRoom extends CubeRoom {

    private BlockFace direction;

    public DirectionalCubeRoom(BlockFace direction, int widthX, int widthZ, int height, int x, int y, int z) {
        super(widthX, widthZ, height, x, y, z);
        // TODO Auto-generated constructor stub
        this.setDirection(direction);
    }

    public BlockFace getDirection() {
        return direction;
    }

    public void setDirection(BlockFace direction) {
        this.direction = direction;
    }

}
