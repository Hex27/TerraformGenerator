package org.terraform.cave;

import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.noise.FastNoise;

public class NoiseCaveRegistry {
    private final TerraformWorld tw;
    private final NoiseCaveAbstract @NotNull [] noiseCaveCarvers;
    private final NoiseCaveAbstract @NotNull [] generateCaveCarvers;

    public NoiseCaveRegistry(TerraformWorld tw) {
        this.tw = tw;
        this.noiseCaveCarvers = new NoiseCaveAbstract[]{
                new CheeseCave()
        };
        this.generateCaveCarvers = new NoiseCaveAbstract[]{
                new NoiseRavine()
        };
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

    public boolean canGenerateCarve(int x, int y, int z, double height){

        //The sea filter is special, pass in HEIGHT as it's about scaling towards the sea
        float filterSea = yBarrier(tw, x,(int)height,z, TerraformGenerator.seaLevel, 5, 1);
        for(NoiseCaveAbstract carver:generateCaveCarvers) {
            if(carver.canCarve(tw, x, y, z, height, filterSea)) return true;
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
    public float yBarrier(@NotNull TerraformWorld tw, float x, float y, float z, float v, float barrier, float limit){

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
