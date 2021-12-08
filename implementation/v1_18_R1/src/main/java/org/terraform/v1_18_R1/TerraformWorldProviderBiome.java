package org.terraform.v1_18_R1;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.block.CraftBlock;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.data.TerraformWorld;
import com.mojang.serialization.Codec;

import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryWritable;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.Climate.Sampler;
import net.minecraft.world.level.biome.WorldChunkManager;

public class TerraformWorldProviderBiome extends WorldChunkManager {

    private final TerraformWorld tw;
    private final IRegistry<BiomeBase> registry;
    private final WorldChunkManager delegate;
    
    private static List<BiomeBase> biomeListToBiomeBaseList(IRegistry<BiomeBase> registry) {

        List<BiomeBase> biomeBases = new ArrayList<>();

        for (Biome biome : Biome.values()) {
        	if(biome == null || biome == Biome.CUSTOM) continue;
            //Preconditions.checkArgument(biome != Biome.CUSTOM, "Cannot use the biome %s", biome);
            biomeBases.add(CraftBlock.biomeToBiomeBase(registry, biome));
        }

        return biomeBases;
    }
    
    public TerraformWorldProviderBiome(TerraformWorld tw, WorldChunkManager delegate) {
        super(biomeListToBiomeBaseList(((CraftServer) Bukkit.getServer()).getServer().aV().b(IRegistry.aR)));
        this.tw = tw;
        this.delegate = delegate;
        this.registry = ((CraftServer) Bukkit.getServer()).getServer().aV().b(IRegistry.aR);
    }

	@Override
	protected Codec<? extends WorldChunkManager> a() {
		throw new UnsupportedOperationException("Cannot serialize TerraformWorldProviderBiome");
	}

	@Override
	public WorldChunkManager a(long arg0) {
		throw new UnsupportedOperationException("Cannot serialize TerraformWorldProviderBiome");
	}
	
	private static boolean debug = false;
	@Override
	public BiomeBase getNoiseBiome(int x, int y, int z, Sampler arg3) {
		//For vanilla cave biome positioning. However, doesn't work now.
//		BiomeBase delegateCandidate = delegate.getNoiseBiome(x, y, z, arg3);
//		if(CraftBlock.biomeBaseToBiome(registry, delegateCandidate) == Biome.LUSH_CAVES 
//				|| CraftBlock.biomeBaseToBiome(registry, delegateCandidate) == Biome.DRIPSTONE_CAVES)
//			return delegateCandidate;
		
		//Left shift x and z 
		BiomeBank bank = tw.getBiomeBank(x << 2, z << 2);
		DedicatedServer dedicatedserver = ((CraftServer) Bukkit.getServer()).getServer();
		
		//aR is the Biome Registry
		//aV is registryAccess
		//b is ownedRegistryOrThrow
        IRegistryWritable<BiomeBase> registrywritable = dedicatedserver.aV().b(IRegistry.aR);
        
		if(bank.getHandler().getCustomBiome() == CustomBiomeType.NONE) {
			
			return CraftBlock.biomeToBiomeBase(registry, bank.getHandler().getBiome());
		} else {
			ResourceKey<BiomeBase> rkey = ResourceKey.a(IRegistry.aR, new MinecraftKey(bank.getHandler().getCustomBiome().getKey()));
	        BiomeBase base = registrywritable.a(rkey);
	        if(base == null) {
	        	String[] split = bank.getHandler().getCustomBiome().getKey().split(":");
	            ResourceKey<BiomeBase> newrkey = ResourceKey.a(IRegistry.aR, new MinecraftKey(split[0],split[1]));
	            base = registrywritable.a(newrkey);
	        }
			
			if(base != null) {
				return base;
			}
			else
				return CraftBlock.biomeToBiomeBase(registry, bank.getHandler().getBiome());
		}
	}

}
