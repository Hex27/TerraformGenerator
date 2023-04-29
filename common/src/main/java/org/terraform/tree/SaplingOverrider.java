package org.terraform.tree;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;

public class SaplingOverrider implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onTreeGrow(StructureGrowEvent event) {
        if (!(event.getWorld().getGenerator() instanceof TerraformGenerator)) return;

        TerraformWorld tw = TerraformWorld.get(event.getWorld());
        PopulatorDataPostGen data = new PopulatorDataPostGen(event.getLocation().getChunk());
        int x = event.getLocation().getBlockX();
        int y = event.getLocation().getBlockY();
        int z = event.getLocation().getBlockZ();
        event.setCancelled(true);

        switch (event.getSpecies()) {
            case ACACIA:
                new FractalTreeBuilder(FractalTypes.Tree.SAVANNA_SMALL)
                        .skipGradientCheck()
                        .build(tw, data, x, y, z);
                break;
            case BIG_TREE:
            case TREE:
                new FractalTreeBuilder(FractalTypes.Tree.NORMAL_SMALL)
                        .skipGradientCheck()
                        .build(tw, data, x, y, z);
                break;
            case BIRCH:
//		case SWAMP: //No small swamp tree :\
//			break;
            case TALL_BIRCH:
                new FractalTreeBuilder(FractalTypes.Tree.BIRCH_SMALL)
                        .skipGradientCheck()
                        .build(tw, data, x, y, z);
                break;
            case COCOA_TREE:
            case SMALL_JUNGLE:
                TreeDB.spawnSmallJungleTree(true, tw, data, x, y, z);
                break;
            case DARK_OAK:
                new FractalTreeBuilder(FractalTypes.Tree.DARK_OAK_SMALL)
                        .skipGradientCheck()
                        .build(tw, data, x, y, z);
                break;
            case JUNGLE:
                if (TConfigOption.MISC_SAPLING_CUSTOM_TREES_BIGTREES.getBoolean())
                    new FractalTreeBuilder(FractalTypes.Tree.JUNGLE_BIG)
                            .skipGradientCheck()
                            .build(tw, data, x, y, z);
                else
                    TreeDB.spawnSmallJungleTree(true, tw, data, x, y, z);
                break;
            case MEGA_REDWOOD:
            case REDWOOD:
            case TALL_REDWOOD:
                new FractalTreeBuilder(FractalTypes.Tree.TAIGA_SMALL)
                        .skipGradientCheck()
                        .build(tw, data, x, y, z);
                break;
            default: //Not handled by TG
                event.setCancelled(false);
                break;
        }
    }
}
