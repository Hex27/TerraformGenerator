package org.terraform.coregen.bukkit;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataPostGen;
import org.terraform.coregen.PopulatorDataRecursiveICA;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.StructurePopulator;
import org.terraform.structure.VillageHousePopulator;
import org.terraform.structure.caves.LargeCavePopulator;
import org.terraform.structure.dungeon.SmallDungeonPopulator;
import org.terraform.structure.mineshaft.MineshaftPopulator;
import org.terraform.structure.monument.MonumentPopulator;
import org.terraform.structure.pyramid.PyramidPopulator;
import org.terraform.structure.shipwreck.ShipwreckPopulator;
import org.terraform.structure.stronghold.StrongholdPopulator;
import org.terraform.utils.GenUtils;

import java.util.ArrayList;
import java.util.Random;

public class TerraformStructurePopulator extends BlockPopulator {
    public static final StructurePopulator[] structurePops = {
            new StrongholdPopulator(), new VillageHousePopulator(), new SmallDungeonPopulator(), 
            new MonumentPopulator(), new ShipwreckPopulator(), new MineshaftPopulator(), 
            new LargeCavePopulator(), new PyramidPopulator()
    };

    private final TerraformWorld tw;

    public TerraformStructurePopulator(TerraformWorld tw) {
        this.tw = tw;
    }

    @Override
    public void populate(World world, Random random, Chunk chunk) {
        //Don't attempt generation pre-injection.
        if (!TerraformGeneratorPlugin.injectedWorlds.contains(world.getName())) return;
        PopulatorDataPostGen data = new PopulatorDataPostGen(chunk);

        //Use IChunkAccess to place blocks instead. Known to cause lighting problems.
        if (TConfigOption.DEVSTUFF_EXPERIMENTAL_STRUCTURE_PLACEMENT.getBoolean())
            data = new PopulatorDataRecursiveICA(chunk);

        //PopulatorDataAbstract data = TerraformGeneratorPlugin.injector.getICAData(chunk);
        //TerraformGeneratorPlugin.logger.debug("s-pop-1");
        ArrayList<BiomeBank> banks = GenUtils.getBiomesInChunk(tw, data.getChunkX(), data.getChunkZ());

        //TerraformGeneratorPlugin.logger.debug("s-pop-2");
        for (StructurePopulator spop : structurePops) {
            //TerraformGeneratorPlugin.logger.debug("s-pop-3");
            if (spop.canSpawn(tw, data.getChunkX(), data.getChunkZ(), banks)) {
                TerraformGeneratorPlugin.logger.info("Generating " + spop.getClass().getName() + " at chunk: " + data.getChunkX() + "," + data.getChunkZ());
                spop.populate(tw, data);
            }
        }
    }
}
