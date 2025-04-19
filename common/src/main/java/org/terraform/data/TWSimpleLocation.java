package org.terraform.data;

import org.jetbrains.annotations.NotNull;

public record TWSimpleLocation(TerraformWorld tw, int x,int y, int z) {

    public TWSimpleLocation(TerraformWorld tw, @NotNull SimpleLocation loc) {
        this(tw,loc.getX(),loc.getY(),loc.getZ());
    }

}
