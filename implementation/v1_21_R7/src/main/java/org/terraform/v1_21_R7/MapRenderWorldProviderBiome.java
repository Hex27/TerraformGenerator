package org.terraform.v1_21_R7;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate.Sampler;
import net.minecraft.world.level.biome.BiomeSource;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.terraform.coregen.HeightMap;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;

import java.util.Set;
import java.util.stream.Stream;

public class MapRenderWorldProviderBiome extends BiomeSource {
    @SuppressWarnings("unused")
    private static final boolean debug = false;
    private final TerraformWorld tw;
    private final Set<Holder<Biome>> biomeList;
    private final Holder<Biome> river;
    private final Holder<Biome> plains;

    public MapRenderWorldProviderBiome(TerraformWorld tw, BiomeSource delegate) {
        // super(biomeListToBiomeList(CustomBiomeHandler.getBiomeRegistry()));
        this.biomeList = CustomBiomeHandler.biomeListToBiomeSet(CustomBiomeHandler.getBiomeRegistry());
        this.tw = tw;
        Registry<Biome> registry = CustomBiomeHandler.getBiomeRegistry();
        this.river = CraftBiome.bukkitToMinecraftHolder(org.bukkit.block.Biome.RIVER);
        this.plains = CraftBiome.bukkitToMinecraftHolder(org.bukkit.block.Biome.PLAINS);
    }

    @Override
    public Stream<Holder<Biome>> collectPossibleBiomes()
    {
        return this.biomeList.stream();
    }

    @Override // c is possibleBiomes
    public Set<Holder<Biome>> possibleBiomes()
    {
        return this.biomeList;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        throw new UnsupportedOperationException("Cannot serialize MapRenderWorldProviderBiome");
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Sampler arg3) {
        // Used to be attempted for cave gen. That didn't work, so now, this is
        // for optimising cartographers and buried treasure.
        // This will return river or plains depending on whether
        // the area is submerged.

        return HeightMap.getBlockHeight(tw, x, z) <= TConfig.c.HEIGHT_MAP_SEA_LEVEL ? river : plains;
    }

}
