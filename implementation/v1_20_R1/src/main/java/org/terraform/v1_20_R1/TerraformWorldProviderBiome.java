package org.terraform.v1_20_R1;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.Climate.Sampler;
import net.minecraft.world.level.biome.WorldChunkManager;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.jetbrains.annotations.Nullable;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class TerraformWorldProviderBiome extends WorldChunkManager {
    private final TerraformWorld tw;
    private final IRegistry<BiomeBase> registry;


    private final Set<Holder<BiomeBase>> biomeList;
    public TerraformWorldProviderBiome(TerraformWorld tw, WorldChunkManager delegate) {
        //super(biomeListToBiomeBaseList(CustomBiomeHandler.getBiomeRegistry()));
        this.biomeList = CustomBiomeHandler.biomeListToBiomeBaseSet(CustomBiomeHandler.getBiomeRegistry());
        this.tw = tw;
        this.registry = CustomBiomeHandler.getBiomeRegistry();
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
		throw new UnsupportedOperationException("Cannot serialize TerraformWorldProviderBiome");
	}
	@SuppressWarnings("unused")
	private static final boolean debug = false;
	@Override
	public @Nullable Holder<BiomeBase> getNoiseBiome(int x, int y, int z, Sampler arg3) {
        //Used for biome generation in NMSChunkGenerator.
        //Left shift x and z
        BiomeBank bank = tw.getBiomeBank(x << 2, z << 2);

        if(bank.getHandler().getCustomBiome() == CustomBiomeType.NONE) {

            return CraftBlock.biomeToBiomeBase(registry, bank.getHandler().getBiome());
        } else {
            ResourceKey<BiomeBase> rkey = CustomBiomeHandler.terraformGenBiomeRegistry.get(bank.getHandler().getCustomBiome()); //ResourceKey.a(IRegistry.aP, new MinecraftKey(bank.getHandler().getCustomBiome().getKey()));
            Optional<Holder.c<BiomeBase>> holder = registry.b(rkey); //g is getHolderOrThrow
            if(holder.isEmpty())
                TerraformGeneratorPlugin.logger.error("Custom biome was not found in the vanilla registry!");


            if(holder.isPresent()) {
                return holder.get();
            } else
                return CraftBlock.biomeToBiomeBase(registry, bank.getHandler().getBiome());
        }
    }

}
