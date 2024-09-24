package org.terraform.structure.villagehouse.mountainhouse;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.villagehouse.VillageHousePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.SphereBuilder;
import org.terraform.utils.SphereBuilder.SphereType;
import org.terraform.utils.StairwayBuilder;

import java.util.Random;

public class MountainhousePopulator extends VillageHousePopulator {
    @Override
    public void populate(@NotNull TerraformWorld tw, @NotNull PopulatorDataAbstract data) {
        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());
        int[] coords = mc.getCenterBiomeSectionBlockCoords(); // getCoordsFromMegaChunk(tw, mc);
        int x = coords[0];// data.getChunkX()*16 + random.nextInt(16);
        int z = coords[1];// data.getChunkZ()*16 + random.nextInt(16);
        int height = GenUtils.getHighestGround(data, x, z);

        // Forcefully sink the house downwards to try and make it more tolerant to wackiness
        Random random = this.getHashedRandom(tw, data.getChunkX(), data.getChunkZ());
        int sinkDown = GenUtils.randInt(random, 7, 10);
        spawnMountainhouse(tw, random, data, x, height - sinkDown, z);
    }

    public void spawnMountainhouse(@NotNull TerraformWorld tw,
                                   @NotNull Random random,
                                   @NotNull PopulatorDataAbstract data,
                                   int x,
                                   int y,
                                   int z)
    {
        try {

            BlockFace face = BlockUtils.getDirectBlockFace(random);
            Wall core = new Wall(data, x, y, z, face);
            BiomeBank biome = tw.getBiomeBank(x, z);

            new SphereBuilder(random, core.getUp(12), Material.AIR).setRX(17)
                                                                   .setRZ(17)
                                                                   .setRY(18)
                                                                   .setMinRadius(0.7)
                                                                   .setHardReplace(true)
                                                                   .build();

            // Place platform to prevent mountain weirdness
            new SphereBuilder(random, core.getDown(2).getRight(7).getFront(3), Material.DIRT).setRX(12)
                                                                                             .setRZ(12)
                                                                                             .setRY(7)
                                                                                             .setPadding(4)
                                                                                             .setSphereFrequency(0.11f)
                                                                                             .setMinRadius(0.8)
                                                                                             .setHardReplace(false)
                                                                                             .setUpperType(Material.GRASS_BLOCK)
                                                                                             .setSphereType(SphereType.LOWER_SEMISPHERE)
                                                                                             .build();

            // Place schematic
            y += GenUtils.randInt(random, 1, 3);
            core = core.getAtY(y);
            TerraSchematic mountainHouse = TerraSchematic.load("mountainhouse", core);
            mountainHouse.parser = new MountainhouseSchematicParser(biome, random, data);
            mountainHouse.setFace(face);
            mountainHouse.apply();

            TerraformGeneratorPlugin.logger.info("Spawning mountainhouse at "
                                                 + x
                                                 + ","
                                                 + y
                                                 + ","
                                                 + z
                                                 + " with rotation of "
                                                 + mountainHouse.getFace());

            data.addEntity(x, y + 1, z, EntityType.VILLAGER); // Two villagers
            data.addEntity(x, y + 1, z, EntityType.VILLAGER);
            data.addEntity(x, y + 1, z, EntityType.CAT); // And a cat.

            // Spawn a base on the house to sit on
            for (int nx = -17 / 2 - 1; nx <= 17 / 2 + 1; nx++) {
                for (int nz = -17 / 2 - 1; nz <= 17 / 2 + 1; nz++) {
                    if (data.getType(x + nx, y - 1, z + nz).toString().contains("PLANKS")
                        || BlockUtils.isStoneLike(data.getType(x + nx, y - 1, z + nz))
                        || data.getType(x + nx, y - 1, z + nz).toString().contains("STONE_BRICKS"))
                    {
                        BlockUtils.setDownUntilSolid(
                                x + nx,
                                y - 2,
                                z + nz,
                                data,
                                Material.COBBLESTONE,
                                Material.ANDESITE,
                                Material.STONE_BRICKS,
                                Material.CRACKED_STONE_BRICKS,
                                Material.COBBLESTONE,
                                Material.ANDESITE
                        );
                    }
                    else if (data.getType(x + nx, y - 1, z + nz).toString().contains("LOG")) {
                        BlockUtils.setDownUntilSolid(x + nx, y - 2, z + nz, data, data.getType(x + nx, y - 1, z + nz));
                    }
                }
            }

            // Spawn a stairway from the house.
            new StairwayBuilder(Material.COBBLESTONE, Material.STONE_BRICKS).setAngled(false)
                                                                            .setMaxExtensionForward(10)
                                                                            .setStairwayDirection(BlockFace.DOWN)
                                                                            .build(core.getFront(4)
                                                                                       .getRight(4)
                                                                                       .getUp(4))
                                                                            .build(core.getFront(4)
                                                                                       .getRight(7)
                                                                                       .getUp(4));

            new StairwayBuilder(Material.STONE_STAIRS, Material.COBBLESTONE_STAIRS, Material.ANDESITE_STAIRS).setAngled(
                                                                                                                     false)
                                                                                                             .setMaxExtensionForward(
                                                                                                                     10)
                                                                                                             .setStairwayDirection(
                                                                                                                     BlockFace.DOWN)
                                                                                                             .build(core.getFront(
                                                                                                                                4)
                                                                                                                        .getRight(
                                                                                                                                5)
                                                                                                                        .getUp(4))
                                                                                                             .build(core.getFront(
                                                                                                                                4)
                                                                                                                        .getRight(
                                                                                                                                6)
                                                                                                                        .getUp(4));

            new StairwayBuilder(Material.SPRUCE_STAIRS).setAngled(false)
                                                       .setDownTypes(Material.SPRUCE_PLANKS)
                                                       .setMaxExtensionForward(10)
                                                       .setStairwayDirection(BlockFace.DOWN)
                                                       .build(new Wall(
                                                               core.getRear(2).getRight(9).getDown(),
                                                               BlockUtils.getRight(core.getDirection())
                                                       ));

        }
        catch (Throwable e) {
            TerraformGeneratorPlugin.logger.error("Something went wrong trying to place mountain house at "
                                                  + x
                                                  + ","
                                                  + y
                                                  + ","
                                                  + z
                                                  + "!");
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }
    }
}
