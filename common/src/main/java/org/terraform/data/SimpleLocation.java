package org.terraform.data;

import org.bukkit.block.BlockFace;
import org.terraform.main.TerraformGeneratorPlugin;

import java.util.Objects;

public class SimpleLocation {

    protected int x;
    protected int y;
    protected int z;

    public SimpleLocation(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }


    public SimpleLocation(SimpleLocation other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    public SimpleLocation getRelative(int x, int y, int z) {
        return new SimpleLocation(this.x + x, this.y + y, this.z + z);
    }

    public SimpleLocation getRelative(BlockFace face) {
        return new SimpleLocation(this.x + face.getModX(), this.y + face.getModY(), this.z + face.getModZ());
    }
    public SimpleLocation getRelative(BlockFace face,int i) {
        return new SimpleLocation(this.x + face.getModX()*i, this.y + face.getModY()*i, this.z + face.getModZ()*i);
    }

    public float distance(SimpleLocation o) {
        return (float) Math.sqrt(Math.pow(o.x - x, 2) + Math.pow(o.y - y, 2) + Math.pow(o.z - z, 2));
    }

    public float distanceSqr(SimpleLocation o) {
        return (float) (Math.pow(o.x - x, 2) + Math.pow(o.y - y, 2) + Math.pow(o.z - z, 2));
    }

    public float distanceQuad(SimpleLocation o) {
        return (float) Math.pow(Math.pow(o.x - x, 2) + Math.pow(o.y - y, 2) + Math.pow(o.z - z, 2),4);
       }

    public float distanceSqr(int nx, int ny, int nz) {
        return (float) (Math.pow(nx - x, 2) + Math.pow(ny - y, 2) + Math.pow(nz - z, 2));
    }
    
    /**
     * Returns a value between 0 and 2PI to represent a 360 degree angle
     * offset of the other location "o" compared to this location.
     */
    public float twoDAngleTo(SimpleLocation o) {
    	
    	//Handle absolute cases first
    	if(o.x == x && o.z == z) {
    		return 0.0f;
    	}else if(o.x == x && o.z > z){
    		return 0.0f;
    	}else if(o.x == x && o.z < z){
    		return (float) Math.PI;
    	}else if(o.x > x && o.z == z){
    		return (float) Math.PI/2;
    	}else if(o.x < x && o.z == z){
    		return (float) (3*Math.PI/2);
    	}
    	//Handle CAST trigo calculations
    	else if(o.x > x && o.z > z) { //A segment
    		return (float) Math.atan((o.x-x)/(o.z-z));
    	}else if(o.x > x && o.z < z) { //C segment
    		return (float) (Math.atan((z-o.z)/(o.x-x)) + Math.PI/2);
    	}else if(o.x < x && o.z < z) { //T segment
    		return (float) (Math.atan((x-o.x)/(z-o.z)) + Math.PI);
    	}else if(o.x < x && o.z > z) { //S segment
    		return (float) (Math.atan((o.z-z)/(x-o.x))+3*Math.PI/2);
    	}
    	
    	// no way something else happens?
    	TerraformGeneratorPlugin.logger.error("2D Angle calculation failed! Input Values: " + o.x + "," + o.z + ":" + x + "," + z);
    	return 0.0f;
    }
    
    /**
     * Returns a value between 0 and PI to represent a 180 degree angle
     * offset of the other location "o" compared to this location.
     * 
     * Will return the same value for segment CA and ST (mirrored)
     */
    public float twoDAngleWrapTo(SimpleLocation o) {
    	
    	//Handle absolute cases first
    	if(o.x == x && o.z == z) {
    		return 0.0f;
    	}else if(o.x == x && o.z > z){
    		return 0;
    	}else if(o.x == x && o.z < z){
    		return (float) Math.PI;
    	}else if(o.x > x && o.z == z){
    		return (float) Math.PI/2;
    	}else if(o.x < x && o.z == z){
    		return (float) (Math.PI/2);
    	}
    	//Handle CAST trigo calculations
    	else if(o.x > x && o.z > z) { //A segment
    		return (float) Math.atan((o.x-x)/(o.z-z));
    	}else if(o.x > x && o.z < z) { //C segment
    		return (float) (Math.atan((z-o.z)/(o.x-x)) + Math.PI/2);
    	}else if(o.x < x && o.z < z) { //T segment
    		return (float) (Math.atan((z-o.z)/(x-o.x)) + Math.PI/2);
    	}else if(o.x < x && o.z > z) { //S segment
    		return (float) Math.atan((x-o.x)/(o.z-z));
    	}
    	
    	// no way something else happens?
    	TerraformGeneratorPlugin.logger.error("2D Angle calculation failed! Input Values: " + o.x + "," + o.z + ":" + x + "," + z);
    	return 0.0f;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, 93929798);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SimpleLocation) {
            SimpleLocation sLoc = (SimpleLocation) obj;
            return sLoc.x == x && sLoc.y == y && sLoc.z == z;
        }
        return false;
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * @return the z
     */
    public int getZ() {
        return z;
    }

    /**
     * @param z the z to set
     */
    public void setZ(int z) {
        this.z = z;
    }
    
    @Override
    public String toString() {
    	return this.x + "," + this.y + "," + this.z;
    }


}
