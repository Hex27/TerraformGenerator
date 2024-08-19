package org.terraform.v1_20_R4;

import net.minecraft.core.*;
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
import org.bukkit.craftbukkit.v1_20_R4.CraftServer;
import org.bukkit.craftbukkit.v1_20_R4.block.CraftBiome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.main.TerraformGeneratorPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class CustomBiomeHandler {

    public static final HashMap<CustomBiomeType, ResourceKey<BiomeBase>> terraformGenBiomeRegistry = new HashMap<>();

    public static IRegistry<BiomeBase> getBiomeRegistry()
    {
        // az is BIOME
        // bc is registryAccess
        // d is registryOrThrow
        return MinecraftServer.getServer().bc().d(Registries.az);
    }

    public static void init() {
        CraftServer craftserver = (CraftServer) Bukkit.getServer();
        DedicatedServer dedicatedserver = craftserver.getServer();
        IRegistryWritable<BiomeBase> registrywritable = (IRegistryWritable<BiomeBase>) getBiomeRegistry();

        // This thing isn't actually writable, so we have to forcefully UNFREEZE IT
        // l is frozen
        try {
            Field frozen = RegistryMaterials.class.getDeclaredField("l");
            frozen.setAccessible(true);
            frozen.set(registrywritable, false);
            TerraformGeneratorPlugin.logger.info("Unfreezing biome registry...");
        }
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
            TerraformGeneratorPlugin.logger.stackTrace(e1);
        }

        BiomeBase forestbiome = registrywritable.a(Biomes.i); // forest

        for (CustomBiomeType type : CustomBiomeType.values()) {
            if (type == CustomBiomeType.NONE) {
                continue;
            }

            try {
                assert forestbiome != null;
                registerCustomBiomeBase(type, dedicatedserver, registrywritable, forestbiome);
                TerraformGeneratorPlugin.logger.info("Registered custom biome: " + type.toString()
                                                                                       .toLowerCase(Locale.ENGLISH));
            }
            catch (NoSuchFieldException |
                   SecurityException |
                   IllegalArgumentException |
                   IllegalAccessException |
                   NoSuchMethodException |
                   InvocationTargetException e) {
                TerraformGeneratorPlugin.logger.error("Failed to register custom biome: " + type.getKey());
                TerraformGeneratorPlugin.logger.stackTrace(e);
            }
        }

        try {
            Field frozen = RegistryMaterials.class.getDeclaredField("l");
            frozen.setAccessible(true);
            frozen.set(registrywritable, true);
            TerraformGeneratorPlugin.logger.info("Freezing biome registry");
        }
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
            TerraformGeneratorPlugin.logger.stackTrace(e1);
        }

    }

    private static void registerCustomBiomeBase(@NotNull CustomBiomeType biomeType,
                                                DedicatedServer dedicatedserver,
                                                @NotNull IRegistryWritable<BiomeBase> registrywritable,
                                                @NotNull BiomeBase forestbiome) throws
            NoSuchFieldException,
            SecurityException,
            IllegalArgumentException,
            IllegalAccessException,
            NoSuchMethodException,
            InvocationTargetException
    {

        // be is reloadableRegistries()
        // a is get()
        // c is DEFAULT_REGISTRATION_INFO
        Field defaultRegInfoField = ReloadableServerRegistries.class.getDeclaredField("c");
        defaultRegInfoField.setAccessible(true);
        RegistrationInfo regInfo = (RegistrationInfo) defaultRegInfoField.get(null);

        // az is BIOME
        ResourceKey<BiomeBase> newKey = ResourceKey.a(
                Registries.az,
                new MinecraftKey("terraformgenerator", biomeType.toString().toLowerCase(Locale.ENGLISH))
        );

        // BiomeBase.a is BiomeBuilder
        BiomeBase.a newBiomeBuilder = new BiomeBase.a();

        // BiomeBase.b is ClimateSettings
        // d is temperatureModifier
        // This temperature modifier stuff is more cleanly handled below.
        //		Class<?> climateSettingsClass = Class.forName("net.minecraft.world.level.biome.BiomeBase.b");
        //		Field temperatureModififierField = climateSettingsClass.getDeclaredField("d");
        //		temperatureModififierField.setAccessible(true);

        // i is climateSettings
        newBiomeBuilder.a(forestbiome.c()); // c is getPrecipitation

        // k is mobSettings
        Field biomeSettingMobsField = BiomeBase.class.getDeclaredField("k");
        biomeSettingMobsField.setAccessible(true);
        BiomeSettingsMobs biomeSettingMobs = (BiomeSettingsMobs) biomeSettingMobsField.get(forestbiome);
        newBiomeBuilder.a(biomeSettingMobs);

        // j is generationSettings
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
        newFog.a(biomeType.getFogColor().isEmpty() ? forestbiome.e() : Integer.parseInt(biomeType.getFogColor(), 16));

        // water color i is getWaterColor
        newFog.b(biomeType.getWaterColor().isEmpty()
                 ? forestbiome.i()
                 : Integer.parseInt(biomeType.getWaterColor(), 16));

        // water fog color j is getWaterFogColor
        newFog.c(biomeType.getWaterFogColor().isEmpty()
                 ? forestbiome.j()
                 : Integer.parseInt(biomeType.getWaterFogColor(), 16));

        // sky color
        newFog.d(biomeType.getSkyColor().isEmpty() ? forestbiome.a() : Integer.parseInt(biomeType.getSkyColor(), 16));


        // Unnecessary values; can be removed safely if you don't want to change them

        // foliage color (leaves, fines and more) f is getFoliageColor
        newFog.e(biomeType.getFoliageColor().isEmpty()
                 ? forestbiome.f()
                 : Integer.parseInt(biomeType.getFoliageColor(), 16));

        // grass blocks color
        newFog.f(biomeType.getGrassColor().isEmpty()
                 ? Integer.parseInt("79C05A", 16)
                 : Integer.parseInt(biomeType.getGrassColor(), 16));


        newBiomeBuilder.a(newFog.a());

        BiomeBase biome = newBiomeBuilder.a(); // biomebuilder.build();

        // Inject into the data registry for biomes
        // RegistryGeneration.a(RegistryGeneration.i, newKey, biome);

        // d is containsKey
        if (registrywritable.d(newKey)) {
            TerraformGeneratorPlugin.logger.info(newKey + " was already registered. Was there a plugin/server reload?");
            return;
        }

        // Inject into the biome registry
        // al is BIOMES
        // aW is registryAccess
        // d is registryOrThrow
        // RegistryMaterials<BiomeBase> registry = (RegistryMaterials<BiomeBase>) getBiomeRegistry();

        // Inject unregisteredIntrusiveHolders with a new map to allow intrusive holders
        // m is unregisteredIntrusiveHolders
        // Field unregisteredIntrusiveHolders = RegistryMaterials.class.getDeclaredField("m");
        // unregisteredIntrusiveHolders.setAccessible(true);
        // unregisteredIntrusiveHolders.set(registrywritable, new IdentityHashMap<>());

        // f is createIntrusiveHolder
        // registrywritable.f(biome);

        // a is RegistryMaterials.register
        // Holder.c is Holder.Reference
        Holder.c<BiomeBase> holder = registrywritable.a(newKey, biome, regInfo);

        // Holder.Reference.bindValue
        Method bindValue = Holder.c.class.getDeclaredMethod("b", Object.class);
        bindValue.setAccessible(true);
        bindValue.invoke(holder, biome);

        // what the fuck is happening here 23/4/2024

        // Make unregisteredIntrusiveHolders null again to remove potential for undefined behaviour
        // unregisteredIntrusiveHolders.set(registrywritable, null);

        // There is a slightly cleaner way this can be done (void bindValue(T value)
        // instead of the whole unregistered intrusive holders stuff),
        // but it also involves reflection so I don't want to
        // change this out just yet. Consider for the next version.
        terraformGenBiomeRegistry.put(biomeType, newKey);

    }


    public static Set<Holder<BiomeBase>> biomeListToBiomeBaseSet(@NotNull IRegistry<BiomeBase> registry) {

        List<Holder<BiomeBase>> biomeBases = new ArrayList<>();

        for (Biome biome : Biome.values()) {
            if (biome == null || biome == Biome.CUSTOM) {
                continue;
            }
            try {
                // Preconditions.checkArgument(biome != Biome.CUSTOM, "Cannot use the biome %s", biome);
                biomeBases.add(CraftBiome.bukkitToMinecraftHolder(biome));
            }
            catch (IllegalStateException e) {
                TerraformGeneratorPlugin.logger.info("Ignoring biome " + biome);
            }
        }

        for (CustomBiomeType cbt : CustomBiomeType.values()) {
            if (cbt == CustomBiomeType.NONE) {
                continue;
            }
            ResourceKey<BiomeBase> rkey = CustomBiomeHandler.terraformGenBiomeRegistry.get(cbt);
            // TerraformGeneratorPlugin.logger.info(cbt + " --- " + rkey);
            // Holder.c is Holder.Reference. It implements Holder. No idk why.
            Optional<Holder.c<BiomeBase>> holder = registry.b(rkey);
            holder.ifPresent(biomeBases::add);
        }

        return Set.copyOf(biomeBases);
    }
}
