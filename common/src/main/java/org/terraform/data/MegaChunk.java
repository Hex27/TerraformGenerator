package org.terraform.data;

import org.terraform.biome.BiomeBank;
import org.terraform.biome.BiomeSection;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.utils.GenUtils;

import java.util.Random;

/**
 * Refers to a cluster of 64x64 chunks
 * 1024 blocks wide.
 * Used for spawning structures.
 */
public class MegaChunk {
	private static final int megaChunkBlockWidth = BiomeSection.sectionWidth*TConfigOption.STRUCTURES_MEGACHUNK_NUMBIOMESECTIONS.getInt(); 
    private int x, z;

    public MegaChunk(SimpleChunkLocation sLoc) {
        this(sLoc.getX(), sLoc.getZ());
    }

    public MegaChunk(int x, int y, int z) {
        this.x = blockCoordsToMega(x);
        this.z = blockCoordsToMega(z);
    }
    
    //A megachunk consists of a bunch of biome sections.
    //The big structures spawn right in the middle of them.
    public MegaChunk(int chunkX, int chunkZ) {
        this((chunkX << 4) | 15, 0, (chunkZ << 4) | 15);
    	
    	//this.x = chunkX >> TConfigOption.STRUCTURES_MEGACHUNK_BITSHIFTS.getInt();
        //this.z = chunkZ >> TConfigOption.STRUCTURES_MEGACHUNK_BITSHIFTS.getInt();
    }

    public MegaChunk getRelative(int x, int z) {
        MegaChunk mc = new MegaChunk(0, 0);
        mc.x = this.x + x;
        mc.z = this.z + z;
        return mc;
    }

    /**
     * @param rand
     * @return A random pair of xz block coords within the mega chunk
     */
    public int[] getRandomCoords(Random rand) {
        
        int lowX = megaToBlockCoords(this.x);
        int lowZ = megaToBlockCoords(this.z);
        int highX = lowX + megaChunkBlockWidth-1;
        int highZ = lowZ + megaChunkBlockWidth-1;

        //Pad the sides. Never generate on the side of a mega chunk.
        int x = GenUtils.randInt(rand, lowX + megaChunkBlockWidth/5, highX - megaChunkBlockWidth/5);
        int z = GenUtils.randInt(rand, lowZ + megaChunkBlockWidth/5, highZ - megaChunkBlockWidth/5);
        return new int[]{x, z};
    }
    
    public int[] getCenterBlockCoords() {
        
        int lowX = megaToBlockCoords(this.x);
        int lowZ = megaToBlockCoords(this.z);
        //TerraformGeneratorPlugin.logger.info("MC(" + this.x + "," + this.z + "):(" + (lowX + megaChunkBlockWidth/2) + "," + (lowZ + megaChunkBlockWidth/2) + ")");
        return new int[]{lowX + megaChunkBlockWidth/2, lowZ + megaChunkBlockWidth/2};
    }
    
    public BiomeSection getCenterBiomeSection(TerraformWorld tw) {
    	int[] coords = getCenterBlockCoords();
    	return BiomeBank.getBiomeSectionFromBlockCoords(tw,coords[0],coords[1]);
    }

    public boolean containsXZBlockCoords(int x, int z) {
        MegaChunk mc = new MegaChunk(x, 0, z);
        return mc.equals(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MegaChunk) {
            MegaChunk megaChunk = (MegaChunk) obj;
            return this.x == megaChunk.x && this.z == megaChunk.z;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 5;

        result = prime * result + x;
        result = prime * result + z;

        return result;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }
    
    private static int blockCoordsToMega(int coord) {
    	if(coord >= 0) {
    		return (int) Math.floor(coord/megaChunkBlockWidth);
    	}
    	else
    	{
    		return -1*((int) Math.ceil(Math.abs(coord)/megaChunkBlockWidth));
    	}
    }

    /**
     * @param coord
     * @return lower bounds of block coords within the megachunk.
     */
    private static int megaToBlockCoords(int coord) {
		return coord*(megaChunkBlockWidth);
    }
}
