package org.terraform.structure.small;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TConfigOption;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.SchematicParser;
import org.terraform.schematic.TerraSchematic;
import org.terraform.utils.GenUtils;

import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Random;

public class DesertWellPopulator {

    public void populate(TerraformWorld tw, Random random,
                         PopulatorDataAbstract data, boolean badlandsWell) {
        if (!TConfigOption.STRUCTURES_DESERTWELL_ENABLED.getBoolean()) return;
        int x = data.getChunkX() * 16 + random.nextInt(16);
        int z = data.getChunkZ() * 16 + random.nextInt(16);
        int height = GenUtils.getHighestGround(data, x, z);
        
        spawnDesertWell(tw, random, data, x, height, z, badlandsWell);
    }

    public void spawnDesertWell(TerraformWorld tw, Random random,
                              PopulatorDataAbstract data, int x, int y, int z, boolean badlandsWell) {

    	y--;
        SimpleBlock core = new SimpleBlock(data, x, y, z);
        TerraformGeneratorPlugin.logger.info("Spawning Desert Well at " + core.getCoords());
        try {
            TerraSchematic desertWell = TerraSchematic.load("desert_well", core);
            desertWell.parser = new DesertWellSchematicParser(random, badlandsWell);
            //swamphut.setFace(face);
            desertWell.apply();
            
            //Reposition center (Because shit's fucky)
            core = core.getRelative(1,0,1);
            
            //Make sure the well is standing on a stable base
            for(int nx = -3; nx <= 3; nx++) {
            	for(int nz = -3; nz <= 3; nz++) {
            		if(!badlandsWell)
                    	new Wall(core.getRelative(nx,-1,nz)).downLPillar(random, 10, Material.SANDSTONE,Material.CHISELED_SANDSTONE,Material.CUT_SANDSTONE,Material.SMOOTH_SANDSTONE);
            		else
                    	new Wall(core.getRelative(nx,-1,nz)).downLPillar(random, 10, Material.RED_SANDSTONE,Material.CHISELED_RED_SANDSTONE,Material.CUT_RED_SANDSTONE,Material.SMOOTH_RED_SANDSTONE);
                }
            }
            
            //Drill hole down
            int depth = GenUtils.randInt(random, 5,30);
            for(int i = 0; i < depth; i++) {
            	if(i < depth-3)
            		core.getRelative(0,-i,0).setType(Material.CAVE_AIR);
            	else
            		core.getRelative(0,-i,0).setType(Material.WATER);
            }
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static class DesertWellSchematicParser extends SchematicParser {
        private final Random rand;
        //private final PopulatorDataAbstract pop;
        private final boolean badlandsWell;

        public DesertWellSchematicParser(Random rand, boolean badlandsWell) {
            this.rand = rand;
            this.badlandsWell = badlandsWell;
        }

        @Override
        public void applyData(SimpleBlock block, BlockData data) {
        	
        	if(this.badlandsWell) {
        		data = Bukkit.createBlockData(
                        StringUtils.replace(
                        		data.getAsString(), 
                        		"sandstone", 
                        		"red_sandstone"
                        )
                );
            	if(data.getMaterial() == Material.RED_SANDSTONE && rand.nextInt(5) == 0) {
            		data = Bukkit.createBlockData(Material.CHISELED_RED_SANDSTONE);
                    super.applyData(block, data);
            		return;
            	}

                if (data.getMaterial() != Material.RED_SANDSTONE_STAIRS
                		&& data.getMaterial() != Material.RED_SANDSTONE_WALL
                		&& data.getMaterial().toString().contains("RED_SANDSTONE")) {
                    data = Bukkit.createBlockData(
                            StringUtils.replace(
                            		data.getAsString(), 
                            		"red_sandstone", 
                            		GenUtils.randMaterial(
                            				rand, 
                            				Material.RED_SANDSTONE, 
                            				Material.SMOOTH_RED_SANDSTONE,
                            				Material.CUT_RED_SANDSTONE
                            				).name().toLowerCase(Locale.ENGLISH)
                            )
                    );
                    super.applyData(block, data);
                }else
                	super.applyData(block, data);
            	return;
        	}
        	else 
        	{
            	if(data.getMaterial() == Material.SANDSTONE && rand.nextInt(5) == 0) {
            		data = Bukkit.createBlockData(Material.CHISELED_SANDSTONE);
                    super.applyData(block, data);
            		return;
            	}

                if (data.getMaterial() != Material.SANDSTONE_STAIRS
                		&& data.getMaterial() != Material.SANDSTONE_WALL
                		&& data.getMaterial().toString().contains("SANDSTONE")) {
                    data = Bukkit.createBlockData(
                            StringUtils.replace(
                            		data.getAsString(), 
                            		"sandstone", 
                            		GenUtils.randMaterial(
                            				rand, 
                            				Material.SANDSTONE, 
                            				Material.SMOOTH_SANDSTONE,
                            				Material.CUT_SANDSTONE
                            				).name().toLowerCase(Locale.ENGLISH)
                            )
                    );
                    super.applyData(block, data);
                }else 
                	super.applyData(block, data);
            	return;
        	}
        	
        	
        }
    }
}
