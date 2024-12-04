package org.terraform.v1_21_R3;

import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.IRegistryWritable;
import net.minecraft.core.RegistryMaterials;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.biome.BiomeFog.GrassColor;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_21_R3.CraftServer;
import org.bukkit.craftbukkit.v1_21_R3.block.CraftBiome;
import org.jetbrains.annotations.NotNull;
import org.terraform.biome.custombiomes.CustomBiomeType;
import org.terraform.main.TerraformGeneratorPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class CustomBiomeHandler {

    public static final HashMap<CustomBiomeType, ResourceKey<BiomeBase>> terraformGenBiomeRegistry = new HashMap<>();

    public static IRegistry<BiomeBase> getBiomeRegistry()
    {
        // aI is BIOME
        // ba is registryAccess
        // a is lookup (for an optional)
        return MinecraftServer.getServer().ba().a(Registries.aI).orElseThrow();
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

        //p is createRegistrationLookup
        //b is getOrThrow
        //a is value()
        Holder<BiomeBase> forestbiome = registrywritable.p().b(Biomes.i); // forest

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
            catch (Throwable e) {
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
                                                @NotNull Holder<BiomeBase> forestBiomeHolder) throws Throwable
    {

        BiomeBase forestbiome = forestBiomeHolder.a();

        // be is reloadableRegistries()
        // a is get()
        // b is DEFAULT_REGISTRATION_INFO
        Field defaultRegInfoField = ReloadableServerRegistries.class.getDeclaredField("b");
        defaultRegInfoField.setAccessible(true);
        Object regInfo = defaultRegInfoField.get(null);

        // aI is BIOME
        ResourceKey<BiomeBase> newKey = ResourceKey.a(
                Registries.aI,
                MinecraftKey.a("terraformgenerator", biomeType.toString().toLowerCase(Locale.ENGLISH))
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

        //biomeBuilder.specialEffects(BiomeSpecialEffects$Builder.build())
        newBiomeBuilder.a(newFog.b());

        BiomeBase biome = newBiomeBuilder.a(); // biomebuilder.build();

        // Inject into the data registry for biomes
        // RegistryGeneration.a(RegistryGeneration.i, newKey, biome);

        // p is createRegistrationLookup
        // a is get. This replaced a contains() check.
        // get().b() is get().isBound(). If it is bound, its used.
        if (registrywritable.p().a(newKey).isPresent()
            && registrywritable.p().a(newKey).get().b()) {
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
        Method register = registrywritable.getClass().getDeclaredMethod("a",
                net.minecraft.resources.ResourceKey.class,
                Object.class,
                Class.forName("net.minecraft.core.RegistrationInfo")
        );
        register.setAccessible(true);
        Holder.c<BiomeBase> holder = (Holder.c<BiomeBase>) register.invoke(registrywritable, newKey, biome, regInfo);

        // Holder.Reference.bindValue
        Method bindValue = Holder.c.class.getDeclaredMethod("b", Object.class);
        bindValue.setAccessible(true);
        bindValue.invoke(holder, biome);


        //Biomes also have TagKeys (See minecraft.tags.BiomeTags)
        // Clone the plains tag keys
        //forestBiomeHolder.tags().toList()
        Set<TagKey<BiomeBase>> tags = new HashSet<TagKey<BiomeBase>>();
        forestBiomeHolder.c().forEach(tags::add);

        // Holder.Reference.bindTags
        Method bindTags = Holder.c.class.getDeclaredMethod("a",java.util.Collection.class);
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


    public static Set<Holder<BiomeBase>> biomeListToBiomeBaseSet(@NotNull IRegistry<BiomeBase> registry) {

        List<Holder<BiomeBase>> biomeBases = new ArrayList<>();

        Registry.BIOME.iterator().forEachRemaining((Biome biome)->{
            try {
                if(biome == null) return;
                Holder<BiomeBase> holder = CraftBiome.bukkitToMinecraftHolder(biome);
                if(holder == null) return;
                // Preconditions.checkArgument(biome != Biome.CUSTOM, "Cannot use the biome %s", biome);
                biomeBases.add(holder);
            }
            catch (Throwable e) {
                TerraformGeneratorPlugin.logger.info("Ignoring biome " + biome);
            }
        });

        for (CustomBiomeType cbt : CustomBiomeType.values()) {
            if (cbt == CustomBiomeType.NONE) {
                continue;
            }
            ResourceKey<BiomeBase> rkey = CustomBiomeHandler.terraformGenBiomeRegistry.get(cbt);
            // TerraformGeneratorPlugin.logger.info(cbt + " --- " + rkey);
            // Holder.c is Holder.Reference. It implements Holder. No idk why.
            Optional<Holder.c<BiomeBase>> holder = registry.a(rkey);
            holder.ifPresent(biomeBases::add);
        }

        return Set.copyOf(biomeBases);
    }
}
