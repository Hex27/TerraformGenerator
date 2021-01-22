package org.terraform.structure.animalfarm;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.SimpleBlock;
import org.terraform.schematic.SchematicParser;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class AnimalFarmSchematicParser extends SchematicParser {

    private final BiomeBank biome;
    private final Random rand;
    private final PopulatorDataAbstract pop;

    public AnimalFarmSchematicParser(BiomeBank biome, Random rand,
                                     PopulatorDataAbstract pop) {
        this.biome = biome;
        this.rand = rand;
        this.pop = pop;
    }

    @Override
    public void applyData(SimpleBlock block, BlockData data) {
        if(data.getMaterial() == Material.COBBLESTONE) {
            if(GenUtils.chance(rand, 1, 5)) data = Bukkit
                    .createBlockData(Material.STONE);
        }
        if(data.getMaterial().toString().contains("COBBLESTONE")) {
            data = Bukkit.createBlockData(
                    data.getAsString().replaceAll(
                            "cobblestone",
                            GenUtils.randMaterial(rand, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE)
                                    .toString().toLowerCase()
                    )
            );
            super.applyData(block, data);
            return;
        } else if(data.getMaterial().toString().contains("STONE_BRICK")) {
            if(GenUtils.chance(rand, 1, 5))
                data = Bukkit.createBlockData(
                        data.getAsString().replaceAll(
                                "stone_brick",
                                "mossy_stone_brick"
                        )
                );
            super.applyData(block, data);
            return;
        } else if(data.getMaterial().toString().contains("OAK")) {
            data = Bukkit.createBlockData(
                    data.getAsString().replaceAll(
                            data.getMaterial().toString().toLowerCase(),
                            BlockUtils.getWoodForBiome(biome,
                                    data.getMaterial().toString()
                                            .replaceAll("OAK_", "")
                            ).toString().toLowerCase())
            );
            super.applyData(block, data);
            return;
        } else if(data.getMaterial() == Material.CHEST) {
            if(GenUtils.chance(rand, 1, 5)) {
                block.setType(Material.AIR);
                return; //A fifth of chests are not placed.
            }
            super.applyData(block, data);
            int i = rand.nextInt(3);
            if(i == 0)
                pop.lootTableChest(block.getX(), block.getY(), block.getZ(),
                        TerraLootTable.VILLAGE_BUTCHER);
            else if(i == 1)
                pop.lootTableChest(block.getX(), block.getY(), block.getZ(),
                        TerraLootTable.VILLAGE_TANNERY);
            else if(i == 2)
                pop.lootTableChest(block.getX(), block.getY(), block.getZ(),
                        TerraLootTable.VILLAGE_SHEPHERD);
            return;
        } else {
            super.applyData(block, data);
        }
    }

}