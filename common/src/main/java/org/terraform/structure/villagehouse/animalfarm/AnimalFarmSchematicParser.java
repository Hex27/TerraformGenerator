package org.terraform.structure.villagehouse.animalfarm;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.schematic.SchematicParser;
import org.terraform.utils.GenUtils;
import org.terraform.utils.WoodUtils;
import org.terraform.utils.WoodUtils.WoodType;

import java.util.Locale;
import java.util.Random;

public class AnimalFarmSchematicParser extends SchematicParser {

    private final BiomeBank biome;
    private final Random rand;
    private final PopulatorDataAbstract pop;

    public AnimalFarmSchematicParser(BiomeBank biome, Random rand, PopulatorDataAbstract pop) {
        this.biome = biome;
        this.rand = rand;
        this.pop = pop;
    }

    @Override
    public void applyData(@NotNull SimpleBlock block, @NotNull BlockData data) {
        if (data.getMaterial() == Material.COBBLESTONE) {
            if (GenUtils.chance(rand, 1, 5)) {
                data = Bukkit.createBlockData(Material.STONE);
            }
        }
        if (data.getMaterial().toString().contains("COBBLESTONE")) {
            data = Bukkit.createBlockData(data.getAsString().replaceAll("cobblestone",
                    GenUtils.randChoice(rand,
                            Material.COBBLESTONE,
                            Material.COBBLESTONE,
                            Material.COBBLESTONE,
                            Material.MOSSY_COBBLESTONE
                    ).toString().toLowerCase(Locale.ENGLISH)
            ));
            super.applyData(block, data);
            return;
        }
        else if (data.getMaterial().toString().contains("STONE_BRICK")) {
            if (GenUtils.chance(rand, 1, 5)) {
                data = Bukkit.createBlockData(data.getAsString().replaceAll("stone_brick", "mossy_stone_brick"));
            }
            super.applyData(block, data);
            return;
        }
        else if (data.getMaterial().toString().contains("OAK")) {
            data = Bukkit.createBlockData(data.getAsString()
                                              .replaceAll(data.getMaterial().toString().toLowerCase(Locale.ENGLISH),
                                                      WoodUtils.getWoodForBiome(biome,
                                                              WoodType.parse(data.getMaterial())
                                                      ).toString().toLowerCase(Locale.ENGLISH)
                                              )
                                              .toString()
                                              .toLowerCase(Locale.ENGLISH));
            super.applyData(block, data);
            return;
        }
        else if (data.getMaterial() == Material.CHEST) {
            if (GenUtils.chance(rand, 1, 5)) {
                block.setType(Material.AIR);
                return; // A fifth of chests is not placed.
            }
            super.applyData(block, data);
            int i = rand.nextInt(3);
            switch (i) {
                case 0 -> pop.lootTableChest(block.getX(), block.getY(), block.getZ(), TerraLootTable.VILLAGE_BUTCHER);
                case 1 -> pop.lootTableChest(block.getX(), block.getY(), block.getZ(), TerraLootTable.VILLAGE_TANNERY);
                default ->
                        pop.lootTableChest(block.getX(), block.getY(), block.getZ(), TerraLootTable.VILLAGE_SHEPHERD);
            }
        }
        else {
            super.applyData(block, data);
        }
    }

}