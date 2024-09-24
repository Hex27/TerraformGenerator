package org.terraform.cave;

import org.terraform.data.TerraformWorld;

/**
 * These caves will carve during generateNoise as a boolean function.
 */
public abstract class NoiseCaveAbstract {

    /**
     * @param surfaceFilter is a function of y that forces noise cave carvers to respect
     *                      the world height and bedrock barriers.
     * @return if not null, that means a cave can be carved at this location with
     * specified material.
     */
    public abstract boolean canCarve(TerraformWorld tw, int rawX, int y, int rawZ, double height, float surfaceFilter);
}
