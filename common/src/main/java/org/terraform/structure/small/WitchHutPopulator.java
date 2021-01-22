package org.terraform.structure.small;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TConfigOption;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.SchematicParser;
import org.terraform.schematic.TerraSchematic;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Random;

public class WitchHutPopulator {

    public void populate(TerraformWorld tw, Random random,
                         PopulatorDataAbstract data) {
        if(!TConfigOption.STRUCTURES_SWAMPHUT_ENABLED.getBoolean()) return;
        int seaLevel = TerraformGenerator.seaLevel;
        int x = data.getChunkX() * 16 + random.nextInt(16);
        int z = data.getChunkZ() * 16 + random.nextInt(16);
        int height = GenUtils.getHighestGround(data, x, z);
        if(height < seaLevel) { //Assume. it's on water
            height = seaLevel + GenUtils.randInt(random, 2, 3);
        } else
            height += GenUtils.randInt(random, 2, 3);

        spawnSwampHut(tw, random, data, x, height, z);
    }

    public void spawnSwampHut(TerraformWorld tw, Random random,
                              PopulatorDataAbstract data, int x, int y, int z) {

        //Refers to center of hut, above the water.
        SimpleBlock core = new SimpleBlock(data, x, y, z);
        TerraformGeneratorPlugin.logger.info("Spawning Swamp Hut at " + core.getCoords());
        try {
            BlockFace face = BlockUtils.getDirectBlockFace(random);
            TerraSchematic swamphut = TerraSchematic.load("swamphut", core);
            swamphut.parser = new WitchHutSchematicParser(random, data);
            swamphut.setFace(face);
            swamphut.apply();
            Wall w = new Wall(core.getRelative(0, -2, 0), face).getRear();

            //Pillars down
            w.getFront().getRight().downUntilSolid(random, Material.OAK_LOG);
            w.getFront().getLeft(2).downUntilSolid(random, Material.OAK_LOG);
            w.getRear(2).getRight().downUntilSolid(random, Material.OAK_LOG);
            w.getRear(2).getLeft(2).downUntilSolid(random, Material.OAK_LOG);

            x = w.getRear(2).get().getX();
            z = w.getRear(2).get().getZ();
            data.addEntity(x, y + 1, z, EntityType.WITCH);
            data.addEntity(x, y + 1, z, EntityType.CAT);
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static class WitchHutSchematicParser extends SchematicParser {
        private final Random rand;
        private final PopulatorDataAbstract pop;

        public WitchHutSchematicParser(Random rand,
                                       PopulatorDataAbstract pop) {
            this.rand = rand;
            this.pop = pop;
        }

        @Override
        public void applyData(SimpleBlock block, BlockData data) {
            if(data.getMaterial().toString().contains("COBBLESTONE")) {
                data = Bukkit.createBlockData(
                        StringUtils.replace(data.getAsString(), "cobblestone", GenUtils.randMaterial(rand, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE,
                                Material.MOSSY_COBBLESTONE).name().toLowerCase(Locale.ENGLISH))
                );
                super.applyData(block, data);

                if(GenUtils.chance(1, 5)) BlockUtils.vineUp(block, 2);
            } else if(data.getMaterial().toString().startsWith("OAK")) {
                super.applyData(block, data);
                if(data.getMaterial().toString().endsWith("LOG")) {
                    if(GenUtils.chance(1, 5)) BlockUtils.vineUp(block, 2);
                }
                super.applyData(block, data);
            } else if(data.getMaterial() == Material.CHEST) {
                super.applyData(block, data);
                pop.lootTableChest(block.getX(), block.getY(), block.getZ(), TerraLootTable.VILLAGE_TEMPLE);
            } else {
                super.applyData(block, data);
            }
        }
    }
}
