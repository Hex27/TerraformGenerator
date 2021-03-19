package org.terraform.utils;

import org.terraform.biome.BiomeBank;
import org.terraform.data.TerraformWorld;

public class GaussianBlur {
	HEAVILY UNOPTIMISED, AND FRANKLY DOESN'T QUITE WORK. REDO.
    private static final int blurRadius = 5;
    private static final int blurLength = 11;
    private static final double[][] weightArr = calculateWeightMatrix();

    private static double[][] getColorMatrix(TempBlurSource source, int x, int z){

        int startX = x-blurRadius;
        int startZ = z -blurRadius;
        int counter = 0;

        double[][] arr = new double[blurLength][blurLength];

        for (int i=startX ; i<startX+blurLength ;i++){
            for (int j = startZ; j < startZ+blurLength; j++){
                arr[counter%blurLength][counter/blurLength] = source.getValue(i,j);
                counter++;
            }
        }

        return arr;
    }

    private static double[][] calculateWeightMatrix() {
    	double[][] weightArr = new double[blurLength][blurLength];
        for (int i=0;i<blurRadius*2+1;i++){
            for (int j=0;j<blurRadius*2+1;j++){

                weightArr[i][j] = getWeight(j-blurRadius,blurRadius-i);
            }
        }

        double weightSum = 0;

        for (int i = 0;i<blurLength;i++){
            for (int j=0; j<blurLength; j++ ){
                weightSum+=weightArr[i][j];
            }
        }

        for (int i = 0;i<blurLength;i++){
            for (int j=0; j<blurLength; j++ ){
                weightArr[i][j] = weightArr[i][j]/weightSum;
            }
        }
		return weightArr;

    
    }

    private static double getWeight(int x,int y){

        double sigma = (blurRadius*2+1)/2;
        double weight = (1/(2*Math.PI*sigma*sigma))*Math.pow(Math.E,((-(x*x+y*y))/((2*sigma)*(2*sigma))));

        return weight;
    }

    private static double getBlurredPixel(TempBlurSource source, int x, int y){

        double blurGray = 0;
        double[][] colorMat = getColorMatrix(source, x,y);

        for (int nx = 0; nx<blurLength; nx++){
            for (int nz = 0; nz<blurLength; nz++ ){
                blurGray += weightArr[nx][nz]*colorMat[nx][nz];
            }
        }

        return blurGray;
    }

    public static void applyBlur(TempBlurSource source, int centX, int centZ){
        for (int x = centX-blurRadius; x <= centX+blurRadius; x++) {
            for (int z = centZ-blurRadius; z <= centZ+blurRadius; z++) {
                source.setValue(x, z, 
                		getBlurredPixel(source, blurRadius+x,blurRadius+z));
            }
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
			return BiomeBank.getBiomeSection(tw, x, z).getBiomeBank().getHandler().calculateHeight(tw, x, z);
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
    
    public static abstract class TempBlurSource{
    	
    	public abstract double getValue(int x, int z);
    	
    	public abstract void setValue(int x, int z, double val);
    	
    }

}