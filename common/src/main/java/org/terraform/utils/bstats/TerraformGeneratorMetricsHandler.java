package org.terraform.utils.bstats;

import org.jetbrains.annotations.NotNull;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;

public class TerraformGeneratorMetricsHandler {

    public TerraformGeneratorMetricsHandler(@NotNull TerraformGeneratorPlugin plugin) {

        int pluginId = 13968;
        Metrics metrics = new Metrics(plugin, pluginId);

        if (metrics.isEnabled()) {
            metrics.addCustomChart(new Metrics.SimplePie("onlyUseLogsNoWood",
                    () -> TConfig.c.MISC_TREES_FORCE_LOGS + ""
            ));
            metrics.addCustomChart(new Metrics.SimplePie("megaChunkNumBiomeSections",
                    () -> TConfig.c.STRUCTURES_MEGACHUNK_NUMBIOMESECTIONS + ""
            ));
            metrics.addCustomChart(new Metrics.SimplePie("biomeSectionBitshifts",
                    () -> TConfig.c.BIOME_SECTION_BITSHIFTS + ""
            ));
            TerraformGeneratorPlugin.logger.stdout("&abStats Metrics enabled.");
        }
        else {
            TerraformGeneratorPlugin.logger.stdout("&cbStats Metrics disabled.");
        }


    }


}
