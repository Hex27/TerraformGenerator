package org.terraform.data;

import java.util.Objects;

public class SimpleLocation {
	
	private int x;
	private int y;
	private int z;
	public SimpleLocation(int x, int y, int z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(x,y,z,93929798);
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof SimpleLocation){
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
	 * @return the y
	 */
	public int getY() {
		return y;
	}
	/**
	 * @return the z
	 */
	public int getZ() {
		return z;
	}
	/**
	 * @param x the x to set
	 */
	public void setX(int x) {
		this.x = x;
	}
	/**
	 * @param y the y to set
	 */
	public void setY(int y) {
		this.y = y;
	}
	/**
	 * @param z the z to set
	 */
	public void setZ(int z) {
		this.z = z;
	}
	
	
}
