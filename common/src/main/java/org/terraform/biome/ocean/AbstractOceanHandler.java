package org.terraform.biome.ocean;

import org.terraform.biome.BiomeHandler;
import org.terraform.biome.BiomeType;
import org.terraform.coregen.HeightMap;
import org.terraform.data.TerraformWorld;

public abstract class AbstractOceanHandler extends BiomeHandler {

    protected final BiomeType oceanType;

    public AbstractOceanHandler(BiomeType oceanType) {
        this.oceanType = oceanType;
    }

    @Override
    public double calculateHeight(TerraformWorld tw, int x, int z) {

        double height = HeightMap.CORE.getHeight(tw, x, z) - 25;

        if (oceanType == BiomeType.DEEP_OCEANIC) {
            height -= 20;
        }

        // If the height is too low, force it back to 3.
        if (height <= 0) {
            height = 3;
        }

        return height;
    }
}
