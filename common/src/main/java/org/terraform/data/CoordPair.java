package org.terraform.data;

public record CoordPair(int x, int z) {
    public CoordPair chunkify(){
        return new CoordPair(x >> 4, z >> 4);
    }
    public double distance(int x, int z){
        return Math.sqrt(distanceSquared(x,z));
    }
    public double distanceSquared(int x, int z){
        return Math.pow(this.x-x,2)+Math.pow(this.z-z,2);
    }
}
