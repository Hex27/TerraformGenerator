package org.terraform.main;

import org.drycell.command.DCCommandManager;
import org.drycell.main.DrycellPlugin;
import org.terraform.command.*;

public class TerraformCommandManager extends DCCommandManager {

    public TerraformCommandManager(DrycellPlugin plugin, String... bases) {
        super(plugin, bases);
        this.registerCommand(new PreviewCommand(plugin, "preview"));
        this.registerCommand(new TimingsCommand(plugin, "timings", "lag"));
        this.registerCommand(new CoconutCommand(plugin, "coconut"));
        this.registerCommand(new ValuesCommand(plugin, "values"));
        this.registerCommand(new SphereCommand(plugin, "sphere"));
        this.registerCommand(new FractalTreeCommand(plugin, "fractal", "fractaltree", "ftree"));
        this.registerCommand(new CaveCommand(plugin, "cave"));
        this.registerCommand(new IceSpikeCommand(plugin, "icespike"));
        this.registerCommand(new CoralCommand(plugin, "coral"));
        this.registerCommand(new WitchHutCommand(plugin, "hut"));
        this.registerCommand(new GiantPumpkinCommand(plugin, "giantpumpkin"));
        this.registerCommand(new MonumentCommand(plugin, "monument", "mon"));
        this.registerCommand(new LargeMonumentLampCommand(plugin, "lml"));
        this.registerCommand(new StrongholdCommand(plugin, "stronghold", "sh"));
        this.registerCommand(new AnimalFarmCommand(plugin, "animalfarm", "af"));
        this.registerCommand(new FarmhouseCommand(plugin, "farmhouse", "fh"));
        this.registerCommand(new AnimalSpawnerCommand(plugin, "animalspawner", "as"));
        this.registerCommand(new MineshaftCommand(plugin, "mineshaft", "ms"));
        this.registerCommand(new ShipwreckCommand(plugin, "shipwreck", "sw"));
        this.registerCommand(new UndergroundDungeonCommand(plugin, "ud", "undergrounddungeon"));
        this.registerCommand(new DrownedDungeonCommand(plugin, "dd", "drowneddungeon"));
        this.registerCommand(new CheckHeightCommand(plugin, "checkheight", "ch"));
        //this.registerCommand(new GuardianSpawnCheckCommand(plugin, "gsc","guardianspawncheck"));
        this.registerCommand(new WandCommand(plugin, "wand"));
        this.registerCommand(new MushroomCommand(plugin, "mushroom"));
        this.registerCommand(new LocateCommand(plugin, "locate"));
        this.registerCommand(new SchematicSaveCommand(plugin, "save"));
        this.registerCommand(new SchematicLoadCommand(plugin, "load"));
        this.registerCommand(new PyramidCommand(plugin, "pyramid"));
        this.registerCommand(new MazeCommand(plugin, "maze"));
        this.registerCommand(new BlockDataTestCommand(plugin, "blockdatatest", "bdt"));
    }

}
