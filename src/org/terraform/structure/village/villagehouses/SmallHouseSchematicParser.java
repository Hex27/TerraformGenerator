package org.terraform.structure.village.villagehouses;

import java.util.Random;

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

public class SmallHouseSchematicParser extends SchematicParser{

    private final BiomeBank biome;
    private final Random rand;
    private final PopulatorDataAbstract pop;

    public SmallHouseSchematicParser(BiomeBank biome, Random rand,
                                    PopulatorDataAbstract pop) {
        this.biome = biome;
        this.rand = rand;
        this.pop = pop;
    }

    
    
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
        } else if (data.getMaterial().toString().contains("OAK")) {
            data = Bukkit.createBlockData(
                    data.getAsString().replaceAll(
                            data.getMaterial().toString().toLowerCase(),
                            BlockUtils.getWoodForBiome(biome,
                                    data.getMaterial().toString()
                                            .replaceAll("OAK_", "")
                            ).toString().toLowerCase())
            );
            super.applyData(block, data);
        } else if (data.getMaterial() == Material.CHEST) {
            if (GenUtils.chance(rand, 1, 5)) {
                block.setType(Material.AIR);
                return; //A fifth of chests are not placed.
            }
            super.applyData(block, data);
            pop.lootTableChest(block.getX(), block.getY(), block.getZ(), TerraLootTable.VILLAGE_PLAINS_HOUSE);
        } else {
            super.applyData(block, data);
        }
    }
}
