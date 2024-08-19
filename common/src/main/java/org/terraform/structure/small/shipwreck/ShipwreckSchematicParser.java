package org.terraform.structure.small.shipwreck;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.schematic.SchematicParser;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.CoralGenerator;
import org.terraform.utils.GenUtils;

import java.util.Locale;
import java.util.Random;

public class ShipwreckSchematicParser extends SchematicParser {
    private static final String[] WOODS = {
            "OAK", "ACACIA", "BIRCH", "SPRUCE", "DARK_OAK", "SPRUCE", "JUNGLE"
    };
    final String woodType;
    private final BiomeBank biome;
    private final @NotNull Random rand;
    private final PopulatorDataAbstract pop;

    public ShipwreckSchematicParser(BiomeBank biome, @NotNull Random rand, PopulatorDataAbstract pop) {
        this.biome = biome;
        this.rand = rand;
        this.pop = pop;
        this.woodType = WOODS[rand.nextInt(WOODS.length)];
    }

    @Override
    public void applyData(@NotNull SimpleBlock block, @NotNull BlockData data) {

        // Water logging
        if (data instanceof Waterlogged logged) {
            logged.setWaterlogged(BlockUtils.isWet(block));
        }

        // Mossy cobble
        if (data.getMaterial().toString().contains("COBBLESTONE")) {
            data = Bukkit.createBlockData(StringUtils.replace(data.getAsString(),
                    "cobblestone",
                    GenUtils.randChoice(rand,
                            Material.COBBLESTONE,
                            Material.COBBLESTONE,
                            Material.COBBLESTONE,
                            Material.MOSSY_COBBLESTONE
                    ).name().toLowerCase(Locale.ENGLISH)
            ));
        }

        // Holes
        if (GenUtils.chance(rand, 1, 30)) {
            if (block.getY() <= TerraformGenerator.seaLevel) {
                data = Bukkit.createBlockData(Material.WATER);
            }
            else {
                data = Bukkit.createBlockData(Material.AIR);
            }

            super.applyData(block, data);
            return;
        }

        if (data.getMaterial().toString().startsWith("OAK") || data.getMaterial()
                                                                   .toString()
                                                                   .startsWith("STRIPPED_OAK"))
        {
            data = Bukkit.createBlockData(data.getAsString().replace("OAK", woodType));
        }


        if (data.getMaterial() == Material.CHEST) {
            if (GenUtils.chance(rand, 4, 5)) {
                if (block.getY() <= TerraformGenerator.seaLevel) {
                    data = Bukkit.createBlockData(Material.WATER);
                }
                else {
                    data = Bukkit.createBlockData(Material.AIR);
                }

                super.applyData(block, data);
                return;
            }
            super.applyData(block, data);
            switch (rand.nextInt(3)) {
                case 0:
                    pop.lootTableChest(block.getX(), block.getY(), block.getZ(), TerraLootTable.SHIPWRECK_TREASURE);
                    break;
                case 1:
                    pop.lootTableChest(block.getX(), block.getY(), block.getZ(), TerraLootTable.SHIPWRECK_SUPPLY);
                    break;
                case 2:
                default:
                    pop.lootTableChest(block.getX(), block.getY(), block.getZ(), TerraLootTable.SHIPWRECK_MAP);
                    break;
            }
            return;
        }

        if (data.getMaterial().isBlock() && data.getMaterial().isSolid()) {
            if (GenUtils.chance(rand, 1, 60) && !biome.toString().contains("COLD") // Dont spawn these in cold places.
                && !biome.toString().contains("FROZEN"))
            { // Corals
                CoralGenerator.generateCoral(block.getPopData(), block.getX(), block.getY(), block.getZ());
            }
            else if (GenUtils.chance(rand, 1, 40)) { // kelp n stuff
                CoralGenerator.generateKelpGrowth(block.getPopData(), block.getX(), block.getY() + 1, block.getZ());
            }
        }

        super.applyData(block, data);
    }

}