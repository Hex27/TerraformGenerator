package org.terraform.tree;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.flat.PaleForestHandler;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;
import org.terraform.utils.GenUtils;
import org.terraform.utils.version.V_1_20;
import org.terraform.utils.version.V_1_21_4;

import java.util.List;

public class SaplingOverrider implements Listener {

    /**
     * Use priority highest to allow other plugins to modify event.getBlocks
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTreeGrow(@NotNull StructureGrowEvent event) {
        if (!TConfig.areTreesEnabled()) {
            return;
        }

        if (!(event.getWorld().getGenerator() instanceof TerraformGenerator)) {
            return;
        }
        TerraformWorld tw = TerraformWorld.get(event.getWorld());
        PopulatorDataPostGen data = new PopulatorDataPostGen(event.getLocation().getChunk());
        int x = event.getLocation().getBlockX();
        int y = event.getLocation().getBlockY();
        int z = event.getLocation().getBlockZ();
        boolean wasCancelled = event.isCancelled();
        event.setCancelled(true);

        boolean isLarge = event.getBlocks().size() > 150;
        List<BlockState> baseBlocks = event.getBlocks()
                                           .stream()
                                           .filter((b) -> Tag.LEAVES.isTagged(b.getType()))
                                           .toList();
        if (baseBlocks.isEmpty()) {   // Leafless trees are not trees
            event.setCancelled(false);
            return;
        }
        BlockState baseBlock = baseBlocks.get(0);

        // This used to use event.getSpecies(), but some weird bug
        // made event.getSpecies() give incorrect values.
        switch (baseBlock.getType()) {
            case ACACIA_LEAVES:
                new FractalTreeBuilder(FractalTypes.Tree.SAVANNA_SMALL).skipGradientCheck().build(tw, data, x, y, z);
                break;
            case OAK_LEAVES:
                FractalTypes.Tree.NORMAL_SMALL.build(
                        tw,
                        new SimpleBlock(data, x, y, z),
                        (nt -> {
                            nt.setCheckGradient(false);
                            //Let grown trees spawn beehives
                            if(GenUtils.RANDOMIZER.nextInt(5) == 0)
                                nt.setSpawnBees(true);
                        })
                );
                break;
            case BIRCH_LEAVES:
                new FractalTreeBuilder(FractalTypes.Tree.BIRCH_SMALL).skipGradientCheck().build(tw, data, x, y, z);
                break;
            case JUNGLE_LEAVES:

                if (TConfig.c.MISC_SAPLING_CUSTOM_TREES_BIGTREES_JUNGLE && isLarge) {
                    new FractalTreeBuilder(FractalTypes.Tree.JUNGLE_BIG).skipGradientCheck().build(tw, data, x, y, z);
                }
                else {
                    TreeDB.spawnSmallJungleTree(true, tw, data, x, y, z);
                }
                break;
            case DARK_OAK_LEAVES:
                FractalTypes.Tree.DARK_OAK_SMALL.build(
                        tw,
                        new SimpleBlock(data, x, y, z),
                        (nt -> nt.setCheckGradient(false))
                );
                break;
            case SPRUCE_LEAVES:
                if (TConfig.c.MISC_SAPLING_CUSTOM_TREES_BIGTREES_SPRUCE && isLarge) {
                    FractalTypes.Tree.TAIGA_BIG.build(tw,
                            new SimpleBlock(data, x, y, z),
                            (nt -> nt.setCheckGradient(false))
                    );
                    // Set the original podzol radius
                    event.getBlocks()
                         .stream()
                         .filter((b) -> b.getType() == Material.PODZOL)
                         .forEach((b) -> data.setType(b.getX(), b.getY(), b.getZ(), b.getType()));
                }
                else {
                    FractalTypes.Tree.TAIGA_SMALL.build(tw,
                            new SimpleBlock(data, x, y, z),
                            (nt -> nt.setCheckGradient(false))
                    );
                }
                break;
            default:
                if (baseBlock.getType() == V_1_20.CHERRY_LEAVES) {
                    new FractalTreeBuilder(FractalTypes.Tree.CHERRY_SMALL).skipGradientCheck().build(tw, data, x, y, z);
                    return;
                }
                if ( baseBlock.getType() == V_1_21_4.PALE_OAK_LEAVES) {
                    FractalTypes.Tree.DARK_OAK_SMALL
                            .build(
                                tw,
                                new SimpleBlock(data, x, y, z),
                                (nt -> {
                                    nt.setCheckGradient(false);
                                    new PaleForestHandler().paleTreeMutator(nt);
                                })
                            );
                    return;
                }
                // Not handled by TG
                event.setCancelled(wasCancelled);
                break;
        }
    }
}
