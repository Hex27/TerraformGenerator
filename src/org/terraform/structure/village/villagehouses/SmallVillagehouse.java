package org.terraform.structure.village.villagehouses;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.SchematicParser;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.village.VillagePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import net.minecraft.server.v1_16_R1.BlockBase.BlockData;

public class SmallVillagehouse extends VillagePopulator{
    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {
        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());
        int[] coords = getCoordsFromMegaChunk(tw, mc);
        int x = coords[0];//data.getChunkX()*16 + random.nextInt(16);
        int z = coords[1];//data.getChunkZ()*16 + random.nextInt(16);
        int height = GenUtils.getHighestGround(data, x, z);
        spawnSmallHouse(tw, this.getHashedRandom(tw, data.getChunkX(), data.getChunkZ()), data, x, height + 1, z);
    }

    
    public void spawnSmallHouse(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x , int y , int z) {
    	try {
    		BiomeBank biome = tw.getBiomeBank(x, y, z);
    		y += GenUtils.randInt(random, 1, 5);
    		TerraSchematic smallHouse = TerraSchematic.load("smallhouse", new Location(tw.getWorld(), x , y , z));
    		smallHouse.parser = new SmallHouseSchematicParser(biome,random,data);
    		smallHouse.setFace(BlockUtils.getDirectBlockFace(random));
    		smallHouse.apply();
    		
    		TerraformGeneratorPlugin.logger.info("Spawning Village Small house at " + x + " Y: " + y + " Z:" +  z);
            for (int nx = -17 / 2 - 1; nx <= 17 / 2 + 1; nx++) {
                for (int nz = -17 / 2 - 1; nz <= 17 / 2 + 1; nz++) {
                    if (data.getType(x + nx, y - 1, z + nz).toString().contains("PLANKS") ||
                            data.getType(x + nx, y - 1, z + nz).toString().contains("STONE_BRICKS"))
                        BlockUtils.setDownUntilSolid(x + nx, y - 2, z + nz, data, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
                    else if (data.getType(x + nx, y - 1, z + nz).toString().contains("LOG"))
                        BlockUtils.setDownUntilSolid(x + nx, y - 2, z + nz, data, data.getType(x + nx, y - 1, z + nz));
                }
            }	
            data.addEntity(x, y + 1, z, EntityType.VILLAGER);
            data.addEntity(x, y + 1, z, EntityType.CAT); //And a cat
        
    	} catch (Throwable e) {
    		
    	}
    }
}


