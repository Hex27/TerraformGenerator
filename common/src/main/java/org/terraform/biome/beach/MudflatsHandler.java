package org.terraform.biome.beach;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.Rotatable;
import org.terraform.biome.BiomeHandler;
import org.terraform.coregen.PopulatorDataAbstract;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TConfigOption;
import org.terraform.structure.small.WitchHutPopulator;
import org.terraform.utils.BlockUtils;
import org.terraform.utils.GenUtils;

import java.util.Random;

public class MudflatsHandler extends BiomeHandler {

    @Override
    public boolean isOcean() {
        return false;
    }

    @Override
    public Biome getBiome() {
        return Biome.SWAMP;
    }

//	@Override
//	public int getHeight(int x, int z, Random rand) {
//		SimplexOctaveGenerator gen = new SimplexOctaveGenerator(rand, 2);
//		gen.setScale(0.005);
//		
//		return (int) (gen.noise(x, z, 0.5, 0.5)*7D+50D);
//	}

    @Override
    public Material[] getSurfaceCrust(Random rand) {
        return new Material[] {GenUtils.weightedRandomMaterial(rand, Material.PODZOL, 35, Material.GRASS_BLOCK, 10),
                GenUtils.randMaterial(rand, Material.DIRT),
                GenUtils.randMaterial(rand, Material.DIRT),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE),
                GenUtils.randMaterial(rand, Material.DIRT, Material.STONE)};
    }

    @Override
    public void populate(TerraformWorld tw, Random random, PopulatorDataAbstract data) {

        boolean spawnHut = GenUtils.chance(tw.getHashedRand(data.getChunkX(), data.getChunkZ(), 66666),
                TConfigOption.STRUCTURES_SWAMPHUT_CHANCE_OUT_OF_TEN_THOUSAND.getInt(), 10000);

        for(int x = data.getChunkX() * 16; x < data.getChunkX() * 16 + 16; x++) {
            for(int z = data.getChunkZ() * 16; z < data.getChunkZ() * 16 + 16; z++) {
                int y = GenUtils.getHighestGround(data, x, z);
                if(data.getBiome(x, z) != getBiome()) continue;
                y++;
                if(data.getType(x, y, z) != Material.AIR) continue;
                if(GenUtils.chance(5, 100)) {
                    if(random.nextBoolean())
                        BlockUtils.setDoublePlant(data, x, y, z, Material.TALL_GRASS);
                    else
                        data.setType(x, y, z, Material.GRASS);
                }
                if(spawnHut
                        && TConfigOption.STRUCTURES_SWAMPHUT_SPAWN_MUDFLAT_HEADS.getBoolean()
                        && GenUtils.chance(10, 100)) {
                    if(BlockUtils.isDirtLike(data.getType(x, y - 1, z))) {
                        Rotatable skull = (Rotatable) Bukkit.createBlockData(Material.PLAYER_HEAD);
                        skull.setRotation(BlockUtils.getXZPlaneBlockFace(random));

                        data.setType(x, y, z, Material.OAK_FENCE);
                        data.setBlockData(x, y + 1, z, skull);
                    }
                }
            }
        }

        if(spawnHut) {
            WitchHutPopulator whp = new WitchHutPopulator();
            whp.populate(tw, random, data);
        }
    }
}
