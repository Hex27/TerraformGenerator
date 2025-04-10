package org.terraform.data;

public record CoordPair(int x, int z) {
    public CoordPair chunkify(){
        return new CoordPair(x >> 4, z >> 4);
    }
}
