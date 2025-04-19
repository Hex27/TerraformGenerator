package org.terraform.structure.small;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.NaturalSpawnType;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.coregen.populatordata.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.schematic.SchematicParser;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.MultiMegaChunkStructurePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.io.FileNotFoundException;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class WitchHutPopulator extends MultiMegaChunkStructurePopulator {

    @Override
    public void populate(@NotNull TerraformWorld tw, @NotNull PopulatorDataAbstract data) {
        if (!isEnabled()) {
            return;
        }

        Random random = this.getHashedRandom(tw, data.getChunkX(), data.getChunkZ());
        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());
        for (int[] coords : getCoordsFromMegaChunk(tw, mc)) {
            int x = coords[0];
            int z = coords[1];
            if (x >> 4 != data.getChunkX() || z >> 4 != data.getChunkZ()) {
                continue;
            }
            int height = GenUtils.getHighestGround(data, x, z);
            if (height < TerraformGenerator.seaLevel) { // Assume. it's on water
                height = TerraformGenerator.seaLevel + GenUtils.randInt(random, 2, 3);
            }
            else {
                height += GenUtils.randInt(random, 2, 3);
            }
            spawnSwampHut(tw, random, data, x, height, z);
        }
    }

    public void spawnSwampHut(TerraformWorld tw,
                              @NotNull Random random,
                              @NotNull PopulatorDataAbstract data,
                              int x,
                              int y,
                              int z)
    {

        // Refers to center of hut, above the water.
        SimpleBlock core = new SimpleBlock(data, x, y, z);
        TerraformGeneratorPlugin.logger.info("Spawning Swamp Hut at " + core.getCoords());
        try {
            BlockFace face = BlockUtils.getDirectBlockFace(random);
            TerraSchematic swamphut = TerraSchematic.load("swamphut", core);
            swamphut.parser = new WitchHutSchematicParser(random, data);
            swamphut.setFace(face);
            swamphut.apply();
            Wall w = new Wall(core.getDown(2), face).getRear();

            // Pillars down
            w.getFront().getRight().downUntilSolid(random, Material.OAK_LOG);
            w.getFront().getLeft(2).downUntilSolid(random, Material.OAK_LOG);
            w.getRear(2).getRight().downUntilSolid(random, Material.OAK_LOG);
            w.getRear(2).getLeft(2).downUntilSolid(random, Material.OAK_LOG);

            x = w.getRear(2).get().getX();
            z = w.getRear(2).get().getZ();
            data.addEntity(x, y + 1, z, EntityType.WITCH);
            data.addEntity(x, y + 1, z, EntityType.CAT);
        }
        catch (FileNotFoundException e) {
            TerraformGeneratorPlugin.logger.stackTrace(e);
        }

        //Register this as a swamp hut with custom spawns
        //7x7x9 (x,y,z)
        Objects.requireNonNull(TerraformGeneratorPlugin.injector.getICAData(data))
               .registerNaturalSpawns(
                NaturalSpawnType.WITCH,
                x - 3,
                    y,
                z - 4,
                x + 3,
                y + 7,
                z + 4
            );

    }

    private boolean rollSpawnRatio(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        return GenUtils.chance(tw.getHashedRand(chunkX, chunkZ, 8242112),
                (int) (TConfig.c.STRUCTURES_SWAMPHUT_SPAWNRATIO * 10000),
                10000
        );
    }

    @Override
    public boolean canSpawn(@NotNull TerraformWorld tw, int chunkX, int chunkZ) {
        if (!isEnabled()) {
            return false;
        }

        MegaChunk mc = new MegaChunk(chunkX, chunkZ);
        int[][] allCoords = getCoordsFromMegaChunk(tw, mc);
        for (int[] coords : allCoords) {
            if (coords[0] >> 4 == chunkX && coords[1] >> 4 == chunkZ) {
                EnumSet<BiomeBank> biomes = GenUtils.getBiomesInChunk(tw, chunkX, chunkZ);
                for (BiomeBank b : biomes) {
                    if (b != BiomeBank.SWAMP && b != BiomeBank.MANGROVE) {
                        return false;
                    }
                }
                return rollSpawnRatio(tw, chunkX, chunkZ);
            }
        }
        return false;
    }

    @Override
    public int[][] getCoordsFromMegaChunk(@NotNull TerraformWorld tw, @NotNull MegaChunk mc) {
        int num = TConfig.c.STRUCTURES_SWAMPHUT_COUNT_PER_MEGACHUNK;
        int[][] coords = new int[num][2];
        for (int i = 0; i < num; i++) {
            coords[i] = mc.getRandomCoords(tw.getHashedRand(mc.getX(), mc.getZ(), 819227 * (1 + i)));
        }
        return coords;
    }

    @Override
    public int[] getNearestFeature(@NotNull TerraformWorld tw, int rawX, int rawZ) {
        MegaChunk mc = new MegaChunk(rawX, 0, rawZ);

        double minDistanceSquared = Integer.MAX_VALUE;
        int[] min = null;
        for (int nx = -1; nx <= 1; nx++) {
            for (int nz = -1; nz <= 1; nz++) {
                for (int[] loc : getCoordsFromMegaChunk(tw, mc)) {
                    double distSqr = Math.pow(loc[0] - rawX, 2) + Math.pow(loc[1] - rawZ, 2);
                    if (distSqr < minDistanceSquared) {
                        minDistanceSquared = distSqr;
                        min = loc;
                    }
                }
            }
        }
        return min;
    }

    @Override
    public boolean isEnabled() {
        return TConfig.areStructuresEnabled() && TConfig.c.STRUCTURES_SWAMPHUT_ENABLED && (
                TConfig.c.BIOME_SWAMP_WEIGHT > 0
                || TConfig.c.BIOME_MANGROVE_WEIGHT > 0);
    }

    @Override
    public @NotNull Random getHashedRandom(@NotNull TerraformWorld world, int chunkX, int chunkZ) {
        return world.getHashedRand(1211221, chunkX, chunkZ);
    }

    @Override
    public int getChunkBufferDistance() {
        return 1;
    }

    private static class WitchHutSchematicParser extends SchematicParser {
        private final Random rand;
        private final PopulatorDataAbstract pop;

        public WitchHutSchematicParser(Random rand, PopulatorDataAbstract pop) {
            this.rand = rand;
            this.pop = pop;
        }

        @Override
        public void applyData(@NotNull SimpleBlock block, @NotNull BlockData data) {
            if (data.getMaterial().toString().contains("COBBLESTONE")) {
                data = Bukkit.createBlockData(StringUtils.replace(
                        data.getAsString(),
                        "cobblestone",
                        GenUtils.randChoice(rand,
                                Material.COBBLESTONE,
                                Material.COBBLESTONE,
                                Material.COBBLESTONE,
                                Material.MOSSY_COBBLESTONE
                        ).name().toLowerCase(Locale.ENGLISH)
                ));
                super.applyData(block, data);

                if (GenUtils.chance(1, 5)) {
                    BlockUtils.vineUp(block, 2);
                }
            }
            else if (data.getMaterial().toString().startsWith("OAK")) {
                super.applyData(block, data);
                if (data.getMaterial().toString().endsWith("LOG")) {
                    if (GenUtils.chance(1, 5)) {
                        BlockUtils.vineUp(block, 2);
                    }
                }
                super.applyData(block, data);
            }
            else if (data.getMaterial() == Material.CHEST) {
                super.applyData(block, data);
                pop.lootTableChest(block.getX(), block.getY(), block.getZ(), TerraLootTable.VILLAGE_TEMPLE);
            }
            else {
                super.applyData(block, data);
            }
        }
    }
}
