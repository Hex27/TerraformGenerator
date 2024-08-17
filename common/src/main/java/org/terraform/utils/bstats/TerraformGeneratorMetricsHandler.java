package org.terraform.utils.bstats;

import org.jetbrains.annotations.NotNull;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;

public class TerraformGeneratorMetricsHandler {

	@SuppressWarnings("unused")
	private final TerraformGeneratorPlugin plugin;
	
	public TerraformGeneratorMetricsHandler(@NotNull TerraformGeneratorPlugin plugin) {
		this.plugin = plugin;
		
		int pluginId = 13968;
        Metrics metrics = new Metrics(plugin, pluginId);
        
        if(metrics.isEnabled()) {
        	metrics.addCustomChart(new Metrics.SimplePie("onlyUseLogsNoWood", () -> TConfigOption.MISC_TREES_FORCE_LOGS.getBoolean() + ""));
        	metrics.addCustomChart(new Metrics.SimplePie("megaChunkNumBiomeSections", () -> TConfigOption.STRUCTURES_MEGACHUNK_NUMBIOMESECTIONS.getInt() + ""));
        	metrics.addCustomChart(new Metrics.SimplePie("biomeSectionBitshifts", () -> TConfigOption.BIOME_SECTION_BITSHIFTS.getInt() + ""));
        	TerraformGeneratorPlugin.logger.stdout("&abStats Metrics enabled.");
        }
        else
        	TerraformGeneratorPlugin.logger.stdout("&cbStats Metrics disabled.");
        
        
	}
	
	
	
}
