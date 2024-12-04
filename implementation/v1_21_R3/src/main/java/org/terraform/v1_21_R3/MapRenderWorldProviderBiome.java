package org.terraform.v1_21_R3;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.Climate.Sampler;
import net.minecraft.world.level.biome.WorldChunkManager;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_21_R3.block.CraftBiome;
import org.terraform.coregen.HeightMap;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfig;

import java.util.Set;
import java.util.stream.Stream;

public class MapRenderWorldProviderBiome extends WorldChunkManager {
    @SuppressWarnings("unused")
    private static final boolean debug = false;
    private final TerraformWorld tw;
    private final Set<Holder<BiomeBase>> biomeList;
    private final Holder<BiomeBase> river;
    private final Holder<BiomeBase> plains;

    public MapRenderWorldProviderBiome(TerraformWorld tw, WorldChunkManager delegate) {
        // super(biomeListToBiomeBaseList(CustomBiomeHandler.getBiomeRegistry()));
        this.biomeList = CustomBiomeHandler.biomeListToBiomeBaseSet(CustomBiomeHandler.getBiomeRegistry());
        this.tw = tw;
        IRegistry<BiomeBase> registry = CustomBiomeHandler.getBiomeRegistry();
        this.river = CraftBiome.bukkitToMinecraftHolder(Biome.RIVER);
        this.plains = CraftBiome.bukkitToMinecraftHolder(Biome.PLAINS);
    }

    @Override
    public Stream<Holder<BiomeBase>> b()
    {
        return this.biomeList.stream();
    }

    @Override // c is getPossibleBiomes
    public Set<Holder<BiomeBase>> c()
    {
        return this.biomeList;
    }

    @Override
    protected MapCodec<? extends WorldChunkManager> a() {
        throw new UnsupportedOperationException("Cannot serialize MapRenderWorldProviderBiome");
    }

    @Override
    public Holder<BiomeBase> getNoiseBiome(int x, int y, int z, Sampler arg3) {
        // Used to be attempted for cave gen. That didn't work, so now, this is
        // for optimising cartographers and buried treasure.
        // This will return river or plains depending on whether
        // the area is submerged.

        return HeightMap.getBlockHeight(tw, x, z) <= TConfig.c.HEIGHT_MAP_SEA_LEVEL ? river : plains;
    }

}
