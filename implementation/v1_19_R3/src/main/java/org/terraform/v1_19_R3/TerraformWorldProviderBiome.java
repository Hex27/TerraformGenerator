package org.terraform.v1_19_R3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

import com.mojang.serialization.Codec;

import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate.Sampler;
import net.minecraft.world.level.biome.WorldChunkManager;

public class TerraformWorldProviderBiome extends WorldChunkManager {

	//Idk what this is for
	public static class TerraformBiomeResolverProxy implements BiomeResolver{
		final TerraformWorldProviderBiome delegate;
		public TerraformBiomeResolverProxy(TerraformWorldProviderBiome delegate) {
			this.delegate = delegate;
		}
		
		@Override
		public @Nullable Holder<BiomeBase> getNoiseBiome(int arg0, int arg1, int arg2, Sampler arg3) {
			return delegate.getNoiseBiome(arg0, arg1, arg2, arg3);
		}
	}
	
    private final TerraformWorld tw;
    private final IRegistry<BiomeBase> registry;

    private static Set<Holder<BiomeBase>> biomeListToBiomeBaseSet(@NotNull IRegistry<BiomeBase> registry) {

        List<Holder<BiomeBase>> biomeBases = new ArrayList<>();

        for (Biome biome : Biome.values()) {
        	if(biome == null || biome == Biome.CUSTOM) continue;
            try {
                //Preconditions.checkArgument(biome != Biome.CUSTOM, "Cannot use the biome %s", biome);
                biomeBases.add(CraftBlock.biomeToBiomeBase(registry, biome));
            } catch(IllegalStateException e) {
                TerraformGeneratorPlugin.logger.info("Ignoring biome " + biome);
            }
        }
        
        for(CustomBiomeType cbt:CustomBiomeType.values()) {
        	if(cbt == CustomBiomeType.NONE) continue;
        	ResourceKey<BiomeBase> rkey = CustomBiomeHandler.terraformGenBiomeRegistry.get(cbt);
        	//TerraformGeneratorPlugin.logger.info(cbt + " --- " + rkey);
            //Holder.c is Holder.Reference. It implements Holder. No idk why.
	        Optional<Holder.c<BiomeBase>> holder = registry.b(rkey);
            holder.ifPresent(biomeBases::add);
        }

        return Set.copyOf(biomeBases);
    }

    private final Set<Holder<BiomeBase>> biomeList;
    public TerraformWorldProviderBiome(TerraformWorld tw, WorldChunkManager delegate) {
        //super(biomeListToBiomeBaseList(CustomBiomeHandler.getBiomeRegistry()));
        this.biomeList = biomeListToBiomeBaseSet(CustomBiomeHandler.getBiomeRegistry());
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
		//For vanilla cave biome positioning. However, doesn't work now.
//		BiomeBase delegateCandidate = delegate.getNoiseBiome(x, y, z, arg3);
//		if(CraftBlock.biomeBaseToBiome(registry, delegateCandidate) == Biome.LUSH_CAVES 
//				|| CraftBlock.biomeBaseToBiome(registry, delegateCandidate) == Biome.DRIPSTONE_CAVES)
//			return delegateCandidate;
		
		//Left shift x and z 
		BiomeBank bank = tw.getBiomeBank(x << 2, z << 2);

        if(bank.getHandler().getCustomBiome() == CustomBiomeType.NONE) {
			
			return CraftBlock.biomeToBiomeBase(registry, bank.getHandler().getBiome());
		} else {
			ResourceKey<BiomeBase> rkey = CustomBiomeHandler.terraformGenBiomeRegistry.get(bank.getHandler().getCustomBiome()); //ResourceKey.a(IRegistry.aP, new MinecraftKey(bank.getHandler().getCustomBiome().getKey()));
			Optional<Holder.c<BiomeBase>> holder = registry.b(rkey); //g is getHolderOrThrow
	        if(holder.isEmpty()) {
                TerraformGeneratorPlugin.logger.error("Custom biome was not found in the vanilla registry!");
	        }
			
			if(holder.isPresent()) {
				return holder.get();
			}
			else
				return CraftBlock.biomeToBiomeBase(registry, bank.getHandler().getBiome());
		}
	}

}
