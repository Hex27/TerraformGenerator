package org.terraform.v1_21_R7;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate.Sampler;
import net.minecraft.world.level.biome.BiomeSource;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.jetbrains.annotations.Nullable;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class TerraformWorldProviderBiome extends BiomeSource {
    @SuppressWarnings("unused")
    private static final boolean debug = false;
    private final TerraformWorld tw;
    private final Registry<Biome> registry;
    private final Set<Holder<Biome>> biomeList;

    public TerraformWorldProviderBiome(TerraformWorld tw, BiomeSource delegate) {
        // super(biomeListToBiomeList(CustomBiomeHandler.getBiomeRegistry()));
        this.biomeList = CustomBiomeHandler.biomeListToBiomeSet(CustomBiomeHandler.getBiomeRegistry());
        this.tw = tw;
        this.registry = CustomBiomeHandler.getBiomeRegistry();
    }

    @Override
    public Stream<Holder<Biome>> collectPossibleBiomes()
    {
        return this.biomeList.stream();
    }

    @Override // c is getPossibleBiomes
    public Set<Holder<Biome>> possibleBiomes()
    {
        return this.biomeList;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        throw new UnsupportedOperationException("Cannot serialize TerraformWorldProviderBiome");
    }

    @Override
    public @Nullable Holder<Biome> getNoiseBiome(int x, int y, int z, Sampler arg3) {
        // Used for biome generation in NMSChunkGenerator.
        // Left shift x and z
        BiomeBank bank = tw.getBiomeBank(x << 2, z << 2);
        if (bank.getHandler().getCustomBiome() == CustomBiomeType.NONE) {

            return CraftBiome.bukkitToMinecraftHolder(bank.getHandler().getBiome());
        }
        else {
            ResourceKey<Biome> rkey = CustomBiomeHandler.terraformGenBiomeRegistry.get(bank.getHandler()
                                                                                               .getCustomBiome()); // ResourceKey.a(Registry.aP, new MinecraftKey(bank.getHandler().getCustomBiome().getKey()));
            Optional<Holder.Reference<Biome>> holder = registry.get(rkey);
            if (holder.isEmpty()) {
                TerraformGeneratorPlugin.logger.error("Custom biome was not found in the vanilla registry!");
            }

            if (holder.isPresent()) {
                return holder.get();
            }
            else {
                return CraftBiome.bukkitToMinecraftHolder(bank.getHandler().getBiome());
            }
        }
    }

}
