package org.terraform.structure.pillager.outpost;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.data.SimpleBlock;
import org.terraform.data.Wall;
import org.terraform.schematic.SchematicParser;
import org.terraform.utils.BannerUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.WoodUtils;
import org.terraform.utils.WoodUtils.WoodType;

import java.util.Locale;
import java.util.Random;

public class OutpostSchematicParser extends SchematicParser {

    private final BiomeBank biome;
    private final Random rand;
    private final PopulatorDataAbstract pop;
    private final int baseY;
    private Material[] toReplace;

    public OutpostSchematicParser(BiomeBank biome, Random rand, PopulatorDataAbstract pop, int baseY) {
        this.biome = biome;
        this.rand = rand;
        this.pop = pop;
        this.baseY = baseY;
        toReplace = new Material[] {
                Material.COBBLESTONE,
                Material.COBBLESTONE,
                Material.COBBLESTONE,
                Material.MOSSY_COBBLESTONE
        };
        if (biome == BiomeBank.BADLANDS || biome == BiomeBank.DESERT) {
            toReplace = new Material[] {
                    Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.ANDESITE
            };
        }

    }

    @Override
    public void applyData(@NotNull SimpleBlock block, @NotNull BlockData data) {
        // No moss on desert areas.
        if (data.getMaterial().toString().contains("COBBLESTONE")) {

            // Desert variants replace cobblestone with andesite instead of mossy cobble.

            data = Bukkit.createBlockData(data.getAsString()
                                              .replaceAll("cobblestone",
                                                      GenUtils.randChoice(rand, toReplace)
                                                              .toString()
                                                              .toLowerCase(Locale.ENGLISH)
                                              ));

            super.applyData(block, data);
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
        }
        else if (data.getMaterial() == Material.CHEST) {
            if (GenUtils.chance(rand, 1, 5)) {
                block.setType(Material.AIR);
                // A fifth of chests are not placed.
            }
            else {
                super.applyData(block, data);
                pop.lootTableChest(block.getX(), block.getY(), block.getZ(), TerraLootTable.PILLAGER_OUTPOST);
            }
        }
        else if (data.getMaterial() == Material.BARREL) {
            if (GenUtils.chance(rand, 3, 5)) {
                block.setType(Material.HAY_BLOCK);
                // 3 fifths of barrels are not placed.
            }
            else {
                super.applyData(block, data);
                pop.lootTableChest(block.getX(), block.getY(), block.getZ(), TerraLootTable.PILLAGER_OUTPOST);
            }
        }
        else if (data.getMaterial() == Material.WHITE_WALL_BANNER || data.getMaterial() == Material.WHITE_BANNER) {
            // Set it first to make the target block a banner
            super.applyData(block, data);
            if (block.getPopData() instanceof PopulatorDataPostGen) {
                // org.bukkit.block.Banner;
                Banner banner = (Banner) ((PopulatorDataPostGen) block.getPopData()).getBlockState(block.getX(),
                        block.getY(),
                        block.getZ());
                banner.setPatterns(BannerUtils.getOminousBannerPatterns());
                banner.update();
            }
        }
        else {
            super.applyData(block, data);
        }

        if (block.getY() == baseY) {
            new Wall(block.getDown()).downUntilSolid(new Random(), toReplace);
        }
    }

}