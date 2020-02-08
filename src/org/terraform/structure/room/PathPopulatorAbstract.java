package org.terraform.structure.room;


public abstract class PathPopulatorAbstract {
	
	public int getPathWidth(){
		return 3;
	}
	
	public abstract void populate(PathPopulatorData ppd);
	
}
