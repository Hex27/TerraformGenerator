package org.terraform.cave;

import org.bukkit.Material;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.noise.FastNoise;

public class NoiseCaveRegistry {
    private final TerraformWorld tw;
    private final NoiseCaveAbstract[] noiseCaveCarvers;
    private final NoiseCaveAbstract[] generateCaveCarvers;

    public NoiseCaveRegistry(TerraformWorld tw) {
        this.tw = tw;
        this.noiseCaveCarvers = new NoiseCaveAbstract[]{
                new CheeseCave()
        };
        this.generateCaveCarvers = new NoiseCaveAbstract[]{};
    }

    public boolean canNoiseCarve(int x, int y, int z, double height){
        float filterHeight = yBarrier(tw, x,y,z, (float)height, 10, 5);
        float filterGround = yBarrier(tw, x,y,z, (float) TerraformGeneratorPlugin.injector.getMinY(), 20, 5);
        float filter = filterHeight*filterGround;
        for(NoiseCaveAbstract carver:noiseCaveCarvers) {
            if(carver.canCarve(tw, x, y, z, height, filter)) return true;
        }
        return false;
    }

    /**
     * Used to prevent functions from passing certain thresholds.
     * Useful for stuff like preventing caves from breaking out
     * the surface or under minimum Y
     * @return a value between 0 and 1 inclusive. 1 for all clear,
     * less than 1 to smooth an approach towards a barrier.
     */
    public float yBarrier(TerraformWorld tw, float x, float y, float z, float v, float barrier, float limit){

        FastNoise boundaryNoise = new FastNoise((int) tw.getSeed()*5);
        boundaryNoise.SetNoiseType(FastNoise.NoiseType.Simplex);
        boundaryNoise.SetFrequency(0.01f);
        barrier += 3*boundaryNoise.GetNoise(x,z); //fuzz the boundary

        if(Math.abs(y-v) <= limit)
            return 0;
        else {
            float abs = Math.abs(y - v);
            if(abs < barrier+limit)
                return (abs-limit)/barrier;
            else
                return 1;
        }
    }
}
