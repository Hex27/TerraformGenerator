package org.terraform.v1_20_R2;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.Climate.Sampler;
import net.minecraft.world.level.biome.WorldChunkManager;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_20_R2.block.CraftBiome;
import org.bukkit.craftbukkit.v1_20_R2.block.CraftBlock;
import org.terraform.coregen.HeightMap;
import org.terraform.data.TerraformWorld;
import org.terraform.main.config.TConfigOption;

import java.util.Set;
import java.util.stream.Stream;

public class MapRenderWorldProviderBiome extends WorldChunkManager {
    private final TerraformWorld tw;
    private final IRegistry<BiomeBase> registry;
    @SuppressWarnings("unused")
	private final WorldChunkManager delegate;

    private Set<Holder<BiomeBase>> biomeList;
    @SuppressWarnings("deprecation")
	public MapRenderWorldProviderBiome(TerraformWorld tw, WorldChunkManager delegate) {
        //super(biomeListToBiomeBaseList(CustomBiomeHandler.getBiomeRegistry()));
        this.biomeList = CustomBiomeHandler.biomeListToBiomeBaseSet(CustomBiomeHandler.getBiomeRegistry());
        this.tw = tw;
        this.delegate = delegate;
        this.registry = CustomBiomeHandler.getBiomeRegistry();
        this.river = CraftBiome.bukkitToMinecraftHolder(Biome.RIVER);
        this.plains = CraftBiome.bukkitToMinecraftHolder(Biome.PLAINS);
    }

    @Override
    public Stream<Holder<BiomeBase>> b()
    {
        return this.biomeList.stream();
    }

    @Override //c is getPossibleBiomes
    public Set<Holder<BiomeBase>>  c()
    {
        return this.biomeList;
    }

	@Override
	protected Codec<? extends WorldChunkManager> a() {
		throw new UnsupportedOperationException("Cannot serialize MapRenderWorldProviderBiome");
	}

	private final Holder<BiomeBase> river;
    private final Holder<BiomeBase> plains;
	@SuppressWarnings("unused")
	private static boolean debug = false;
	@Override
	public Holder<BiomeBase> getNoiseBiome(int x, int y, int z, Sampler arg3) {
		//Used to be attempted for cave gen. That didn't work, so now, this is
        //for optimising cartographers and buried treasure.
        //This will return river or plains depending on whether
        //the area is submerged.

        return HeightMap.getBlockHeight(tw, x,z) <= TConfigOption.HEIGHT_MAP_SEA_LEVEL.getInt() ?
                river : plains;
	}

}
