package org.terraform.structure.pillager.outpost;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.block.data.BlockData;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.coregen.TerraLootTable;
import org.terraform.data.SimpleBlock;
import org.terraform.schematic.SchematicParser;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Random;

public class OutpostSchematicParser extends SchematicParser {

    private final BiomeBank biome;
    private final Random rand;
    private final PopulatorDataAbstract pop;

    public OutpostSchematicParser(BiomeBank biome, Random rand,
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
            pop.lootTableChest(block.getX(), block.getY(), block.getZ(), TerraLootTable.PILLAGER_OUTPOST);
        } else if (data.getMaterial() == Material.BARREL) {
            if (GenUtils.chance(rand, 3, 5)) {
                block.setType(Material.HAY_BLOCK);
                return; //3 fifths of barrels are not placed.
            }
            super.applyData(block, data);
            pop.lootTableChest(block.getX(), block.getY(), block.getZ(), TerraLootTable.PILLAGER_OUTPOST);
        } else if (data.getMaterial() == Material.WHITE_WALL_BANNER||data.getMaterial() == Material.WHITE_BANNER) {
        	//Set it first to make the target block a banner
        	super.applyData(block, data);
        	if(block.getPopData() instanceof PopulatorDataPostGen) {
        		/**
        		 * kms
        		 * https://minecraft.fandom.com/wiki/Banner/Patterns
					Pattern:"mr",Color:CYAN
					Pattern:"bs",Color:LIGHT_GRAY
					Pattern:"cs",Color:GRAY
					Pattern:"bo",Color:LIGHT_GRAY
					Pattern:"ms",Color:BLACK
					Pattern:"hh",Color:LIGHT_GRAY
					Pattern:"mc",Color:LIGHT_GRAY
					Pattern:"bo",Color:BLACK
        		 */
        		//org.bukkit.block.Banner;
        		Banner banner = (Banner) ((PopulatorDataPostGen) block.getPopData()).getBlockState(block.getX(),block.getY(),block.getZ());
        		banner.setPatterns(new ArrayList<Pattern>() {{
        			add(new Pattern(DyeColor.CYAN, PatternType.RHOMBUS_MIDDLE));
        			add(new Pattern(DyeColor.LIGHT_GRAY, PatternType.STRIPE_BOTTOM));
        			add(new Pattern(DyeColor.GRAY, PatternType.STRIPE_CENTER));
        			add(new Pattern(DyeColor.LIGHT_GRAY, PatternType.BORDER));
        			add(new Pattern(DyeColor.BLACK, PatternType.STRIPE_MIDDLE));
        			add(new Pattern(DyeColor.LIGHT_GRAY, PatternType.HALF_HORIZONTAL));
        			add(new Pattern(DyeColor.LIGHT_GRAY, PatternType.CIRCLE_MIDDLE));
        			add(new Pattern(DyeColor.BLACK, PatternType.BORDER));
        		}});
                banner.update();
        	}
        	return;
        } else {
            super.applyData(block, data);
        }
    }

}