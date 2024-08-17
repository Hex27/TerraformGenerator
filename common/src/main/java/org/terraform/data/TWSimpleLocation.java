package org.terraform.data;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TWSimpleLocation extends SimpleLocation {

	final TerraformWorld tw;
	public TWSimpleLocation(TerraformWorld tw, int x, int y, int z) {
		super(x, y, z);
		this.tw = tw;
		// TODO Auto-generated constructor stub
	}
	
	public TWSimpleLocation(TerraformWorld tw, @NotNull SimpleLocation loc) {
		super(loc);
		this.tw = tw;
	}
	

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, tw.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TWSimpleLocation sLoc) {
            return sLoc.x == x
            		&& sLoc.y == y 
            		&& sLoc.z == z 
            		&& tw.getName().equals(sLoc.getTerraformWorld().getName());
        }
        return false;
    }
    
    public TerraformWorld getTerraformWorld() {
    	return tw;
    }

}
