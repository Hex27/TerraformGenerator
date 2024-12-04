package org.terraform.v1_21_R3;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.RegionLimitedWorldAccess;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.*;
import net.minecraft.world.level.BlockColumn;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.structures.BuriedTreasureStructure;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentStructure;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdStructure;
import net.minecraft.world.level.levelgen.structure.structures.WoodlandMansionStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.MegaChunk;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.main.config.TConfig;
import org.terraform.structure.SingleMegaChunkStructurePopulator;
import org.terraform.structure.StructureLocator;
import org.terraform.structure.StructurePopulator;
import org.terraform.structure.StructureRegistry;
import org.terraform.structure.VanillaStructurePopulator;
import org.terraform.structure.monument.MonumentPopulator;
import org.terraform.structure.pillager.mansion.MansionPopulator;
import org.terraform.structure.small.buriedtreasure.BuriedTreasurePopulator;
import org.terraform.structure.stronghold.StrongholdPopulator;
import org.terraform.structure.trialchamber.TrialChamberPopulator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class NMSChunkGenerator extends ChunkGenerator {
    private final @NotNull ChunkGenerator delegate;
    private final @NotNull TerraformWorld tw;
    private final @NotNull MapRenderWorldProviderBiome mapRendererBS;
    private final @NotNull TerraformWorldProviderBiome twBS;
    private final @NotNull Method tryGenerateStructure;
    private final ArrayList<MinecraftKey> possibleStructureSets = new ArrayList<>();

    private final @NotNull Method getWriteableArea;
    private final @NotNull Supplier featuresPerStep;

    public NMSChunkGenerator(String worldName, long seed, @NotNull ChunkGenerator delegate)
            throws NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalAccessException
    {
        super(
                delegate.d(), // WorldChunkManager d() is getBiomeSource()
                delegate.d); // Idk what generationSettingsGetter is
        tw = TerraformWorld.get(worldName, seed);
        this.delegate = delegate;

        // Set the long term biome handler to this one. The normal behaving one
        // is initiated inside the cave carver
        mapRendererBS = new MapRenderWorldProviderBiome(tw, delegate.d());
        twBS = new TerraformWorldProviderBiome(TerraformWorld.get(worldName, seed), delegate.d());

        //This is needed for addVanillaFeatures
        Field f = ChunkGenerator.class.getDeclaredField("c");
        f.setAccessible(true);
        featuresPerStep = (Supplier) f.get(delegate);

        getWriteableArea = ChunkGenerator.class.getDeclaredMethod("a", IChunkAccess.class);
        getWriteableArea.setAccessible(true);

        // This is tryGenerateStructure
        // Register VanillaStructurePopulators to allow Minecraft to properly
        // handle them
        for(StructurePopulator pop : StructureRegistry.getAllPopulators())
        {
            if(pop instanceof VanillaStructurePopulator vsp)
            {
                possibleStructureSets.add(MinecraftKey.a(vsp.structureRegistryKey)); // MinecraftKey.create
            }
        }
        tryGenerateStructure = ChunkGenerator.class.getDeclaredMethod("a",
                StructureSet.a.class,
                StructureManager.class,
                IRegistryCustom.class,
                RandomState.class,
                StructureTemplateManager.class,
                long.class,
                IChunkAccess.class,
                ChunkCoordIntPair.class,
                SectionPosition.class,
                ResourceKey.class);
        tryGenerateStructure.setAccessible(true);
    }


    @Override // getBiomeSource
    public @NotNull WorldChunkManager d() {
        return mapRendererBS;
    }

    public @NotNull TerraformWorld getTerraformWorld() {
        return tw;
    }

    @Override
    protected @NotNull MapCodec<? extends ChunkGenerator> b() {
        return MapCodec.unit(null);
    }

    @Override // createBiomes
    public @NotNull CompletableFuture<IChunkAccess> a(RandomState randomstate, Blender blender, StructureManager structuremanager, @NotNull IChunkAccess ichunkaccess)
    {
        return CompletableFuture.supplyAsync(() -> {
            return ichunkaccess; // Don't do any calculations here, biomes are set in applyCarvers
        }, SystemUtils.h().a("init_biomes"));
        //SystemUtils.backgroundExecutor().
    }

    @Override // findNearestMapFeature
    public Pair<BlockPosition, Holder<Structure>> a(WorldServer worldserver, @NotNull HolderSet<Structure> holderset,
                                                    @NotNull BlockPosition blockposition, int i, boolean flag) {

        int pX = blockposition.u(); // getX
        int pZ = blockposition.w(); // getZ

        for(Holder<Structure> holder:holderset) {
            Structure feature = holder.a();
            // StructureGenerator<?> structuregenerator = feature.;
            TerraformGeneratorPlugin.logger.info("Vanilla locate for " + feature.getClass().getName() + " invoked.");

            if (holder.a().getClass() == StrongholdStructure.class) { // stronghold
                int[] coords = new StrongholdPopulator().getNearestFeature(tw, pX, pZ);
                return new Pair<>(new BlockPosition(coords[0], 20, coords[1]), holder);
            }
            else if(!TConfig.c.DEVSTUFF_VANILLA_LOCATE_DISABLE)
            {
                if (holder.a().getClass() == OceanMonumentStructure.class) { // Monument

                    int[] coords = StructureLocator.locateSingleMegaChunkStructure(tw, pX, pZ, new MonumentPopulator(), TConfig.c.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS);

                    return new Pair<>
                            (new BlockPosition(coords[0], 50, coords[1]), holder);
                } else if (holder.a().getClass() == WoodlandMansionStructure.class) { // Mansion

                    int[] coords = StructureLocator.locateSingleMegaChunkStructure(tw, pX, pZ, new MansionPopulator(), TConfig.c.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS);

                    return new Pair<>
                            (new BlockPosition(coords[0], 50, coords[1]), holder);
                } else if (holder.a() instanceof JigsawStructure
                           //aU is structure
                        && MinecraftServer.getServer().ba().a(Registries.aU).orElseThrow().a(MinecraftKey.a("trial_chambers")) == holder.a()
                ) { // Trial Chamber

                    int[] coords = StructureLocator.locateSingleMegaChunkStructure(tw, pX, pZ, new TrialChamberPopulator(), TConfig.c.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS);

                    return new Pair<>
                            (new BlockPosition(coords[0], 50, coords[1]), holder);
                } else if (holder.a().getClass() == BuriedTreasureStructure.class) {
                    // Buried Treasure
                    int[] coords = StructureLocator.locateMultiMegaChunkStructure(tw, new MegaChunk(pX, 0, pZ), new BuriedTreasurePopulator(), TConfig.c.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS);
                    if(coords == null) return null;
                    return new Pair<>
                            (new BlockPosition(coords[0], 50, coords[1]), holder);
                }
            }
        }
        return null;
    }

    @Override // applyBiomeDecoration
    public void a(GeneratorAccessSeed generatoraccessseed, IChunkAccess ichunkaccess, StructureManager structuremanager) {
        delegate.a(generatoraccessseed, ichunkaccess, structuremanager);

        // This triggers structure gen. Needed for VanillaStructurePopulator
        addVanillaDecorations(generatoraccessseed,ichunkaccess, structuremanager);
    }

    //This has to be overridden because calling the normal one will make vanilla
    // generate ores. The giant commented swath of stuff did it
    @Override
    public void addVanillaDecorations(GeneratorAccessSeed generatoraccessseed, IChunkAccess ichunkaccess, StructureManager structuremanager) { // CraftBukkit
        ChunkCoordIntPair chunkcoordintpair = ichunkaccess.f(); //getPos

        //debugVoidTerrain
        if (!SharedConstants.a(chunkcoordintpair)) {
            //SectionPosition.of(...,generatoraccessseed.getMinSection())
            SectionPosition sectionposition = SectionPosition.a(chunkcoordintpair, generatoraccessseed.ao());
            BlockPosition blockposition = sectionposition.j(); //origin()

            //generatoraccessseed.registryAccess().lookupOrThrow(Registries.STRUCTURE);
            IRegistry<Structure> iregistry = generatoraccessseed.K_().e(Registries.aU);

            //iregistry.stream()
            Map<Integer, List<Structure>> map = (Map) iregistry.s().collect(Collectors.groupingBy((structure) -> {
                //structure.step()
                return structure.c().ordinal();
            }));
            List<FeatureSorter.b> list = (List) featuresPerStep.get(); //this.featuresPerStep
            SeededRandom seededrandom = new SeededRandom(new XoroshiroRandomSource(RandomSupport.a())); //generateUniqueSeed
            //seededrandom.setDecorationSeed(generatoraccessseed.getSeed(), blockposition.getX(), blockposition.getZ())
            long i = seededrandom.a(generatoraccessseed.E(), blockposition.u(), blockposition.w());
            Set<Holder<BiomeBase>> set = new ObjectArraySet();
//            ChunkCoordIntPair.rangeClosed(sectionposition.chunk(),...
            ChunkCoordIntPair.a(sectionposition.r(), 1).forEach((chunkcoordintpair1) -> {
//              IChunkAccess ichunkaccess1 = generatoraccessseed.getChunk(chunkcoordintpair1.x, chunkcoordintpair1.z);
                IChunkAccess ichunkaccess1 = generatoraccessseed.a(chunkcoordintpair1.h, chunkcoordintpair1.i);
                ChunkSection[] achunksection = ichunkaccess1.d(); //getSections
                int j = achunksection.length;

                for (int k = 0; k < j; ++k) {
                    ChunkSection chunksection = achunksection[k];
                    //getBiomes
                    PalettedContainerRO<Holder<BiomeBase>> palettedcontainerro = chunksection.i(); // CraftBukkit - decompile error

                    Objects.requireNonNull(set);
                    palettedcontainerro.a(set::add); //getAll
                }

            });
            //set.retainAll(this.biomeSource.possibleBiomes());
            set.retainAll(this.b.c());
            int j = list.size();

            try {
                //generatoraccessseed.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
                IRegistry<PlacedFeature> iregistry1 = generatoraccessseed.K_().e(Registries.aT);
                int k = Math.max(WorldGenStage.Decoration.values().length, j);

                for (int l = 0; l < k; ++l) {
                    int i1 = 0;
                    Iterator iterator;
                    CrashReportSystemDetails crashreportsystemdetails;

                    if (structuremanager.a()) { //shouldGenerateStructures
                        List<Structure> list1 = (List) map.getOrDefault(l, Collections.emptyList());

                        for (iterator = list1.iterator(); iterator.hasNext(); ++i1) {
                            Structure structure = (Structure) iterator.next();

                            seededrandom.b(i, i1, l); //setFeatureSeed
                            Supplier<String> supplier = () -> {
                                //getResourceKey
                                Optional optional = iregistry.d(structure).map(Object::toString);

                                Objects.requireNonNull(structure);
                                return (String) optional.orElseGet(structure::toString);
                            };

                            try {
                                //setCurrentlyGenerating
                                generatoraccessseed.a(supplier);
                                //startsForStructure
                                structuremanager.a(sectionposition, structure).forEach((structurestart) -> {
                                    //placeInChunk(...getWritableArea...)
                                    try{
                                        structurestart.a(generatoraccessseed, structuremanager, this,
                                                seededrandom, (StructureBoundingBox) getWriteableArea.invoke(null,ichunkaccess),
                                                chunkcoordintpair);
                                    }catch(IllegalAccessException | InvocationTargetException e){
                                        CrashReport crashreport = CrashReport.a(e, "TerraformGenerator");
                                        throw new ReportedException(crashreport);
                                    }
                                });
                            } catch (Exception exception) {
                                //forThrowable
                                CrashReport crashreport = CrashReport.a(exception, "Feature placement");

                                crashreportsystemdetails = crashreport.a("Feature"); //addCategory
                                Objects.requireNonNull(supplier);
                                crashreportsystemdetails.a("Description", supplier::get); //setDetail
                                throw new ReportedException(crashreport);
                            }
                        }
                    }

                    /*
                    if (l < j) {
                        IntArraySet intarrayset = new IntArraySet();

                        iterator = set.iterator();

                        while (iterator.hasNext()) {
                            Holder<BiomeBase> holder = (Holder) iterator.next();
                            List<HolderSet<PlacedFeature>> list2 = ((BiomeSettingsGeneration) this.generationSettingsGetter.apply(holder)).features();

                            if (l < list2.size()) {
                                HolderSet<PlacedFeature> holderset = (HolderSet) list2.get(l);
                                FeatureSorter.b featuresorter_b = (FeatureSorter.b) list.get(l);

                                holderset.stream().map(Holder::value).forEach((placedfeature) -> {
                                    intarrayset.add(featuresorter_b.indexMapping().applyAsInt(placedfeature));
                                });
                            }
                        }

                        int j1 = intarrayset.size();
                        int[] aint = intarrayset.toIntArray();

                        Arrays.sort(aint);
                        FeatureSorter.b featuresorter_b1 = (FeatureSorter.b) list.get(l);

                        for (int k1 = 0; k1 < j1; ++k1) {
                            int l1 = aint[k1];
                            PlacedFeature placedfeature = (PlacedFeature) featuresorter_b1.features().get(l1);
                            Supplier<String> supplier1 = () -> {
                                Optional optional = iregistry1.getResourceKey(placedfeature).map(Object::toString);

                                Objects.requireNonNull(placedfeature);
                                return (String) optional.orElseGet(placedfeature::toString);
                            };

                            seededrandom.setFeatureSeed(i, l1, l);

                            try {
                                generatoraccessseed.setCurrentlyGenerating(supplier1);
                                placedfeature.placeWithBiomeCheck(generatoraccessseed, this, seededrandom, blockposition);
                            } catch (Exception exception1) {
                                //forThrowable
                                CrashReport crashreport1 = CrashReport.a(exception1, "Feature placement");

                                crashreportsystemdetails = crashreport1.a("Feature"); //addCategory
                                Objects.requireNonNull(supplier1);
                                crashreportsystemdetails.a("Description", supplier1::get); //setDetail
                                throw new ReportedException(crashreport1);
                            }
                        }
                    }
                    */
                }

                generatoraccessseed.a((Supplier) null); //setCurrentlyGenerating
            } catch (Exception exception2) {
                //forThrowable
                CrashReport crashreport2 = CrashReport.a(exception2, "Biome decoration");

                crashreport2.a("Generation") //addCategory
                            .a("CenterX", (Object) chunkcoordintpair.e) //setDetail
                            .a("CenterZ", (Object) chunkcoordintpair.f) //setDetail
                            .a("Decoration Seed", (Object) i); //setDetail
                throw new ReportedException(crashreport2);
            }
        }
    }


    @Override // applyCarvers
    public void a(RegionLimitedWorldAccess regionlimitedworldaccess, long seed,
                  RandomState randomstate, BiomeManager biomemanager,
                  StructureManager structuremanager, @NotNull IChunkAccess ichunkaccess)
    {
        // POPULATES BIOMES. IMPORTANT
        // (net.minecraft.world.level.biome.BiomeResolver,net.minecraft.world.level.biome.Climate$Sampler)
        // Use twBS as it is the biome provider that actually calculates biomes.
        // The other one only returns river/plains
        ichunkaccess.a(this.twBS, null); // This can be null as its passed into twBS

        // Call delegate applyCarvers to apply spigot ChunkGenerator;
        delegate.a(regionlimitedworldaccess, seed, randomstate, biomemanager,structuremanager, ichunkaccess);
    }

    @Override // getSeaLevel
    public int e() {
        return delegate.e();
    }

    /**
     * Overridden to allow VanillaStructurePopulator to work.
     * The code comes from createStructures, but with a lot of the in-built
     * checks cut out and replaced with TFG code.
     */
    @Override
    public void a(IRegistryCustom iregistrycustom, @NotNull ChunkGeneratorStructureState chunkgeneratorstructurestate, StructureManager structuremanager, @NotNull IChunkAccess ichunkaccess, StructureTemplateManager structuretemplatemanager, ResourceKey<World> resourcekey) {
        ChunkCoordIntPair chunkcoordintpair = ichunkaccess.f(); // getPos
        SectionPosition sectionposition = SectionPosition.a(ichunkaccess); // bottomOf
        RandomState randomstate = chunkgeneratorstructurestate.c(); // randomState
        MegaChunk mc = new MegaChunk(chunkcoordintpair.h, chunkcoordintpair.i);
        SingleMegaChunkStructurePopulator[] spops = StructureRegistry.getLargeStructureForMegaChunk(tw, mc);
        int[] centerCoords = mc.getCenterBiomeSectionChunkCoords();

        for(SingleMegaChunkStructurePopulator pop:spops)
        {
            if(!(pop instanceof VanillaStructurePopulator vpop)) continue;
            // possibleStructureSets
            possibleStructureSets
                .stream().filter((resourceLoc)->{
                    return vpop.structureRegistryKey.equals(resourceLoc.a()); // MinecraftKey.getPath()
                })
                .map((resourceLoc)-> MinecraftServer.getServer().ba().a(Registries.aW).orElseThrow().a(resourceLoc))
                .forEach((structureSet) -> {
                StructurePlacement structureplacement = structureSet.b(); // placement()
                List<StructureSet.a> list = structureSet.a(); // structures()

                // This will be true depending on the structure manager
                if (centerCoords[0] == chunkcoordintpair.h
                        && centerCoords[1] == chunkcoordintpair.i) {

                    // d() -> getLevelSeed()
                    try{
                        Object retVal = tryGenerateStructure.invoke(this, list.getFirst(), structuremanager, iregistrycustom, randomstate,
                                structuretemplatemanager, chunkgeneratorstructurestate.d(),
                                ichunkaccess, chunkcoordintpair, sectionposition, resourcekey);
                        TerraformGeneratorPlugin.logger.info(chunkcoordintpair.h + "," + chunkcoordintpair.i + " will spawn a vanilla structure, with tryGenerateStructure == " + retVal);
                    }
                    catch(Throwable t)
                    {
                        TerraformGeneratorPlugin.logger.info(chunkcoordintpair.h + "," + chunkcoordintpair.i + " Failed to generate a vanilla structure");
                        TerraformGeneratorPlugin.logger.stackTrace(t);
                    }
                }
            });
        }
    }
    @Override // createReferences. Structure related
    public void a(GeneratorAccessSeed gas,StructureManager manager,IChunkAccess ica)
    {
        delegate.a(gas, manager, ica);
    }

    @Override // getSpawnHeight
    public int a(LevelHeightAccessor levelheightaccessor) {
        return 64;
    }

    @Override // fillFromNoise
    public CompletableFuture<IChunkAccess> a(Blender blender,
                                             RandomState randomstate, StructureManager structuremanager,
                                             IChunkAccess ichunkaccess) {
        return delegate.a(blender,
                randomstate, structuremanager,
                ichunkaccess);
    }

    @Override // buildSurface. Used to be buildBase
    public void a(RegionLimitedWorldAccess regionlimitedworldaccess, StructureManager structuremanager, RandomState randomstate, IChunkAccess ichunkaccess)
    {
        delegate.a(regionlimitedworldaccess, structuremanager, randomstate, ichunkaccess);
    }


    @Override // getBaseColumn
    public BlockColumn a(int i, int j, LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
        return this.delegate.a(i,j,levelheightaccessor,randomstate);
    }

    // spawnOriginalMobs
    public void a(RegionLimitedWorldAccess regionlimitedworldaccess) {
        this.delegate.a(regionlimitedworldaccess);
    }


    // getSeaLevel
    @Override
    public int f() {
        return TerraformGenerator.seaLevel;
    }

    // getMinY
    @Override
    public int g() {
        return this.delegate.g();
    }

    @Override // getFirstFreeHeight
    public int b(int i, int j, HeightMap.Type heightmap_type,
                 LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
        return this.a(i, j, heightmap_type, levelheightaccessor, randomstate);
    }


    @Override // getFirstOccupiedHeight
    public int c(int i, int j, HeightMap.Type heightmap_type,
                 LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
        return this.a(i, j, heightmap_type, levelheightaccessor, randomstate) - 1;
    }

    @Override // getBaseHeight
    public int a(int i, int j, HeightMap.Type heightmap_type, LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
        // return delegate.a(x, z, var2, var3);
        return 100;
        // return org.terraform.coregen.HeightMap.getBlockHeight(tw, x, z);
    }

    // private static boolean biomeDebug = false;


    @Override // addDebugScreenInfo
    public void a(List<String> list, RandomState randomstate, BlockPosition blockposition) {

    }

}
