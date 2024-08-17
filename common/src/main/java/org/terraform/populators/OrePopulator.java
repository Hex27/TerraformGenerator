package org.terraform.populators;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;
import org.terraform.utils.version.Version;

import java.util.Objects;
import java.util.Random;

public class OrePopulator {

    private final Material type;
    private final int baseChance; //Chance to spawn per attempt to spawn
    private final int maxOreSize; //Maximum size of one vein
    private final int minOreSize;
    private final int maxNumberOfVeins; //Maximum number of veins per chunk
    private final int peakSpawnChanceHeight; //Optimal height for ore to spawn
    private final int maxSpawnHeight; //max y height where ore can be rarely found
    private int minRange; //min spawn height
    private final BiomeBank[] requiredBiomes;
    private final int maxDistance;
    private final boolean ignorePeakSpawnChance;

    public OrePopulator(Material type, int baseChance, int maxOreSize,
                        int maxNumberOfVeins, int peakSpawnChanceHeight, int maxSpawnHeight, 
                        boolean ignorePeakSpawnChance, BiomeBank... requiredBiomes) {
        this.type = type;
        this.baseChance = baseChance;
        this.maxOreSize = maxOreSize;
        this.minOreSize = maxOreSize/2;
        this.maxNumberOfVeins = maxNumberOfVeins;
        this.peakSpawnChanceHeight = peakSpawnChanceHeight;
        this.maxSpawnHeight = maxSpawnHeight;
        this.requiredBiomes = requiredBiomes;
        this.ignorePeakSpawnChance = ignorePeakSpawnChance;
        this.minRange = TerraformGeneratorPlugin.injector.getMinY()+1;
        this.maxDistance = Math.max(Math.abs(minRange - peakSpawnChanceHeight), Math.abs(maxSpawnHeight - peakSpawnChanceHeight));
    }

    public OrePopulator(Material type, int baseChance, int maxOreSize,
                        int maxNumberOfVeins, int minRange, int peakSpawnChanceHeight, 
                        int maxSpawnHeight, boolean ignorePeakSpawnChance, 
                        BiomeBank... requiredBiomes) {
        this.type = type;
        this.baseChance = baseChance;
        this.maxOreSize = maxOreSize;
        this.minOreSize = maxOreSize/2;
        this.maxNumberOfVeins = maxNumberOfVeins;
        this.minRange = minRange;
        this.peakSpawnChanceHeight = peakSpawnChanceHeight;
        this.maxSpawnHeight = maxSpawnHeight;
        this.requiredBiomes = requiredBiomes;
        this.ignorePeakSpawnChance = ignorePeakSpawnChance;
        //this.minRange = TerraformGeneratorPlugin.injector.getMinY()+1;
        this.maxDistance = Math.max(Math.abs(minRange - peakSpawnChanceHeight), Math.abs(maxSpawnHeight - peakSpawnChanceHeight));
    }
    
    public void populate(@NotNull TerraformWorld world, @NotNull Random random, @NotNull PopulatorDataAbstract data) {
    	if(requiredBiomes.length > 0) {
    		BiomeBank b = BiomeBank.getBiomeSectionFromChunk(world, data.getChunkX(), data.getChunkZ()).getBiomeBank();
    		boolean canPopulate = false;
    		for(BiomeBank comp:requiredBiomes)
                if(comp == b) {
                    canPopulate = true;
                    break;
                }
    		
    		if(!canPopulate) return;
    	}
    	
    	//Attempt maxNumberOfVeins number of times
    	for (int i = 0; i < this.maxNumberOfVeins; i++) {
    		
    		//Roll chance to spawn one ore
    		if (GenUtils.chance(random, this.baseChance, 100)) {
    			//RNG determined X Y and Z within chunk
    			int x = GenUtils.randInt(random, 0, 15) + data.getChunkX() * 16;
            	int z = GenUtils.randInt(random, 0, 15) + data.getChunkZ() * 16;
            	//int groundHeight = GenUtils.getHighestGround(data, x, z);
            	int range = maxSpawnHeight;
            	//Low chance for ores to spawn above max range
            	//if (GenUtils.chance(random, 1, 5)) range = maxSpawnHeight;
            	
            	//Ignore ground height and just spawn
            	//Range cannot be above ground
            	//if(range > groundHeight) range = groundHeight;
            	
            	//Spawn failed.
            	if(minRange > range) continue;
            	if(minRange < world.minY) minRange = world.minY;
            	
            	int y = GenUtils.randInt(random, minRange + 64, range + 64) - 64; //The 64 is to make sure no negative numbers are fed in.

            	if(!ignorePeakSpawnChance) {
            		//Calculate chance based on spawnHeight and peakSpawnChanceHeight height. Max chance at peakSpawnChanceHeight.
                	int distance = Math.abs(y - peakSpawnChanceHeight);
                	
                	if(!GenUtils.chance((int) Math.round(100.0*(1.0 - ((float)distance)/((float)maxDistance))), 100)) {

                        continue;
                	}
            	}
            	
            	//Generate ore with rough sphere size.
            	placeOre(
            			Objects.hash(world.getSeed(),x,y,z), 
            			data, x, y, z);
            	
    		}
    	}
    }
    
    //Don't use simpleblock to forcefully compress memory usage and GC invocations by this.
    public void placeOre(int seed, @NotNull PopulatorDataAbstract data, int coreX, int coreY, int coreZ) {
    	double size = GenUtils.randDouble(new Random(seed), minOreSize, maxOreSize);
    	//Size is the volume of the sphere, so radius is:
    	double radius = Math.pow(((3.0/4.0)*size*(1.0/Math.PI)), 1.0/3.0);
    	
        if (radius <= 0) return;
        if (radius <= 0.5) {
            //block.setReplaceType(ReplaceType.ALL);
            data.setType(coreX,coreY,coreZ,GenUtils.randMaterial(new Random(seed), type));
            return;
        }
        
        FastNoise noise = new FastNoise(seed);
        noise.SetNoiseType(NoiseType.Simplex);
        noise.SetFrequency(0.09f);

        for (double x = -radius; x <= radius; x++) {
            for (double y = -radius; y <= radius; y++) {
                for (double z = -radius; z <= radius; z++) {
                    int relX = (int) Math.round(x) + coreX;
                    int relY = (int) Math.round(y) + coreY;
                    int relZ = (int) Math.round(z) + coreZ;
                    if(relY > TerraformGeneratorPlugin.injector.getMaxY()
                    || relY <= TerraformGeneratorPlugin.injector.getMinY()) //do not touch bedrock layer
                        continue;
                	//SimpleBlock rel = block.getRelative((int)Math.round(x), (int)Math.round(y), (int)Math.round(z));
                    double equationResult = Math.pow(x, 2) / Math.pow(radius,2)
                            + Math.pow(y, 2) / Math.pow(radius, 2)
                            + Math.pow(z, 2) / Math.pow(radius, 2);
                    Material oreType = data.getType(relX,relY,relZ);
                    if (equationResult <= 1 + 0.7 * noise.GetNoise(relX, relY, relZ)) {
                        if (oreType == Material.STONE) {
                            data.setType(relX,relY,relZ,type);
                        }
                        //1.17 behaviour
                        else if(Version.isAtLeast(17)) 
                        {
                        	//Deepslate replacing other ores
                        	if(type == Material.DEEPSLATE
                        			&& BlockUtils.ores.contains(oreType)) {
                        		data.setType(relX,relY,relZ,BlockUtils.deepSlateVersion(oreType));
                        	}
                        	//Normal ores replacing deepslate
                        	else if(oreType == Material.DEEPSLATE)
                        	{
                        		data.setType(relX,relY,relZ,BlockUtils.deepSlateVersion(type));
                        	}
                        } 
                        
                    }
                }
            }
        }
    }

	public Material getType() {
		return type;
	}

	public int getBaseChance() {
		return baseChance;
	}

	public int getMaxOreSize() {
		return maxOreSize;
	}

	public int getMinOreSize() {
		return minOreSize;
	}

	public int getMaxNumberOfVeins() {
		return maxNumberOfVeins;
	}

	public int getPeakSpawnChanceHeight() {
		return peakSpawnChanceHeight;
	}

	public int getMaxSpawnHeight() {
		return maxSpawnHeight;
	}

	public int getMinRange() {
		return minRange;
	}

	public BiomeBank[] getRequiredBiomes() {
		return requiredBiomes;
	}

	public int getMaxDistance() {
		return maxDistance;
	}

	public boolean isIgnorePeakSpawnChance() {
		return ignorePeakSpawnChance;
	}
}
