package org.terraform.main;

import org.drycell.command.DCCommandManager;
import org.drycell.main.DrycellPlugin;
import org.terraform.command.AnimalFarmCommand;
import org.terraform.command.AnimalSpawnerCommand;
import org.terraform.command.CaveCommand;
import org.terraform.command.CheckHeightCommand;
import org.terraform.command.CoconutCommand;
import org.terraform.command.CoralCommand;
import org.terraform.command.DrownedDungeonCommand;
import org.terraform.command.FarmhouseCommand;
import org.terraform.command.FractalTreeCommand;
import org.terraform.command.GiantPumpkinCommand;
import org.terraform.command.GuardianSpawnCheckCommand;
import org.terraform.command.IceSpikeCommand;
import org.terraform.command.LargeMonumentLampCommand;
import org.terraform.command.MineshaftCommand;
import org.terraform.command.MonumentCommand;
import org.terraform.command.MushroomCommand;
import org.terraform.command.PreviewCommand;
import org.terraform.command.SchematicLoadCommand;
import org.terraform.command.SchematicSaveCommand;
import org.terraform.command.ShipwreckCommand;
import org.terraform.command.SphereCommand;
import org.terraform.command.StrongholdCommand;
import org.terraform.command.TimingsCommand;
import org.terraform.command.UndergroundDungeonCommand;
import org.terraform.command.ValuesCommand;
import org.terraform.command.WandCommand;
import org.terraform.command.WitchHutCommand;

public class TerraformCommandManager extends DCCommandManager {

	public TerraformCommandManager(DrycellPlugin plugin, String... bases) {
		super(plugin, bases);
		this.registerCommand(new PreviewCommand(plugin, "preview"));
		this.registerCommand(new TimingsCommand(plugin, "timings","lag"));
		this.registerCommand(new CoconutCommand(plugin, "coconut"));
		this.registerCommand(new ValuesCommand(plugin, "values"));
		this.registerCommand(new SphereCommand(plugin, "sphere"));
		this.registerCommand(new FractalTreeCommand(plugin, "fractal","fractaltree","ftree"));
		this.registerCommand(new CaveCommand(plugin, "cave"));
		this.registerCommand(new IceSpikeCommand(plugin, "icespike"));
		this.registerCommand(new CoralCommand(plugin, "coral"));
		this.registerCommand(new WitchHutCommand(plugin, "hut"));
		this.registerCommand(new GiantPumpkinCommand(plugin, "giantpumpkin"));
		this.registerCommand(new MonumentCommand(plugin, "monument","mon"));
		this.registerCommand(new LargeMonumentLampCommand(plugin, "lml"));
		this.registerCommand(new StrongholdCommand(plugin, "stronghold","sh"));
		this.registerCommand(new AnimalFarmCommand(plugin, "animalfarm","af"));
		this.registerCommand(new FarmhouseCommand(plugin, "farmhouse","fh"));
		this.registerCommand(new AnimalSpawnerCommand(plugin, "animalspawner","as"));
		this.registerCommand(new MineshaftCommand(plugin, "mineshaft","ms"));
		this.registerCommand(new ShipwreckCommand(plugin, "shipwreck","sw"));
		this.registerCommand(new UndergroundDungeonCommand(plugin, "ud","undergrounddungeon"));
		this.registerCommand(new DrownedDungeonCommand(plugin, "dd","drowneddungeon"));
		this.registerCommand(new CheckHeightCommand(plugin, "checkheight","ch"));
		//this.registerCommand(new GuardianSpawnCheckCommand(plugin, "gsc","guardianspawncheck"));
		this.registerCommand(new WandCommand(plugin, "wand"));
		this.registerCommand(new MushroomCommand(plugin, "mushroom"));
		this.registerCommand(new SchematicSaveCommand(plugin, "save"));
		this.registerCommand(new SchematicLoadCommand(plugin, "load"));
	}

}
