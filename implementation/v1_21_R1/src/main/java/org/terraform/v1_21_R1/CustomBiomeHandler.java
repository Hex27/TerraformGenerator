package org.terraform.v1_21_R1;

import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryWritable;
import net.minecraft.core.RegistryMaterials;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.biome.BiomeFog.GrassColor;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_21_R1.CraftServer;
import org.bukkit.craftbukkit.v1_21_R1.block.CraftBiome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.main.TerraformGeneratorPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class CustomBiomeHandler {

    public static final HashMap<CustomBiomeType, ResourceKey<BiomeBase>> terraformGenBiomeRegistry = new HashMap<>();

    public static IRegistry<BiomeBase> getBiomeRegistry() {
        return MinecraftServer.getServer().bc().d(Registries.aF);
    }

    public static void init() {
        CraftServer craftserver = (CraftServer) Bukkit.getServer();
        DedicatedServer dedicatedserver = craftserver.getServer();
        IRegistryWritable<BiomeBase> registrywritable = (IRegistryWritable<BiomeBase>) getBiomeRegistry();

        // Unfreeze the biome registry
        try {
            Field frozen = RegistryMaterials.class.getDeclaredField("l");
            frozen.setAccessible(true);
            frozen.set(registrywritable, false);
            TerraformGeneratorPlugin.logger.info("Unfreezing biome registry...");
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
            TerraformGeneratorPlugin.logger.stackTrace(e1);
        }

        BiomeBase forestbiome = registrywritable.a(Biomes.i); // Use forest biome as a base

        for (CustomBiomeType type : CustomBiomeType.values()) {
            if (type == CustomBiomeType.NONE) continue;

            try {
                assert forestbiome != null;
                registerCustomBiomeBase(type, dedicatedserver, registrywritable, forestbiome);
                TerraformGeneratorPlugin.logger.info("Registered custom biome: " + type.toString().toLowerCase(Locale.ENGLISH));
            } catch (Throwable e) {
                TerraformGeneratorPlugin.logger.error("Failed to register custom biome: " + type.getKey());
                TerraformGeneratorPlugin.logger.stackTrace(e);
            }
        }

        // Freeze the biome registry again
        try {
            Field frozen = RegistryMaterials.class.getDeclaredField("l");
            frozen.setAccessible(true);
            frozen.set(registrywritable, true);
            TerraformGeneratorPlugin.logger.info("Freezing biome registry");
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
            TerraformGeneratorPlugin.logger.stackTrace(e1);
        }
    }

    private static void registerCustomBiomeBase(
            @NotNull CustomBiomeType biomeType,
            DedicatedServer dedicatedserver,
            @NotNull IRegistryWritable<BiomeBase> registrywritable,
            @NotNull BiomeBase forestbiome
    ) throws Throwable {

        // Adjusted namespace to "mysticbiomes"
        ResourceKey<BiomeBase> newKey = ResourceKey.a(
                Registries.aF,
                MinecraftKey.a("mysticbiomes", biomeType.toString().toLowerCase(Locale.ENGLISH))
        );

        BiomeBase.a newBiomeBuilder = new BiomeBase.a();

        newBiomeBuilder.a(forestbiome.c()); // Precipitation
        newBiomeBuilder.a(getBiomeSettingsMobs(forestbiome));
        newBiomeBuilder.a(getBiomeSettingsGeneration(forestbiome));

        newBiomeBuilder.a(0.7F); // Temperature of biome
        newBiomeBuilder.b(biomeType.getRainFall()); // Downfall of biome

        if (biomeType.isCold()) {
            newBiomeBuilder.a(BiomeBase.TemperatureModifier.b); // Frozen biome
        } else {
            newBiomeBuilder.a(BiomeBase.TemperatureModifier.a); // Normal biome
        }

        BiomeFog.a newFog = new BiomeFog.a();
        newFog.a(GrassColor.a); // Grass color placeholder

        // Set biome colors, defaulting to forest biome colors if not specified
        setBiomeColors(newFog, biomeType, forestbiome);

        newBiomeBuilder.a(newFog.a());

        BiomeBase biome = newBiomeBuilder.a(); // Build the biome

        if (registrywritable.d(newKey)) {
            TerraformGeneratorPlugin.logger.info(newKey + " was already registered. Was there a plugin/server reload?");
            return;
        }

        Method register = registrywritable.getClass().getDeclaredMethod("a",
                net.minecraft.resources.ResourceKey.class,
                Object.class,
                Class.forName("net.minecraft.core.RegistrationInfo")
        );
        register.setAccessible(true);

        Object registrationInfo = getDefaultRegistrationInfo();
        if (!(registrationInfo instanceof net.minecraft.core.RegistrationInfo)) {
            throw new IllegalStateException("Expected RegistrationInfo but got " + registrationInfo.getClass().getName());
        }

        // Capture the raw returned object
        Object rawHolder = register.invoke(
                registrywritable,
                newKey,
                biome, registrationInfo
        );

        // Safely cast if it's indeed of the expected type
        if (rawHolder instanceof Holder.c) {
            Holder.c<BiomeBase> holder = (Holder.c<BiomeBase>) rawHolder;

            // Proceed with binding and registering the value
            Method bindValue = Holder.c.class.getDeclaredMethod("b", Object.class);
            bindValue.setAccessible(true);
            bindValue.invoke(holder, biome);

            terraformGenBiomeRegistry.put(biomeType, newKey);
        } else {
            throw new IllegalStateException("Unexpected return type: " + rawHolder.getClass().getName());
        }
    }

    private static BiomeSettingsMobs getBiomeSettingsMobs(BiomeBase forestbiome) throws NoSuchFieldException, IllegalAccessException {
        Field biomeSettingMobsField = BiomeBase.class.getDeclaredField("k");
        biomeSettingMobsField.setAccessible(true);
        return (BiomeSettingsMobs) biomeSettingMobsField.get(forestbiome);
    }

    private static BiomeSettingsGeneration getBiomeSettingsGeneration(BiomeBase forestbiome) throws NoSuchFieldException, IllegalAccessException {
        Field biomeSettingGenField = BiomeBase.class.getDeclaredField("j");
        biomeSettingGenField.setAccessible(true);
        return (BiomeSettingsGeneration) biomeSettingGenField.get(forestbiome);
    }

    private static void setBiomeColors(BiomeFog.a newFog, CustomBiomeType biomeType, BiomeBase forestbiome) {
        newFog.a(biomeType.getFogColor().isEmpty() ? forestbiome.e() : Integer.parseInt(biomeType.getFogColor(), 16));
        newFog.b(biomeType.getWaterColor().isEmpty() ? forestbiome.i() : Integer.parseInt(biomeType.getWaterColor(), 16));
        newFog.c(biomeType.getWaterFogColor().isEmpty() ? forestbiome.j() : Integer.parseInt(biomeType.getWaterFogColor(), 16));
        newFog.d(biomeType.getSkyColor().isEmpty() ? forestbiome.a() : Integer.parseInt(biomeType.getSkyColor(), 16));
        newFog.e(biomeType.getFoliageColor().isEmpty() ? forestbiome.f() : Integer.parseInt(biomeType.getFoliageColor(), 16));
        newFog.f(biomeType.getGrassColor().isEmpty() ? Integer.parseInt("79C05A", 16) : Integer.parseInt(biomeType.getGrassColor(), 16));
    }

    private static Object getDefaultRegistrationInfo() throws NoSuchFieldException, IllegalAccessException {
        Field defaultRegInfoField = ReloadableServerRegistries.class.getDeclaredField("c");
        defaultRegInfoField.setAccessible(true);
        return defaultRegInfoField.get(null);
    }

    public static Set<Holder<BiomeBase>> biomeListToBiomeBaseSet(@NotNull IRegistry<BiomeBase> registry) {
        List<Holder<BiomeBase>> biomeBases = new ArrayList<>();

        for (Biome biome : Biome.values()) {
            if (biome == null || biome == Biome.CUSTOM) {
                continue;
            }
            try {
                biomeBases.add(CraftBiome.bukkitToMinecraftHolder(biome));
            } catch (IllegalStateException e) {
                TerraformGeneratorPlugin.logger.info("Ignoring biome " + biome);
            }
        }

        for (CustomBiomeType cbt : CustomBiomeType.values()) {
            if (cbt == CustomBiomeType.NONE) {
                continue;
            }
            ResourceKey<BiomeBase> rkey = CustomBiomeHandler.terraformGenBiomeRegistry.get(cbt);
            Optional<Holder.c<BiomeBase>> holder = registry.b(rkey);
            holder.ifPresent(biomeBases::add);
        }

        return Set.copyOf(biomeBases);
    }
}
