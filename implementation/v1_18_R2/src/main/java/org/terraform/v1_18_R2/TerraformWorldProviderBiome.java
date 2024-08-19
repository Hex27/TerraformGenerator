package org.terraform.v1_18_R2;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.level.biome.BiomeBase;
import net.minecraft.world.level.biome.Climate.Sampler;
import net.minecraft.world.level.biome.WorldChunkManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.biome.BiomeBank;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;

import java.util.ArrayList;
import java.util.List;

public class TerraformWorldProviderBiome extends WorldChunkManager {

    @SuppressWarnings("unused")
    private static final boolean debug = false;
    private final TerraformWorld tw;
    private final IRegistry<BiomeBase> registry;

    public TerraformWorldProviderBiome(TerraformWorld tw, WorldChunkManager delegate) {
        super(biomeListToBiomeBaseList(((CraftServer) Bukkit.getServer()).getServer().aU().b(IRegistry.aP)));
        this.tw = tw;
        this.registry = ((CraftServer) Bukkit.getServer()).getServer().aU().b(IRegistry.aP);
    }

    private static @NotNull List<Holder<BiomeBase>> biomeListToBiomeBaseList(@NotNull IRegistry<BiomeBase> registry) {

        List<Holder<BiomeBase>> biomeBases = new ArrayList<>();

        for (Biome biome : Biome.values()) {
            if (biome == null || biome == Biome.CUSTOM) {
                continue;
            }
            // Preconditions.checkArgument(biome != Biome.CUSTOM, "Cannot use the biome %s", biome);
            biomeBases.add(CraftBlock.biomeToBiomeBase(registry, biome));
        }

        for (CustomBiomeType cbt : CustomBiomeType.values()) {
            if (cbt == CustomBiomeType.NONE) {
                continue;
            }
            ResourceKey<BiomeBase> rkey = CustomBiomeHandler.terraformGenBiomeRegistry.get(cbt);
            // TerraformGeneratorPlugin.logger.info(cbt + " --- " + rkey);
            Holder<BiomeBase> holder = registry.g(rkey);
            if (holder != null) {
                biomeBases.add(holder);
            }
        }

        return biomeBases;
    }

    @Override
    protected Codec<? extends WorldChunkManager> a() {
        throw new UnsupportedOperationException("Cannot serialize TerraformWorldProviderBiome");
    }

    @Override
    public WorldChunkManager a(long arg0) {
        throw new UnsupportedOperationException("Cannot serialize TerraformWorldProviderBiome");
    }

    @Override
    public @Nullable Holder<BiomeBase> getNoiseBiome(int x, int y, int z, Sampler arg3) {
        // For vanilla cave biome positioning. However, doesn't work now.
        //		BiomeBase delegateCandidate = delegate.getNoiseBiome(x, y, z, arg3);
        //		if(CraftBlock.biomeBaseToBiome(registry, delegateCandidate) == Biome.LUSH_CAVES
        //				|| CraftBlock.biomeBaseToBiome(registry, delegateCandidate) == Biome.DRIPSTONE_CAVES)
        //			return delegateCandidate;

        // Left shift x and z
        BiomeBank bank = tw.getBiomeBank(x << 2, z << 2);
        DedicatedServer dedicatedserver = ((CraftServer) Bukkit.getServer()).getServer();

        IRegistry<BiomeBase> iregistry = dedicatedserver.aU().b(IRegistry.aP);

        if (bank.getHandler().getCustomBiome() == CustomBiomeType.NONE) {

            return CraftBlock.biomeToBiomeBase(registry, bank.getHandler().getBiome());
        }
        else {
            ResourceKey<BiomeBase> rkey = CustomBiomeHandler.terraformGenBiomeRegistry.get(bank.getHandler()
                                                                                               .getCustomBiome()); // ResourceKey.a(IRegistry.aP, new MinecraftKey(bank.getHandler().getCustomBiome().getKey()));
            Holder<BiomeBase> holder = iregistry.g(rkey); // g is getHolderOrThrow
            if (holder == null) {
                TerraformGeneratorPlugin.logger.error("Custom biome was not found in the vanilla registry!");
            }

            if (holder != null) {
                return holder;
            }
            else {
                return CraftBlock.biomeToBiomeBase(registry, bank.getHandler().getBiome());
            }
        }
    }

}
