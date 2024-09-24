package org.terraform.v1_18_R2;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryWritable;
import net.minecraft.core.RegistryMaterials;
import net.minecraft.data.RegistryGeneration;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.biome.BiomeFog.GrassColor;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.main.TerraformGeneratorPlugin;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;

public class CustomBiomeHandler {

    public static final HashMap<CustomBiomeType, ResourceKey<BiomeBase>> terraformGenBiomeRegistry = new HashMap<>();

    public static void init() {
        CraftServer craftserver = (CraftServer) Bukkit.getServer();
        DedicatedServer dedicatedserver = craftserver.getServer();

        // aP is BIOME_REGISTRY
        // aU is registryAccess
        // b is ownedRegistryOrThrow
        IRegistryWritable<BiomeBase> registrywritable = (IRegistryWritable<BiomeBase>) dedicatedserver.aU()
                                                                                                      .b(IRegistry.aP);

        // This thing isn't actually writable, so we have to forcefully UNFREEZE IT
        try {
            Field frozen = RegistryMaterials.class.getDeclaredField("bL");
            frozen.setAccessible(true);
            frozen.set(registrywritable, false);
            TerraformGeneratorPlugin.logger.info("Unfreezing biome registry...");
        }
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
            TerraformGeneratorPlugin.logger.stackTrace(e1);
        }

        BiomeBase forestbiome = registrywritable.a(Biomes.h); // forest

        for (CustomBiomeType type : CustomBiomeType.values()) {
            if (type == CustomBiomeType.NONE) {
                continue;
            }

            try {
                registerCustomBiomeBase(type, dedicatedserver, registrywritable, forestbiome);
                TerraformGeneratorPlugin.logger.info("Registered custom biome: " + type.toString()
                                                                                       .toLowerCase(Locale.ENGLISH));
            }
            catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                TerraformGeneratorPlugin.logger.error("Failed to register custom biome: " + type.getKey());
                TerraformGeneratorPlugin.logger.stackTrace(e);
            }
        }

        try {
            Field frozen = RegistryMaterials.class.getDeclaredField("bL");
            frozen.setAccessible(true);
            frozen.set(registrywritable, true);
            TerraformGeneratorPlugin.logger.info("Freezing biome registry");
        }
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
            TerraformGeneratorPlugin.logger.stackTrace(e1);
        }

    }

    private static void registerCustomBiomeBase(@NotNull CustomBiomeType biomeType,
                                                @NotNull DedicatedServer dedicatedserver,
                                                IRegistryWritable<BiomeBase> registrywritable,
                                                @NotNull BiomeBase forestbiome)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
    {

        ResourceKey<BiomeBase> newKey = ResourceKey.a(
                IRegistry.aP,
                new MinecraftKey("terraformgenerator", biomeType.toString().toLowerCase(Locale.ENGLISH))
        );

        // BiomeBase.a is BiomeBuilder
        BiomeBase.a newBiomeBuilder = new BiomeBase.a();

        // l is biomeCategory
        Field f = BiomeBase.class.getDeclaredField("l");
        f.setAccessible(true);
        newBiomeBuilder.a((BiomeBase.Geography) f.get(forestbiome));
        newBiomeBuilder.a(forestbiome.c()); // c is getPrecipitation

        Field biomeSettingMobsField = BiomeBase.class.getDeclaredField("k");
        biomeSettingMobsField.setAccessible(true);
        BiomeSettingsMobs biomeSettingMobs = (BiomeSettingsMobs) biomeSettingMobsField.get(forestbiome);
        newBiomeBuilder.a(biomeSettingMobs);

        Field biomeSettingGenField = BiomeBase.class.getDeclaredField("j");
        biomeSettingGenField.setAccessible(true);
        BiomeSettingsGeneration biomeSettingGen = (BiomeSettingsGeneration) biomeSettingGenField.get(forestbiome);
        newBiomeBuilder.a(biomeSettingGen);

        newBiomeBuilder.a(0.7F); // Temperature of biome
        newBiomeBuilder.b(biomeType.getRainFall()); // Downfall of biome

        // BiomeBase.TemperatureModifier.a will make your biome normal
        // BiomeBase.TemperatureModifier.b will make your biome frozen
        if (biomeType.isCold()) {
            newBiomeBuilder.a(BiomeBase.TemperatureModifier.b);
        }
        else {
            newBiomeBuilder.a(BiomeBase.TemperatureModifier.a);
        }

        BiomeFog.a newFog = new BiomeFog.a();
        newFog.a(GrassColor.a); // This doesn't affect the actual final grass color, just leave this line as it is or you will get errors

        // Set biome colours. If field is empty, default to forest color

        // fogcolor
        newFog.a(biomeType.getFogColor().isEmpty() ? forestbiome.f() : Integer.parseInt(biomeType.getFogColor(), 16));

        // water color
        newFog.b(biomeType.getWaterColor().isEmpty()
                 ? forestbiome.k()
                 : Integer.parseInt(biomeType.getWaterColor(), 16));

        // water fog color
        newFog.c(biomeType.getWaterFogColor().isEmpty()
                 ? forestbiome.l()
                 : Integer.parseInt(biomeType.getWaterFogColor(), 16));

        // sky color
        newFog.d(biomeType.getSkyColor().isEmpty() ? forestbiome.a() : Integer.parseInt(biomeType.getSkyColor(), 16));

        // Unnecessary values; can be removed safely if you don't want to change them

        // foliage color (leaves, fines and more)

        newFog.e(biomeType.getFoliageColor().isEmpty()
                 ? forestbiome.g()
                 : Integer.parseInt(biomeType.getFoliageColor(), 16));

        // grass blocks color
        newFog.f(biomeType.getGrassColor().isEmpty()
                 ? Integer.parseInt("79C05A", 16)
                 : Integer.parseInt(biomeType.getGrassColor(), 16));


        newBiomeBuilder.a(newFog.a());

        BiomeBase biome = newBiomeBuilder.a(); // biomebuilder.build();

        // Inject into the data registry for biomes
        RegistryGeneration.a(RegistryGeneration.i, newKey, biome);

        // Inject into the biome registry
        // aP is BIOME_REGISTRY
        // aU is registryAccess
        // b is ownedRegistryOrThrow
        RegistryMaterials<BiomeBase> registry = ((RegistryMaterials<BiomeBase>) dedicatedserver.aU().b(IRegistry.aP));

        // a is ownedRegistryOrThrow
        registry.a(newKey, biome, Lifecycle.stable());

        terraformGenBiomeRegistry.put(biomeType, newKey);

    }
}
