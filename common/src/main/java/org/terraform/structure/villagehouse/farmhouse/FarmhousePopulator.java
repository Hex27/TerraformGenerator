package org.terraform.structure.villagehouse.farmhouse;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.EntityType;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.MegaChunk;
import org.terraform.data.SimpleBlock;
import org.terraform.data.TerraformWorld;
import org.terraform.data.Wall;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.schematic.TerraSchematic;
import org.terraform.structure.villagehouse.VillageHousePopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;
import org.terraform.utils.Temperature;
import org.terraform.utils.noise.FastNoise;
import org.terraform.utils.noise.FastNoise.NoiseType;

import java.util.Random;

public class FarmhousePopulator extends VillageHousePopulator {
    @Override
    public void populate(TerraformWorld tw, PopulatorDataAbstract data) {
        MegaChunk mc = new MegaChunk(data.getChunkX(), data.getChunkZ());
        int[] coords = getCoordsFromMegaChunk(tw, mc);
        int x = coords[0];//data.getChunkX()*16 + random.nextInt(16);
        int z = coords[1];//data.getChunkZ()*16 + random.nextInt(16);
        int height = GenUtils.getHighestGround(data, x, z);
        spawnFarmHouse(tw, this.getHashedRandom(tw, data.getChunkX(), data.getChunkZ()), data, x, height + 1, z);
    }

    public void spawnFarmHouse(TerraformWorld tw, Random random, PopulatorDataAbstract data, int x, int y, int z) {
        try {
            BiomeBank biome = tw.getBiomeBank(x, z);
            y += GenUtils.randInt(random, 1, 3);
            TerraSchematic farmHouse = TerraSchematic.load("farmhouse", new Location(tw.getWorld(), x, y, z));
            farmHouse.parser = new FarmhouseSchematicParser(biome, random, data);
            farmHouse.setFace(BlockUtils.getDirectBlockFace(random));
            farmHouse.apply();

            TerraformGeneratorPlugin.logger.info("Spawning farmhouse at " + x + "," + y + "," + z + " with rotation of " + farmHouse.getFace());

            data.addEntity(x, y + 1, z, EntityType.VILLAGER); //Two villagers
            data.addEntity(x, y + 1, z, EntityType.VILLAGER);
            data.addEntity(x, y + 1, z, EntityType.CAT); //And a cat.

            //Spawn a base on the house to sit on
            for (int nx = -17 / 2 - 1; nx <= 17 / 2 + 1; nx++) {
                for (int nz = -17 / 2 - 1; nz <= 17 / 2 + 1; nz++) {
                    if (data.getType(x + nx, y - 1, z + nz).toString().contains("PLANKS") ||
                            data.getType(x + nx, y - 1, z + nz).toString().contains("STONE_BRICKS"))
                        BlockUtils.setDownUntilSolid(x + nx, y - 2, z + nz, data, Material.COBBLESTONE, Material.COBBLESTONE, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE);
                    else if (data.getType(x + nx, y - 1, z + nz).toString().contains("LOG"))
                        BlockUtils.setDownUntilSolid(x + nx, y - 2, z + nz, data, data.getType(x + nx, y - 1, z + nz));
                }
            }

            //Spawn a stairway from the house.
            Wall w = new Wall(new SimpleBlock(data, x, y - 1, z), farmHouse.getFace()).getRight();
            for (int i = 0; i < 7; i++)
                w = w.getFront();
            //while(w.getType() != Material.DIRT){
            while (!w.getType().isSolid() ||
                    w.getType().toString().contains("PLANKS")) {
                Stairs stairs = (Stairs) Bukkit.createBlockData(GenUtils.randMaterial(random, Material.COBBLESTONE_STAIRS, Material.COBBLESTONE_STAIRS,
                        Material.COBBLESTONE_STAIRS, Material.MOSSY_COBBLESTONE_STAIRS));
                stairs.setFacing(w.getDirection().getOppositeFace());
                w.getRight().setBlockData(stairs);
                w.setBlockData(stairs);
                w.getLeft().setBlockData(stairs);
                w.getLeft().getLeft().getRelative(0, 1, 0).downUntilSolid(random, BlockUtils.getWoodForBiome(biome, "LOG"));
                w.getLeft().getLeft().getRelative(0, 2, 0).setType(GenUtils.randMaterial(random, Material.COBBLESTONE_WALL, Material.COBBLESTONE_WALL, Material.COBBLESTONE_WALL,
                        Material.MOSSY_COBBLESTONE_WALL));
                w.getRight().getRight().getRelative(0, 1, 0).downUntilSolid(random, BlockUtils.getWoodForBiome(biome, "LOG"));
                w.getRight().getRight().getRelative(0, 2, 0).setType(GenUtils.randMaterial(random, Material.COBBLESTONE_WALL, Material.COBBLESTONE_WALL,
                        Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL));
                w = w.getFront().getRelative(0, -1, 0);
            }

            createFields(tw, biome, random, data, x, y, z);

        } catch (Throwable e) {
            TerraformGeneratorPlugin.logger.error("Something went wrong trying to place farmhouse at " + x + "," + y + "," + z + "!");
            e.printStackTrace();
        }
    }

    public void placeLamp(TerraformWorld tw, BiomeBank biome, Random rand, PopulatorDataAbstract data, int x, int y, int z) {
        SimpleBlock b = new SimpleBlock(data, x, y, z);
        b.setType(GenUtils.randMaterial(rand, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS));
        b.getRelative(0, 1, 0).setType(GenUtils.randMaterial(rand, Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL));
        b.getRelative(0, 2, 0).setType(GenUtils.randMaterial(rand, Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL));
        b.getRelative(0, 3, 0).setType(GenUtils.randMaterial(rand, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE));
        b.getRelative(0, 4, 0).setType(Material.CAMPFIRE);
        b.getRelative(0, 5, 0).setType(GenUtils.randMaterial(rand, Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS));
        for (BlockFace face : BlockUtils.directBlockFaces) {
            Slab tSlab = (Slab) Bukkit.createBlockData(GenUtils.randMaterial(rand, Material.STONE_BRICK_SLAB, Material.MOSSY_STONE_BRICK_SLAB));
            tSlab.setType(Type.TOP);
            b.getRelative(face).getRelative(0, 3, 0).setBlockData(tSlab);
            b.getRelative(face).getRelative(0, 4, 0).setType(GenUtils.randMaterial(rand, Material.COBBLESTONE_WALL, Material.MOSSY_COBBLESTONE_WALL));
            b.getRelative(face).getRelative(0, 5, 0).setType(GenUtils.randMaterial(rand, Material.STONE_BRICK_SLAB, Material.MOSSY_STONE_BRICK_SLAB));
        }
    }

    public void createFields(TerraformWorld tw, BiomeBank biome, Random random, PopulatorDataAbstract data, int x, int y, int z) {
        FastNoise fieldNoise = new FastNoise(tw.getHashedRand(x, y, z, 23).nextInt(225));
        fieldNoise.SetNoiseType(NoiseType.Simplex);
        fieldNoise.SetFrequency(0.05f);

        FastNoise radiusNoise = new FastNoise(tw.getHashedRand(x, y, z, 23).nextInt(225));
        radiusNoise.SetNoiseType(NoiseType.Cubic);
        radiusNoise.SetFrequency(0.09f);

        Material cropOne = Material.WHEAT;
        Material cropTwo = Material.CARROTS;

        if (BiomeBank.getBiomeSectionFromBlockCoords(tw, x, z).getTemperature() <= Temperature.SNOWY) {
            cropOne = Material.POTATOES;
            cropTwo = Material.BEETROOTS;
        }

        for (int nx = -50; nx <= 50; nx++) {
            for (int nz = -50; nz <= 50; nz++) {
                int height = GenUtils.getTrueHighestBlock(data, x + nx, z + nz);
                if (!BlockUtils.isDirtLike(data.getType(x + nx, height, z + nz)) ||
                        data.getType(x + nx, height + 1, z + nz) != Material.AIR)
                    continue;

                double noise = fieldNoise.GetNoise(nx + x, nz + z);

                double dist = Math.pow(nx, 2) + Math.pow(nz, 2);
                double multiplier = Math.pow((1 / (dist - 2500)) + 1, 255);
                if (multiplier < 0 || dist > 2500 + (radiusNoise.GetNoise(nx, nz) * 500.0)) multiplier = 0;
                noise = noise * multiplier;

                if (dist < 2500)
                    if (GenUtils.chance(random, 1, 300)) {
                        data.setType(x + nx, height + 1, z + nz, Material.COMPOSTER);
                        continue;
                    }

                if (noise < -0.2) { //Crop one
                    if (GenUtils.chance(random, 1, 15)) {
                        data.setType(nx + x, height, nz + z, Material.WATER);
                        for (BlockFace face : BlockUtils.directBlockFaces) {
                            BlockUtils.setDownUntilSolid(nx + x + face.getModX(), height, nz + z + face.getModZ(),
                                    data,
                                    Material.FARMLAND);
                        }
                    } else {
                        Farmland fl = (Farmland) Bukkit.createBlockData(Material.FARMLAND);
                        fl.setMoisture(fl.getMaximumMoisture());
                        data.setBlockData(nx + x, height, nz + z, fl);
                        Ageable crop = (Ageable) Bukkit.createBlockData(cropOne);
                        crop.setAge(GenUtils.randInt(random, 0, crop.getMaximumAge()));
                        if (!data.getType(nx + x, height + 1, nz + z).isSolid())
                            data.setBlockData(nx + x, height + 1, nz + z, crop);
                    }
                } else if (noise > 0.2) { //Crop two
                    if (GenUtils.chance(random, 1, 15)) {
                        data.setType(nx + x, height, nz + z, Material.WATER);
                        for (BlockFace face : BlockUtils.directBlockFaces) {
                            BlockUtils.setDownUntilSolid(nx + x + face.getModX(), height, nz + z + face.getModZ(),
                                    data,
                                    Material.FARMLAND);
                        }
                    } else {
                        Farmland fl = (Farmland) Bukkit.createBlockData(Material.FARMLAND);
                        fl.setMoisture(fl.getMaximumMoisture());
                        data.setBlockData(nx + x, height, nz + z, fl);
                        Ageable crop = (Ageable) Bukkit.createBlockData(cropTwo);
                        crop.setAge(GenUtils.randInt(random, 0, crop.getMaximumAge()));
                        data.setBlockData(nx + x, height + 1, nz + z, crop);
                    }
                } else if (Math.abs(noise) < 0.2 && Math.abs(noise) > 0.1) { //Grass hedges
                    BlockUtils.setPersistentLeaves(data, nx + x, height + 1, nz + z, BlockUtils.getWoodForBiome(biome, "LEAVES"));

                    if (GenUtils.chance(random, 1, 100)) {
                        placeLamp(tw, biome, random, data, nx + x, height + 1, z + nz);
                    }
                    //data.setType(nx+x, height+1, nz+z, Material.OAK_LEAVES);
                } else {
                    if (GenUtils.chance(random, (int) (100 * Math.pow(multiplier, 3)), 100)) {
                        data.setType(nx + x, height, nz + z, GenUtils.randMaterial(random, Material.COBBLESTONE, Material.MOSSY_COBBLESTONE));
                    }
                }

            }
        }

    }


}
