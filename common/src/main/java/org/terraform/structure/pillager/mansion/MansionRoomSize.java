package org.terraform.structure.pillager.mansion;

public class MansionRoomSize {

    private final int widthX;
    private final int widthZ;

    public MansionRoomSize(int widthX, int widthZ) {
        super();
        this.widthX = widthX;
        this.widthZ = widthZ;
    }

    public int getWidthX() {
        return widthX;
    }

    public int getWidthZ() {
        return widthZ;
    }

    @Override
    public int hashCode() {
        return widthX + 74077 * widthZ;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof MansionRoomSize) {
            return ((MansionRoomSize) other).widthX == widthX && ((MansionRoomSize) other).widthZ == widthZ;
        }
        return false;
    }

}
