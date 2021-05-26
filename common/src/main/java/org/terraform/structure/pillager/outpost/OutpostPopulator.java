package org.terraform.structure.pillager.outpost;

import org.bukkit.Location;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.villagehouse.VillageHousePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import java.util.Random;

public class OutpostPopulator extends VillageHousePopulator {
    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {
        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());
        int[] coords = mc.getCenterBlockCoords(); //getCoordsFromMegaChunk(tw, mc);
        int x = coords[0];//data.getChunkX()*16 + random.nextInt(16);
        int z = coords[1];//data.getChunkZ()*16 + random.nextInt(16);
        int height = GenUtils.getHighestGround(data, x, z);
        spawnOutpost(tw, this.getHashedRandom(tw, data.getChunkX(), data.getChunkZ()), data, x, height + 1, z);
    }

    public void spawnOutpost(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z) {
        try {
            BiomeBank biome = tw.getBiomeBank(x, z);
            TerraSchematic outpostBase = TerraSchematic.load("outpost/outpostbase1", new Location(tw.getWorld(), x, y, z));
            outpostBase.parser = new OutpostSchematicParser(biome, random, data);
            outpostBase.setFace(BlockUtils.getDirectBlockFace(random));
            outpostBase.apply();
            TerraSchematic outpostCore = TerraSchematic.load("outpost/outpostcore1", new Location(tw.getWorld(), x, y+5, z));
            outpostCore.parser = new OutpostSchematicParser(biome, random, data);
            outpostCore.setFace(BlockUtils.getDirectBlockFace(random));
            outpostCore.apply();
            
            TerraformGeneratorPlugin.logger.info("Spawning outpost at " + x + "," + y + "," + z + " with rotation of " + outpostBase.getFace());

            //data.addEntity(x, y + 1, z, EntityType.VILLAGER); //Two villagers
            //data.addEntity(x, y + 1, z, EntityType.VILLAGER);
            //data.addEntity(x, y + 1, z, EntityType.CAT); //And a cat.

        } catch (Throwable e) {
            TerraformGeneratorPlugin.logger.error("Something went wrong trying to place outpost at " + x + "," + y + "," + z + "!");
            e.printStackTrace();
        }
    }

}
