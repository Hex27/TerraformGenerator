package org.terraform.cave;

import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.noise.FastNoise;

public class NoiseCaveRegistry {
    private final TerraformWorld tw;
    private final NoiseCaveAbstract[] noiseCaveCarvers;

    public NoiseCaveRegistry(TerraformWorld tw) {
        this.tw = tw;
        this.noiseCaveCarvers = new NoiseCaveAbstract[]{
                new CheeseCave()
        };
    }

    public boolean canCarve(int x, int y, int z, double height){
        float filterHeight = barrier(tw, x,y,z, (float)height, 10, 5);
        float filterGround = barrier(tw, x,y,z, (float) TerraformGeneratorPlugin.injector.getMinY(), 20, 5);
        float filter = filterHeight*filterGround;
        for(NoiseCaveAbstract carver:noiseCaveCarvers)
            if(carver.canCarve(tw,x,y,z,filter)) return true;

        return false;
    }

    /**
     * Used to prevent functions from passing certain thresholds.
     * Useful for stuff like preventing caves from breaking into
     * the ocean or under minimum Y
     * @return a value between 0 and 1 inclusive.
     */
    public float barrier(TerraformWorld tw, float x, float y, float z, float v, float barrier, float limit){

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
