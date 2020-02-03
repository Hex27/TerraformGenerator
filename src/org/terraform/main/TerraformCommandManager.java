package org.terraform.main;

import org.drycell.command.DCCommandManager;
import org.drycell.main.DrycellPlugin;
import org.terraform.command.*;

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
		this.registerCommand(new RoomTestCommand(plugin, "roomtest","rt"));
		this.registerCommand(new CheckHeightCommand(plugin, "checkheight","ch"));
	}

}
