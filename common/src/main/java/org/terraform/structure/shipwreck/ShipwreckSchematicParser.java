package org.terraform.structure.shipwreck;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.SimpleBlock;
import org.terraform.schematic.SchematicParser;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;

import java.util.Locale;
import java.util.Random;

public class ShipwreckSchematicParser extends SchematicParser {
    private static final String[] WOODS = {
            "OAK",
            "ACACIA",
            "BIRCH",
            "SPRUCE",
            "DARK_OAK",
            "SPRUCE",
            "JUNGLE"
    };
    private final BiomeBank biome;
    private final Random rand;
    private final PopulatorDataAbstract pop;
    String woodType;

    public ShipwreckSchematicParser(BiomeBank biome, Random rand,
                                    PopulatorDataAbstract pop) {
        this.biome = biome;
        this.rand = rand;
        this.pop = pop;
        this.woodType = WOODS[rand.nextInt(WOODS.length)];
    }

    @Override
    public void applyData(SimpleBlock block, BlockData data) {

        //Water logging
        if (data instanceof Waterlogged) {
            Waterlogged logged = (Waterlogged) data;
            logged.setWaterlogged(BlockUtils.isWet(block));
        }

        //Mossy cobble
        if (data.getMaterial().toString().contains("COBBLESTONE")) {
            data = Bukkit.createBlockData(
                    StringUtils.replace(data.getAsString(), "cobblestone",
                            GenUtils.randMaterial(rand, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE)
                                    .name().toLowerCase(Locale.ENGLISH)
                    )
            );
        }

        //Holes
        if (GenUtils.chance(rand, 1, 30)) {
            if (block.getY() <= TerraformGenerator.seaLevel)
                data = Bukkit.createBlockData(Material.WATER);
            else
                data = Bukkit.createBlockData(Material.AIR);

            super.applyData(block, data);
            return;
        }

        if (data.getMaterial().toString().startsWith("OAK") ||
                data.getMaterial().toString().startsWith("STRIPPED_OAK")) {
            data = Bukkit.createBlockData(data.getAsString().replace("OAK", woodType));
        }


        if (data.getMaterial() == Material.CHEST) {
            if (GenUtils.chance(rand, 4, 5)) {
                if (block.getY() <= TerraformGenerator.seaLevel)
                    data = Bukkit.createBlockData(Material.WATER);
                else
                    data = Bukkit.createBlockData(Material.AIR);

                super.applyData(block, data);
                return;
            }
            super.applyData(block, data);
            if (GenUtils.chance(rand, 1, 5)) {
                pop.lootTableChest(block.getX(), block.getY(), block.getZ(), TerraLootTable.SHIPWRECK_TREASURE);
            } else
                pop.lootTableChest(block.getX(), block.getY(), block.getZ(), TerraLootTable.SHIPWRECK_SUPPLY);
            return;
        }

        if (data.getMaterial().isBlock() && data.getMaterial().isSolid()) {
            if (GenUtils.chance(rand, 1, 60)
                    && !biome.toString().contains("COLD") //Dont spawn these in cold places.
                    && !biome.toString().contains("FROZEN")) { //Corals
                CoralGenerator.generateCoral(block.getPopData(),
                        block.getX(),
                        block.getY(),
                        block.getZ());
            } else if (GenUtils.chance(rand, 1, 40)) { //kelp n stuff
                CoralGenerator.generateKelpGrowth(block.getPopData(),
                        block.getX(),
                        block.getY() + 1,
                        block.getZ());
            }
        }

        super.applyData(block, data);
    }

}