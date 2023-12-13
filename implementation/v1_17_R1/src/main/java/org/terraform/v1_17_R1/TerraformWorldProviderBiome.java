package org.terraform.v1_17_R1;

import com.mojang.serialization.Codec;
import net.minecraft.core.IRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.WorldChunkManager;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class TerraformWorldProviderBiome extends WorldChunkManager {
    private final TerraformWorld tw;
    private final IRegistry<BiomeBase> registry;
    @SuppressWarnings("unused")
	private final WorldChunkManager delegate;

    @SuppressWarnings("deprecation")
	public TerraformWorldProviderBiome(TerraformWorld tw, WorldChunkManager delegate) {
        super(biomeListToBiomeBaseList(CustomBiomeHandler.getBiomeRegistry()));
        this.tw = tw;
        this.delegate = delegate;
        this.registry = CustomBiomeHandler.getBiomeRegistry();
    }

    public static List<BiomeBase> biomeListToBiomeBaseList(IRegistry<BiomeBase> registry) {

        List<BiomeBase> biomeBases = new ArrayList<>();

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
            Optional<BiomeBase> holder = Optional.ofNullable(registry.d(rkey));
            holder.ifPresent(biomeBases::add);
        }

        return biomeBases;
    }

	@Override
	protected Codec<? extends WorldChunkManager> a() {
		throw new UnsupportedOperationException("Cannot serialize TerraformWorldProviderBiome");
	}

    @Override
    public WorldChunkManager a(long l) {
        return null;
    }

    @SuppressWarnings("unused")
	private static boolean debug = false;
	@Override
	public BiomeBase getBiome(int x, int y, int z) {
        //Used for biome generation in NMSChunkGenerator.
        //Left shift x and z
        BiomeBank bank = tw.getBiomeBank(x << 2, z << 2);

        if(bank.getHandler().getCustomBiome() == CustomBiomeType.NONE) {

            return CraftBlock.biomeToBiomeBase(registry, bank.getHandler().getBiome());
        } else {
            ResourceKey<BiomeBase> rkey = CustomBiomeHandler.terraformGenBiomeRegistry.get(bank.getHandler().getCustomBiome()); //ResourceKey.a(IRegistry.aP, new MinecraftKey(bank.getHandler().getCustomBiome().getKey()));
            Optional<BiomeBase> holder = Optional.ofNullable(registry.d(rkey));
            if(holder.isEmpty())
                TerraformGeneratorPlugin.logger.error("Custom biome was not found in the vanilla registry!");

            return holder.orElseGet(() -> CraftBlock.biomeToBiomeBase(registry, bank.getHandler().getBiome()));
        }
    }
}
