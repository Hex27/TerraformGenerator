package org.terraform.coregen;

import org.bukkit.Material;
import org.terraform.biome.BiomeBank;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.populators.OrePopulator;
import org.terraform.structure.StructureBufferDistanceHandler;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Random;

public class TerraformPopulator {
    //private static Set<SimpleChunkLocation> chunks = new HashSet<SimpleChunkLocation>();

    //private RiverWormCreator rwc;

    private static final OrePopulator[] ORE_POPS = {
            // Ores
            new OrePopulator(Material.COAL_ORE, TConfigOption.ORE_COAL_CHANCE.getInt(), TConfigOption.ORE_COAL_VEINSIZE.getInt(), TConfigOption.ORE_COAL_MAXVEINNUMBER.getInt(),
                    TConfigOption.ORE_COAL_MINSPAWNHEIGHT.getInt(), TConfigOption.ORE_COAL_COMMONSPAWNHEIGHT.getInt(), TConfigOption.ORE_COAL_MAXSPAWNHEIGHT.getInt()),
            new OrePopulator(Material.IRON_ORE, TConfigOption.ORE_IRON_CHANCE.getInt(), TConfigOption.ORE_IRON_VEINSIZE.getInt(), TConfigOption.ORE_IRON_MAXVEINNUMBER.getInt(),
                    TConfigOption.ORE_IRON_MINSPAWNHEIGHT.getInt(), TConfigOption.ORE_IRON_COMMONSPAWNHEIGHT.getInt(), TConfigOption.ORE_IRON_MAXSPAWNHEIGHT.getInt()),
            new OrePopulator(Material.GOLD_ORE, TConfigOption.ORE_GOLD_CHANCE.getInt(), TConfigOption.ORE_GOLD_VEINSIZE.getInt(), TConfigOption.ORE_GOLD_MAXVEINNUMBER.getInt(),
                    TConfigOption.ORE_GOLD_MINSPAWNHEIGHT.getInt(), TConfigOption.ORE_GOLD_COMMONSPAWNHEIGHT.getInt(), TConfigOption.ORE_GOLD_MAXSPAWNHEIGHT.getInt()),
            new OrePopulator(Material.DIAMOND_ORE, TConfigOption.ORE_DIAMOND_CHANCE.getInt(), TConfigOption.ORE_DIAMOND_VEINSIZE.getInt(),
                    TConfigOption.ORE_DIAMOND_MAXVEINNUMBER.getInt(), TConfigOption.ORE_DIAMOND_MINSPAWNHEIGHT.getInt(), TConfigOption.ORE_DIAMOND_COMMONSPAWNHEIGHT.getInt(),
                    TConfigOption.ORE_DIAMOND_MAXSPAWNHEIGHT.getInt()),
            new OrePopulator(Material.LAPIS_ORE, TConfigOption.ORE_LAPIS_CHANCE.getInt(), TConfigOption.ORE_LAPIS_VEINSIZE.getInt(),
                    TConfigOption.ORE_LAPIS_MAXVEINNUMBER.getInt(), TConfigOption.ORE_LAPIS_MINSPAWNHEIGHT.getInt(), TConfigOption.ORE_LAPIS_COMMONSPAWNHEIGHT.getInt(),
                    TConfigOption.ORE_LAPIS_MAXSPAWNHEIGHT.getInt()),
            new OrePopulator(Material.REDSTONE_ORE, TConfigOption.ORE_REDSTONE_CHANCE.getInt(), TConfigOption.ORE_REDSTONE_VEINSIZE.getInt(),
                    TConfigOption.ORE_REDSTONE_MAXVEINNUMBER.getInt(), TConfigOption.ORE_REDSTONE_MINSPAWNHEIGHT.getInt(), TConfigOption.ORE_REDSTONE_COMMONSPAWNHEIGHT.getInt(),
                    TConfigOption.ORE_REDSTONE_MAXSPAWNHEIGHT.getInt()),
            //Non-ores
            new OrePopulator(Material.GRAVEL, TConfigOption.ORE_GRAVEL_CHANCE.getInt(), TConfigOption.ORE_GRAVEL_VEINSIZE.getInt(),
                    TConfigOption.ORE_GRAVEL_MAXVEINNUMBER.getInt(), TConfigOption.ORE_GRAVEL_MINSPAWNHEIGHT.getInt(), TConfigOption.ORE_GRAVEL_COMMONSPAWNHEIGHT.getInt(),
                    TConfigOption.ORE_GRAVEL_MAXSPAWNHEIGHT.getInt()),
            new OrePopulator(Material.ANDESITE, TConfigOption.ORE_ANDESITE_CHANCE.getInt(), TConfigOption.ORE_ANDESITE_VEINSIZE.getInt(),
                    TConfigOption.ORE_ANDESITE_MAXVEINNUMBER.getInt(), TConfigOption.ORE_ANDESITE_MINSPAWNHEIGHT.getInt(), TConfigOption.ORE_ANDESITE_COMMONSPAWNHEIGHT.getInt(),
                    TConfigOption.ORE_ANDESITE_MAXSPAWNHEIGHT.getInt()),
            new OrePopulator(Material.DIORITE, TConfigOption.ORE_DIORITE_CHANCE.getInt(), TConfigOption.ORE_DIORITE_VEINSIZE.getInt(),
                    TConfigOption.ORE_DIORITE_MAXVEINNUMBER.getInt(), TConfigOption.ORE_DIORITE_MINSPAWNHEIGHT.getInt(), TConfigOption.ORE_DIORITE_COMMONSPAWNHEIGHT.getInt(),
                    TConfigOption.ORE_DIORITE_MAXSPAWNHEIGHT.getInt()),
            new OrePopulator(Material.GRANITE, TConfigOption.ORE_GRANITE_CHANCE.getInt(), TConfigOption.ORE_GRANITE_VEINSIZE.getInt(),
                    TConfigOption.ORE_GRANITE_MAXVEINNUMBER.getInt(), TConfigOption.ORE_GRANITE_MINSPAWNHEIGHT.getInt(), TConfigOption.ORE_GRANITE_COMMONSPAWNHEIGHT.getInt(),
                    TConfigOption.ORE_GRANITE_MAXSPAWNHEIGHT.getInt())
    };


    //private CaveWormCreator cavePop;

    public TerraformPopulator(TerraformWorld tw) {
        //this.rwc = new RiverWormCreator(tw);
    }

    public void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data) {

        //ores
        for (OrePopulator ore : ORE_POPS) {
            //TerraformGeneratorPlugin.logger.info("Generating ores...");
            ore.populate(tw, random, data);
        }

        // Get all biomes in a chunk
        ArrayList<BiomeBank> banks = GenUtils.getBiomesInChunk(tw, data.getChunkX(), data.getChunkZ());
//        		new ArrayList<>();

        
        boolean canDecorate = StructureBufferDistanceHandler.canDecorateChunk(tw, data.getChunkX(), data.getChunkZ());
        for (BiomeBank bank : banks) {
            // Biome specific populators
            bank.getHandler().populateSmallItems(tw, random, data);
            
            //Only decorate disruptive features if the structures allow for them
            if(canDecorate)
            	bank.getHandler().populateLargeItems(tw, random, data);
                        
            // Cave populators
            bank.getCavePop().populate(tw, random, data);
        }


    }
}
