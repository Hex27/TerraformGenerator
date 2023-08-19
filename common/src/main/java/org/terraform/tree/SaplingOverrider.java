package org.terraform.tree;

import org.bukkit.Tag;
import org.bukkit.TreeType;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfigOption;
import org.terraform.utils.version.OneTwentyBlockHandler;

import java.util.List;
import java.util.stream.Collectors;

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

        boolean isLarge = event.getBlocks().size() > 150;
        List<BlockState> baseBlocks = event.getBlocks().stream()
                .filter((b) -> Tag.LEAVES.isTagged(b.getType()))
                .toList();
        if(baseBlocks.size() == 0)
        {   //Leafless trees are not trees
            event.setCancelled(false);
            return;
        }
        BlockState baseBlock = baseBlocks.get(0);

        //This used to use event.getSpecies(), but some weird bug
        //made event.getSpecies() give incorrect values.
        switch (baseBlock.getType()) {
            case ACACIA_LEAVES:
                new FractalTreeBuilder(FractalTypes.Tree.SAVANNA_SMALL)
                        .skipGradientCheck()
                        .build(tw, data, x, y, z);
                break;
            case OAK_LEAVES:
                new FractalTreeBuilder(FractalTypes.Tree.NORMAL_SMALL)
                        .skipGradientCheck()
                        .build(tw, data, x, y, z);
                break;
            case BIRCH_LEAVES:
                new FractalTreeBuilder(FractalTypes.Tree.BIRCH_SMALL)
                        .skipGradientCheck()
                        .build(tw, data, x, y, z);
                break;
            case JUNGLE_LEAVES:

                if (TConfigOption.MISC_SAPLING_CUSTOM_TREES_BIGTREES.getBoolean()
                        && isLarge)
                    new FractalTreeBuilder(FractalTypes.Tree.JUNGLE_BIG)
                            .skipGradientCheck()
                            .build(tw, data, x, y, z);
                else
                    TreeDB.spawnSmallJungleTree(true, tw, data, x, y, z);
                break;
            case DARK_OAK_LEAVES:
                new FractalTreeBuilder(FractalTypes.Tree.DARK_OAK_SMALL)
                        .skipGradientCheck()
                        .build(tw, data, x, y, z);
                break;
            case SPRUCE_LEAVES:
                new FractalTreeBuilder(FractalTypes.Tree.TAIGA_SMALL)
                        .skipGradientCheck()
                        .build(tw, data, x, y, z);
                break;
            default:
                if(baseBlock.getType() == OneTwentyBlockHandler.CHERRY_LEAVES)
                {
                    new FractalTreeBuilder(FractalTypes.Tree.CHERRY_SMALL)
                            .skipGradientCheck()
                            .build(tw, data, x, y, z);
                    return;
                }
                //Not handled by TG
                event.setCancelled(false);
                break;
        }
    }
}
