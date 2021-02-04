package org.terraform.structure.village.plains;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.schematic.SchematicParser;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.room.CubeRoom;
import org.terraform.structure.room.RoomPopulatorAbstract;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.Version;

import java.io.FileNotFoundException;
import java.util.Random;

public class PlainsVillageWellPopulator extends RoomPopulatorAbstract {


    public PlainsVillageWellPopulator(Random rand, boolean forceSpawn, boolean unique) {
        super(rand, forceSpawn, unique);
    }

    @Override
    public void populate(PopulatorDataAbstract data, CubeRoom room) {
        int x = room.getX();
        int z = room.getZ();
        int y = GenUtils.getHighestGround(data, x, z);
        
        try {
            SimpleBlock core = new SimpleBlock(data, x,y+1,z);
			TerraSchematic schem = TerraSchematic.load("plainsvillage-well1", core);
			schem.parser = new PlainsVillageWellSchematicParser();
			schem.apply();
			
			int depth = GenUtils.randInt(rand, 5, 20);
			
			for(int i = 0; i < depth; i++) {
				core.getRelative(0,-i,0).setType(Material.WATER);
				for(BlockFace face:BlockUtils.xzPlaneBlockFaces) {
					core.getRelative(0,-i,0).getRelative(face).setType(Material.WATER);
				}
			}
			
			for(int nx = -3; nx <= 3; nx++) {
				for(int nz = -3; nz <= 3; nz++) {
					Wall target = new Wall(core.getRelative(nx,0,nz));
					if(target.getType() == Material.COBBLESTONE
							||target.getType() == Material.MOSSY_COBBLESTONE) {
						target.getRelative(0,-1,0).downUntilSolid(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
					}
				}
			}
			
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		}

    }

    @Override
    public boolean canPopulate(CubeRoom room) {
        return room.getWidthX() <= 10;
    }
    
    private class PlainsVillageWellSchematicParser extends SchematicParser{
        @Override
        public void applyData(SimpleBlock block, BlockData data) {
            if (data.getMaterial().toString().contains("COBBLESTONE")) {
                data = Bukkit.createBlockData(
                        data.getAsString().replaceAll(
                                "cobblestone",
                                GenUtils.randMaterial(rand, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE)
                                        .toString().toLowerCase()
                        )
                );
                super.applyData(block, data);
            } else if(data.getMaterial() == Material.IRON_BARS){
            	if(Version.isAtLeast(16)) {
            		data = Bukkit.createBlockData(Material.CHAIN);
            	}
                super.applyData(block, data);
            }else {
                super.applyData(block, data);
            }
        }
    }
}
