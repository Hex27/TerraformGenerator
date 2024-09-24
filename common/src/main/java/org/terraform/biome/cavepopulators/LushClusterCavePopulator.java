package org.terraform.biome.cavepopulators;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.populatordata.PopulatorDataICABiomeWriterAbstract;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.small_items.PlantBuilder;
import org.terraform.tree.TreeDB;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.SphereBuilder;
import org.terraform.utils.blockdata.BisectedBuilder;
import org.terraform.utils.blockdata.DirectionalBuilder;
import org.terraform.utils.blockdata.MultipleFacingBuilder;
import org.terraform.utils.version.Version;

import java.util.Random;

public class LushClusterCavePopulator extends AbstractCaveClusterPopulator {

    private final boolean isForLargeCave;

    public LushClusterCavePopulator(float radius, boolean isForLargeCave) {
        super(radius);
        this.isForLargeCave = isForLargeCave;
    }

    @Override
    public void oneUnit(@NotNull TerraformWorld tw,
                        @NotNull Random random,
                        @NotNull SimpleBlock ceil,
                        @NotNull SimpleBlock floor,
                        boolean boundary)
    {

        // =========================
        // Upper decorations
        // =========================

        int caveHeight = ceil.getY() - floor.getY();

        // Don't decorate wet areas
        if (!BlockUtils.isWet(ceil.getDown())) {
            // Don't touch slabbed floors or stalagmites
            if (Tag.SLABS.isTagged(floor.getType()) || Tag.WALLS.isTagged(floor.getType())) {
                return;
            }

            // Ceiling is sometimes roots
            if (GenUtils.chance(random, 1, 8)) {
                // This part doesn't spawn Azaleas
                ceil.setType(Material.ROOTED_DIRT);
                if (random.nextBoolean()) {
                    PlantBuilder.HANGING_ROOTS.build(ceil.getDown());
                }

            }
            else // If not, it's moss
            {
                ceil.setType(Material.MOSS_BLOCK);
                for (BlockFace face : BlockUtils.sixBlockFaces) {
                    if (ceil.getRelative(face).getType() == Material.LAVA) {
                        ceil.getRelative(face).setType(Material.AIR);
                    }
                }

                // Spore blossom
                if (GenUtils.chance(random, 1, 15)) {
                    PlantBuilder.SPORE_BLOSSOM.build(ceil.getDown());
                }
            }

            // Spawn these on the surface, and let the roots go downwards.
            // Hopefully, there won't be random small caves in between the tree
            // and this cave hole.
            if (isForLargeCave && GenUtils.chance(random, 1, 300)) {
                SimpleBlock base = ceil.getGround();
                if (BlockUtils.isDirtLike(base.getType()) && !BlockUtils.isWet(base.getUp())) {
                    TreeDB.spawnAzalea(random, tw, base.getPopData(), base.getX(), base.getY() + 1, base.getZ());
                }
            }

            // Glow Berries
            int glowBerryChance = 5;
            if (isForLargeCave) {
                glowBerryChance = 7;
            }
            if (GenUtils.chance(random, 1, glowBerryChance)) {
                int h = caveHeight / 4;
                if (h < 1) {
                    h = 1;
                }
                if (h > 6) {
                    h = 6;
                }
                BlockUtils.downLCaveVines(h, ceil.getDown());
            }
        }

        // =========================
        // Lower decorations
        // =========================

        // If floor is submerged, set it to clay, then don't touch it.
        if (BlockUtils.isWet(floor.getUp())) {
            if (!isForLargeCave) {
                floor.setType(Material.CLAY);
            }
            return;
        }

        // Ground is moss.
        floor.setType(Material.MOSS_BLOCK);


        if (GenUtils.chance(random, 1, 15)) { // Azaleas
            if (random.nextBoolean()) {
                PlantBuilder.AZALEA.build(floor.getUp());
            }
            else {
                PlantBuilder.FLOWERING_AZALEA.build(floor.getUp());
            }
        }
        else if (Version.isAtLeast(17) && GenUtils.chance(random, 1, 7)) { // Dripleaves
            if (TConfig.arePlantsEnabled()) {
                if (random.nextBoolean()) {
                    new DirectionalBuilder(Material.BIG_DRIPLEAF).setFacing(BlockUtils.getDirectBlockFace(random))
                                                                 .apply(floor.getUp());
                }
                else {
                    new BisectedBuilder(Material.SMALL_DRIPLEAF).placeBoth(floor.getUp());
                }
            }
        }
        else if (GenUtils.chance(random, 1, 6))
        // Grass
        {
            PlantBuilder.GRASS.build(floor.getUp());
        }
        else if (GenUtils.chance(random, 1, 7))
        // Moss carpets
        {
            PlantBuilder.MOSS_CARPET.build(floor.getUp());
        }


        // =========================
        // Wall decorations
        // =========================
        if (!isForLargeCave) {
            SimpleBlock target = floor;
            while (target.getY() != ceil.getY()) {
                // Place small pools of water for axolotls
                if (target.getY() - floor.getY() < 3 && GenUtils.chance(1, 700)) {
                    new SphereBuilder(
                            random,
                            target,
                            Material.WATER
                    ).setSphereType(SphereBuilder.SphereType.LOWER_SEMISPHERE)
                     .setCointainmentMaterials(Material.CLAY)
                     .setRX(3)
                     .setRY(2)
                     .setRZ(3)
                     .setDoLiquidContainment(true)
                     .setHardReplace(true)
                     .build();
                }

                // Replace the walls with moss, and line with lichen
                for (BlockFace face : BlockUtils.directBlockFaces) {
                    SimpleBlock rel = target.getRelative(face);

                    if (BlockUtils.isStoneLike(rel.getType())) {
                        rel.setType(Material.MOSS_BLOCK);
                        if (TConfig.arePlantsEnabled() && BlockUtils.isAir(target.getType()) && GenUtils.chance(
                                random,
                                1,
                                5
                        ))
                        {
                            new MultipleFacingBuilder(Material.GLOW_LICHEN).setFace(face, true).apply(target);
                        }
                    }
                }
                target = target.getUp();
            }
        }

        // =========================
        // Biome Setter
        // =========================
        if (TerraformGeneratorPlugin.injector.getICAData(ceil.getPopData()) instanceof PopulatorDataICABiomeWriterAbstract data) {
            while (floor.getY() < ceil.getY()) {
                data.setBiome(floor.getX(), floor.getY(), floor.getZ(), Biome.LUSH_CAVES);
                floor = floor.getUp();
            }
        }
    }


}
