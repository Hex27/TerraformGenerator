package org.terraform.cave;

import org.terraform.data.TerraformWorld;
import org.terraform.utils.noise.FastNoise;

/**
 * These caves will carve during generateNoise as a boolean function.
 */
public abstract class NoiseCaveAbstract {

    /**
     * @param filter is a function of y that forces noise cave carvers to respect
     *               the world height and bedrock barriers.
     * @return if true, that means a cave can be carved at this location.
     */
    public abstract boolean canCarve(TerraformWorld tw, int rawX, int y, int rawZ, float filter);


}
