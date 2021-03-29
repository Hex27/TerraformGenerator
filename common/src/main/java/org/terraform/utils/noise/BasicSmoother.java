package org.terraform.utils.noise;

import java.util.HashMap;

import org.terraform.biome.BiomeBank;
import org.terraform.data.SimpleLocation;
import org.terraform.data.TerraformWorld;

public class BasicSmoother {
	
	private static final int blurRadius = 5;
	private static final int candidateCount = (int) Math.pow(1+2*blurRadius, 2);

    private static double getBlurredPixel(TempBlurSource source, int x, int z){
    	double totalHeight = 0;
    	for(int nx = x-blurRadius; nx <= x+blurRadius; nx++) {
    		for(int nz = z-blurRadius; nz <= z+blurRadius; nz++) {
    			totalHeight += source.getValue(nx, nz);
        	}
    	}
    	return totalHeight/candidateCount;
    }

    public static void applyBlur(TempBlurSource source, int centX, int centZ){
    	int passes = 3;
    	TempBlurSource tempSrc = source;
        for(int i = 0; i < passes; i++) {
        	TempBlurSource temp = new HashMapBlurSource();
        	if(i == passes-1)
        		temp = source; //Apply back to main source
        	
        	for (int x = centX-blurRadius; x <= centX+blurRadius; x++) {
                for (int z = centZ-blurRadius; z <= centZ+blurRadius; z++) {
                    temp.setValue(x, z, 
                    		getBlurredPixel(tempSrc, x, z));
                }
            }
    		tempSrc = temp;
        }
    }
    
    public static class HeightMapBlurSource extends TempBlurSource {
    	TerraformWorld tw;
    	int x;
    	int z;
    	double result = 0;
    	public HeightMapBlurSource(TerraformWorld tw, int x, int z) {
    		this.tw = tw;
    		this.x = x;
    		this.z = z;
    	}
    	
		@Override
		public double getValue(int x, int z) {
			return BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z).getBiomeBank().getHandler().calculateHeight(tw, x, z);
		}

		@Override
		public void setValue(int x, int z, double val) {
			if(x == this.x && z == this.z) {
				result = val;
			}
		}
		
		public double getResult() {
			return result;
		}
    }
    
    public static class HashMapBlurSource extends TempBlurSource{

    	HashMap<SimpleLocation, Double> map = new HashMap<>();
    	
		@Override
		public double getValue(int x, int z) {
			
			return map.getOrDefault(new SimpleLocation(x,0,z), 0.0);
		}

		@Override
		public void setValue(int x, int z, double val) {
			map.put(new SimpleLocation(x,0,z), val);
		}
    	
    }
    
    public static abstract class TempBlurSource{
    	
    	public abstract double getValue(int x, int z);
    	
    	public abstract void setValue(int x, int z, double val);
    	
    }

}