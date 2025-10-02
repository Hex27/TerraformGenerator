package org.terraform.v1_21_R6;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.MappedRegistry;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.GrassColor;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.main.TerraformGeneratorPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class CustomBiomeHandler {

    public static final HashMap<CustomBiomeType, ResourceKey<Biome>> terraformGenBiomeRegistry = new HashMap<>();

    public static Registry<Biome> getBiomeRegistry()
    {
        // aN is BIOME
        // bg is registryAccess
        // a is lookup (for an optional)
        return MinecraftServer.getServer().registryAccess().lookup(Registries.BIOME).orElseThrow();
    }

    public static void init() {
        CraftServer craftserver = (CraftServer) Bukkit.getServer();
        DedicatedServer dedicatedserver = craftserver.getServer();
        WritableRegistry<Biome> registrywritable = (WritableRegistry<Biome>) getBiomeRegistry();

        // This thing isn't actually writable, so we have to forcefully UNFREEZE IT
        // l is frozen
        try {
            Field frozen = MappedRegistry.class.getDeclaredField("l");
            frozen.setAccessible(true);
            frozen.set(registrywritable, false);
            TerraformGeneratorPlugin.logger.info("Unfreezing biome registry...");
        }
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
            TerraformGeneratorPlugin.logger.stackTrace(e1);
        }

        //p is createRegistrationLookup
        //b is getOrThrow
        //a is value()
        Holder<Biome> forestbiome = registrywritable.createRegistrationLookup().getOrThrow(Biomes.FOREST); // forest

        for (CustomBiomeType type : CustomBiomeType.values()) {
            if (type == CustomBiomeType.NONE) {
                continue;
            }

            try {
                assert forestbiome != null;
                registerCustomBiome(type, dedicatedserver, registrywritable, forestbiome);
                TerraformGeneratorPlugin.logger.info("Registered custom biome: " + type.toString()
                                                                                       .toLowerCase(Locale.ENGLISH));
            }
            catch (Throwable e) {
                TerraformGeneratorPlugin.logger.error("Failed to register custom biome: " + type.getKey());
                TerraformGeneratorPlugin.logger.stackTrace(e);
            }
        }

        try {
            Field frozen = MappedRegistry.class.getDeclaredField("l");
            frozen.setAccessible(true);
            frozen.set(registrywritable, true);
            TerraformGeneratorPlugin.logger.info("Freezing biome registry");
        }
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
            TerraformGeneratorPlugin.logger.stackTrace(e1);
        }

    }

    private static void registerCustomBiome(@NotNull CustomBiomeType biomeType,
                                                DedicatedServer dedicatedserver,
                                                @NotNull WritableRegistry<Biome> registrywritable,
                                                @NotNull Holder<Biome> forestBiomeHolder) throws Throwable
    {

        Biome forestbiome = forestBiomeHolder.value();

        // b is DEFAULT_REGISTRATION_INFO
        Field defaultRegInfoField = ReloadableServerRegistries.class.getDeclaredField("b");
        defaultRegInfoField.setAccessible(true);
        Object regInfo = defaultRegInfoField.get(null);

        // aN is BIOME
        ResourceKey<Biome> newKey = ResourceKey.create(
                Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath("terraformgenerator", biomeType.toString().toLowerCase(Locale.ENGLISH))
        );

        // Biome.a is BiomeBuilder
        Biome.BiomeBuilder newBiomeBuilder = new Biome.BiomeBuilder();

        // Biome.b is ClimateSettings
        // d is temperatureModifier
        // This temperature modifier stuff is more cleanly handled below.
        //		Class<?> climateSettingsClass = Class.forName("net.minecraft.world.level.biome.Biome.b");
        //		Field temperatureModififierField = climateSettingsClass.getDeclaredField("d");
        //		temperatureModififierField.setAccessible(true);

        // i is climateSettings
        newBiomeBuilder.hasPrecipitation(forestbiome.hasPrecipitation()); // c is hasPrecipitation

        // k is mobSettings
        Field biomeSettingMobsField = Biome.class.getDeclaredField("k");
        biomeSettingMobsField.setAccessible(true);
        MobSpawnSettings biomeSettingMobs = (MobSpawnSettings) biomeSettingMobsField.get(forestbiome);
        newBiomeBuilder.mobSpawnSettings(biomeSettingMobs);

        // j is generationSettings
        Field biomeSettingGenField = Biome.class.getDeclaredField("j");
        biomeSettingGenField.setAccessible(true);
        BiomeGenerationSettings biomeSettingGen = (BiomeGenerationSettings) biomeSettingGenField.get(forestbiome);
        newBiomeBuilder.generationSettings(biomeSettingGen);

        newBiomeBuilder.temperature(0.7F); // Temperature of biome
        newBiomeBuilder.downfall(biomeType.getRainFall()); // Downfall of biome

        // Biome.TemperatureModifier.a will make your biome normal
        // Biome.TemperatureModifier.b will make your biome frozen
        if (biomeType.isCold()) {
            newBiomeBuilder.temperatureAdjustment(Biome.TemperatureModifier.FROZEN);
        }
        else {
            newBiomeBuilder.temperatureAdjustment(Biome.TemperatureModifier.NONE);
        }

        //newFog is BiomeSpecialEffects.Builder
        BiomeSpecialEffects.Builder newFog = new BiomeSpecialEffects.Builder();
        newFog
            //idk what this does
            //.grassColorOverride(GrassColor.getDefaultColor())
            //fogColor
            .fogColor(biomeType.getFogColor().isEmpty()
               ? forestbiome.getFogColor()
               : Integer.parseInt(biomeType.getFogColor(), 16))
            //waterColor
            .waterColor(biomeType.getWaterColor().isEmpty()
               ? forestbiome.getWaterColor()
               : Integer.parseInt(biomeType.getWaterColor(), 16))
            //waterFogColor
            .waterFogColor(biomeType.getWaterFogColor().isEmpty()
                 ? forestbiome.getWaterFogColor()
                 : Integer.parseInt(biomeType.getWaterFogColor(), 16))
            //skyColor
            .skyColor(biomeType.getSkyColor().isEmpty()
               ? forestbiome.getSkyColor()
               : Integer.parseInt(biomeType.getSkyColor(), 16))
            //foliageColor
            .foliageColorOverride(biomeType.getFoliageColor().isEmpty()
                 ? forestbiome.getFoliageColor()
                 : Integer.parseInt(biomeType.getFoliageColor(), 16))
            //grassColorOverride
            .grassColorOverride(biomeType.getGrassColor().isEmpty()
                 ? Integer.parseInt("79C05A", 16)
                 : Integer.parseInt(biomeType.getGrassColor(), 16));

        //biomeBuilder.specialEffects(BiomeSpecialEffects$Builder.build())
        newBiomeBuilder.specialEffects(newFog.build());

        Biome biome = newBiomeBuilder.build(); // biomebuilder.build();

        // Inject into the data registry for biomes
        // RegistryGeneration.a(RegistryGeneration.i, newKey, biome);

        // p is createRegistrationLookup
        // a is get. This replaced a contains() check.
        // get().b() is get().isBound(). If it is bound, its used.
        if (registrywritable.createRegistrationLookup().get(newKey).isPresent()
            && registrywritable.createRegistrationLookup().get(newKey).get().isBound()) {
            TerraformGeneratorPlugin.logger.info(newKey + " was already registered. Was there a plugin/server reload?");
            return;
        }

        // Inject into the biome registry
        // al is BIOMES
        // aW is registryAccess
        // d is registryOrThrow
        // MappedRegistry<Biome> registry = (MappedRegistry<Biome>) getBiomeRegistry();

        // Inject unregisteredIntrusiveHolders with a new map to allow intrusive holders
        // m is unregisteredIntrusiveHolders
        // Field unregisteredIntrusiveHolders = MappedRegistry.class.getDeclaredField("m");
        // unregisteredIntrusiveHolders.setAccessible(true);
        // unregisteredIntrusiveHolders.set(registrywritable, new IdentityHashMap<>());

        // f is createIntrusiveHolder
        // registrywritable.f(biome);

        // a is MappedRegistry.register
        // Holder.c is Holder.Reference
        Method register = registrywritable.getClass().getDeclaredMethod("a",
                net.minecraft.resources.ResourceKey.class,
                Object.class,
                Class.forName("net.minecraft.core.RegistrationInfo")
        );
        register.setAccessible(true);
        Holder.Reference<Biome> holder = (Holder.Reference<Biome>) register.invoke(registrywritable, newKey, biome, regInfo);

        // Holder.Reference.bindValue
        Method bindValue = Holder.Reference.class.getDeclaredMethod("b", Object.class);
        bindValue.setAccessible(true);
        bindValue.invoke(holder, biome);


        //Biomes also have TagKeys (See minecraft.tags.BiomeTags)
        // Clone the plains tag keys
        //forestBiomeHolder.tags().toList()
        Set<TagKey<Biome>> tags = new HashSet<TagKey<Biome>>();
        forestBiomeHolder.tags().forEach(tags::add);

        // Holder.Reference.bindTags
        Method bindTags = Holder.Reference.class.getDeclaredMethod("a",java.util.Collection.class);
        bindTags.setAccessible(true);
        bindTags.invoke(holder, tags);


        // what the fuck is happening here 23/4/2024

        // Make unregisteredIntrusiveHolders null again to remove potential for undefined behaviour
        // unregisteredIntrusiveHolders.set(registrywritable, null);

        // There is a slightly cleaner way this can be done (void bindValue(T value)
        // instead of the whole unregistered intrusive holders stuff),
        // but it also involves reflection so I don't want to
        // change this out just yet. Consider for the next version.
        terraformGenBiomeRegistry.put(biomeType, newKey);

    }


    public static Set<Holder<Biome>> biomeListToBiomeSet(@NotNull Registry<Biome> registry) {

        List<Holder<Biome>> Biomes = new ArrayList<>();

        org.bukkit.Registry.BIOME.iterator().forEachRemaining((org.bukkit.block.Biome biome)->{
            try {
                if(biome == null) return;
                Holder<Biome> holder = CraftBiome.bukkitToMinecraftHolder(biome);
                if(holder == null) return;
                // Preconditions.checkArgument(biome != Biome.CUSTOM, "Cannot use the biome %s", biome);
                Biomes.add(holder);
            }
            catch (Throwable e) {
                TerraformGeneratorPlugin.logger.info("Ignoring biome " + biome);
            }
        });

        for (CustomBiomeType cbt : CustomBiomeType.values()) {
            if (cbt == CustomBiomeType.NONE) {
                continue;
            }
            ResourceKey<Biome> rkey = CustomBiomeHandler.terraformGenBiomeRegistry.get(cbt);
            // TerraformGeneratorPlugin.logger.info(cbt + " --- " + rkey);
            // Holder.c is Holder.Reference. It implements Holder. No idk why.
            Optional<Holder.Reference<Biome>> holder = registry.get(rkey);
            holder.ifPresent(Biomes::add);
        }

        return Set.copyOf(Biomes);
    }
}
