package org.terraform.v1_21_R6;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.FeatureCountTracker;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
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
import org.terraform.utils.version.TerraformFieldHandler;
import org.terraform.utils.version.TerraformMethodHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class NMSChunkGenerator extends ChunkGenerator {
    private final @NotNull ChunkGenerator delegate;
    private final @NotNull TerraformWorld tw;
    private final @NotNull MapRenderWorldProviderBiome mapRendererBS;
    private final @NotNull TerraformWorldProviderBiome twBS;
    private final @NotNull TerraformMethodHandler tryGenerateStructure;
    private final ArrayList<ResourceLocation> possibleStructureSets = new ArrayList<>();

    private final @NotNull TerraformMethodHandler getWriteableArea;
    private final @NotNull Supplier featuresPerStep;

    public NMSChunkGenerator(String worldName, long seed, @NotNull ChunkGenerator delegate)
            throws NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalAccessException
    {
        super(
                delegate.getBiomeSource(), // BiomeSource d() is getBiomeSource()
                delegate.generationSettingsGetter); // Idk what generationSettingsGetter is
        tw = TerraformWorld.get(worldName, seed);
        this.delegate = delegate;

        // Set the long term biome handler to this one. The normal behaving one
        // is initiated inside the cave carver
        mapRendererBS = new MapRenderWorldProviderBiome(tw, delegate.getBiomeSource());
        twBS = new TerraformWorldProviderBiome(TerraformWorld.get(worldName, seed), delegate.getBiomeSource());

        //This is needed for addVanillaFeatures (c)

        featuresPerStep = (Supplier) new TerraformFieldHandler(ChunkGenerator.class, "featuresPerStep", "c")
                .field.get(delegate);

        //a
        getWriteableArea = new TerraformMethodHandler(ChunkGenerator.class,
                new String[]{"getWritableArea", "a"}, ChunkAccess.class);

        // This is tryGenerateStructure
        // Register VanillaStructurePopulators to allow Minecraft to properly
        // handle them
        for(StructurePopulator pop : StructureRegistry.getAllPopulators())
        {
            if(pop instanceof VanillaStructurePopulator vsp)
            {
                possibleStructureSets.add(ResourceLocation.parse(vsp.structureRegistryKey)); // ResourceLocation.create
            }
        }
        //a
        tryGenerateStructure = new TerraformMethodHandler(ChunkGenerator.class,
                new String[]{"tryGenerateStructure", "a"},
                StructureSet.StructureSelectionEntry.class,
                StructureManager.class,
                RegistryAccess.class,
                RandomState.class,
                StructureTemplateManager.class,
                long.class,
                ChunkAccess.class,
                ChunkPos.class,
                SectionPos.class,
                ResourceKey.class);
    }


    @Override // getBiomeSource
    public @NotNull BiomeSource getBiomeSource() {
        return mapRendererBS;
    }

    public @NotNull TerraformWorld getTerraformWorld() {
        return tw;
    }

    @Override
    protected @NotNull MapCodec<? extends ChunkGenerator> codec() {
        return MapCodec.unit(null);
    }

    @Override // createBiomes
    public @NotNull CompletableFuture<ChunkAccess> createBiomes(RandomState randomstate, Blender blender, StructureManager structuremanager, @NotNull ChunkAccess ChunkAccess)
    {
        return CompletableFuture.supplyAsync(() -> {
            return ChunkAccess; // Don't do any calculations here, biomes are set in applyCarvers
        }, Util.backgroundExecutor().forName("init_biomes"));
        //Util.backgroundExecutor().
    }

    @Override // findNearestMapStructure
    public Pair<BlockPos, Holder<Structure>> findNearestMapStructure(ServerLevel ServerLevel, @NotNull HolderSet<Structure> holderset,
                                                    @NotNull BlockPos BlockPos, int i, boolean flag) {

        int pX = BlockPos.getX(); // getX
        int pZ = BlockPos.getZ(); // getZ

        for(Holder<Structure> holder:holderset) {
            Structure feature = holder.value();
            // StructureGenerator<?> structuregenerator = feature.;
            TerraformGeneratorPlugin.logger.info("Vanilla locate for " + feature.getClass().getName() + " invoked.");

            if (holder.value().getClass() == StrongholdStructure.class) { // stronghold
                int[] coords = new StrongholdPopulator().getNearestFeature(tw, pX, pZ);
                return new Pair<>(new BlockPos(coords[0], 20, coords[1]), holder);
            }
            else if(!TConfig.c.DEVSTUFF_VANILLA_LOCATE_DISABLE)
            {
                if (holder.value().getClass() == OceanMonumentStructure.class) { // Monument

                    int[] coords = StructureLocator.locateSingleMegaChunkStructure(tw, pX, pZ, new MonumentPopulator(), TConfig.c.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS);

                    return new Pair<>
                            (new BlockPos(coords[0], 50, coords[1]), holder);
                } else if (holder.value().getClass() == WoodlandMansionStructure.class) { // Mansion

                    int[] coords = StructureLocator.locateSingleMegaChunkStructure(tw, pX, pZ, new MansionPopulator(), TConfig.c.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS);

                    return new Pair<>
                            (new BlockPos(coords[0], 50, coords[1]), holder);
                } else if (holder.value() instanceof JigsawStructure
                           //bm is structure
                        && MinecraftServer.getServer().registryAccess().lookup(Registries.STRUCTURE).orElseThrow().getValue(ResourceLocation.parse("trial_chambers")) == holder.value()
                ) { // Trial Chamber

                    int[] coords = StructureLocator.locateSingleMegaChunkStructure(tw, pX, pZ, new TrialChamberPopulator(), TConfig.c.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS);

                    return new Pair<>
                            (new BlockPos(coords[0], 50, coords[1]), holder);
                } else if (holder.value().getClass() == BuriedTreasureStructure.class) {
                    // Buried Treasure
                    int[] coords = StructureLocator.locateMultiMegaChunkStructure(tw, new MegaChunk(pX, 0, pZ), new BuriedTreasurePopulator(), TConfig.c.DEVSTUFF_VANILLA_LOCATE_TIMEOUTMILLIS);
                    if(coords == null) return null;
                    return new Pair<>
                            (new BlockPos(coords[0], 50, coords[1]), holder);
                }
            }
        }
        return null;
    }

    @Override // applyBiomeDecoration
    public void applyBiomeDecoration(WorldGenLevel worldGenLevel, ChunkAccess ChunkAccess, StructureManager structuremanager) {
        delegate.applyBiomeDecoration(worldGenLevel, ChunkAccess, structuremanager);

        // This triggers structure gen. Needed for VanillaStructurePopulator
        addVanillaDecorations(worldGenLevel,ChunkAccess, structuremanager);
    }

    //This has to be overridden because calling the normal one will make vanilla
    // generate ores. The giant commented swath of stuff did it
    @Override
    public void addVanillaDecorations(WorldGenLevel worldGenLevel, ChunkAccess chunkAccess, StructureManager structuremanager) { // CraftBukkit
        ChunkPos ChunkPos = chunkAccess.getPos();
        if (!SharedConstants.debugVoidTerrain(ChunkPos)) {
            SectionPos sectionPos = SectionPos.of(ChunkPos, worldGenLevel.getMinSectionY());
            BlockPos BlockPos = sectionPos.origin();
            Registry<Structure> iregistry = worldGenLevel.registryAccess().lookupOrThrow(Registries.STRUCTURE);
            Map<Integer, List<Structure>> map = (Map<Integer, List<Structure>>)iregistry.stream().collect(Collectors.groupingBy((structurex) -> {
                return structurex.step().ordinal();
            }));
            //This is private c
            List<FeatureSorter.StepFeatureData> list = (List<FeatureSorter.StepFeatureData>)this.featuresPerStep.get();
            WorldgenRandom seededrandom = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.generateUniqueSeed()));
            long i = seededrandom.setDecorationSeed(worldGenLevel.getSeed(), BlockPos.getX(), BlockPos.getZ());
            Set<Holder<Biome>> set = new ObjectArraySet<Holder<Biome>>();
            ChunkPos.rangeClosed(sectionPos.chunk(), 1).forEach((ChunkPos1) -> {
                ChunkAccess ichunkaccess1 = worldGenLevel.getChunk(ChunkPos1.x, ChunkPos1.z);

                for (LevelChunkSection chunksection : ichunkaccess1.getSections()) {
                    PalettedContainerRO<Holder<Biome>> palettedcontainerro = chunksection.getBiomes();

                    Objects.requireNonNull(set);
                    palettedcontainerro.getAll(set::add);
                }
            });
            set.retainAll(this.biomeSource.possibleBiomes());
            int j = list.size();

            try {
                Registry iregistry1 = worldGenLevel.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
                int k = Math.max(GenerationStep.Decoration.values().length, j);

                for(int l = 0; l < k; ++l) {
                    int i1 = 0;
                    if (structuremanager.shouldGenerateStructures()) {
                        for (Structure structure : (List<Structure>) map.getOrDefault(l, Collections.emptyList())) {
                            seededrandom.setFeatureSeed(i, i1, l);
                            Supplier<String> supplier = () -> {
                                Optional optional = iregistry.getResourceKey(structure).map(Object::toString);
                                Objects.requireNonNull(structure);
                                return (String)optional.orElseGet(structure::toString);
                            };

                            try {
                                worldGenLevel.setCurrentlyGenerating(supplier);
                                structuremanager.startsForStructure(sectionPos, structure).forEach((structurestart) -> {
                                    try{
                                        structurestart.placeInChunk(worldGenLevel, structuremanager, this,
                                                seededrandom, (BoundingBox) getWriteableArea.method.invoke(null,chunkAccess),
                                                ChunkPos);
                                    }catch(IllegalAccessException | InvocationTargetException e){
                                        CrashReport crashreport = CrashReport.forThrowable(e, "TerraformGenerator");
                                        throw new ReportedException(crashreport);
                                    }
                                });
                            } catch (Exception var31) {
                                CrashReport crashreport = CrashReport.forThrowable(var31, "Feature placement");
                                CrashReportCategory crashreportsystemdetails = crashreport.addCategory("Feature");
                                Objects.requireNonNull(supplier);
                                crashreportsystemdetails.setDetail("Description", supplier::get);
                                throw new ReportedException(crashreport);
                            }
                        }
                    }

                    //This section creates ores. In order to see what the other stuff is,
                    // refer to previous version's code.
/*
                    if (l < j) {
                        IntSet intset = new IntArraySet();
                        Iterator var35 = set.iterator();

                        while(var35.hasNext()) {
                            Holder holder = (Holder)var35.next();
                            List list1 = ((BiomeSettingsGeneration)this.d.apply(holder)).c();
                            if (l < list1.size()) {
                                HolderSet holderset = (HolderSet)list1.get(l);
                                FeatureSorter.b featuresorter_b = (FeatureSorter.b)list.get(l);
                                holderset.a().map(Holder::a).forEach((placedfeaturex) -> {
                                    intset.add(featuresorter_b.b().applyAsInt(placedfeaturex));
                                });
                            }
                        }

                        int j1 = intset.size();
                        int[] aint = intset.toIntArray();
                        Arrays.sort(aint);
                        FeatureSorter.b featuresorter_b1 = (FeatureSorter.b)list.get(l);

                        for(int k1 = 0; k1 < j1; ++k1) {
                            int l1 = aint[k1];
                            PlacedFeature placedfeature = (PlacedFeature)featuresorter_b1.a().get(l1);
                            Supplier supplier1 = () -> {
                                Optional optional = iregistry1.d(placedfeature).map(Object::toString);
                                Objects.requireNonNull(placedfeature);
                                Objects.requireNonNull(placedfeature);
                                return (String)optional.orElseGet(placedfeature::toString);
                            };
                            seededrandom.b(i, l1, l);

                            try {
                                WorldGenLevel.a(supplier1);
                                placedfeature.b(WorldGenLevel, this, seededrandom, BlockPos);
                            } catch (Exception var30) {
                                CrashReport crashreport1 = CrashReport.a(var30, "Feature placement");
                                CrashReportCategory crashreportsystemdetails1 = crashreport1.a("Feature");
                                Objects.requireNonNull(supplier1);
                                Objects.requireNonNull(supplier1);
                                crashreportsystemdetails1.a("Description", supplier1::get);
                                throw new ReportedException(crashreport1);
                            }
                        }
                    }
*/
                }

                worldGenLevel.setCurrentlyGenerating((Supplier)null);
                if (SharedConstants.DEBUG_FEATURE_COUNT) {
                    FeatureCountTracker.chunkDecorated(worldGenLevel.getLevel());
                }
            } catch (Exception var32) {
                CrashReport crashreport2 = CrashReport.forThrowable(var32, "Biome decoration");
                crashreport2.addCategory("Generation").setDetail("CenterX", ChunkPos.x).setDetail("CenterZ", ChunkPos.z).setDetail("Decoration Seed", i);
                throw new ReportedException(crashreport2);
            }
        }

    }


    @Override // applyCarvers
    public void applyCarvers(WorldGenRegion worldGenRegion, long seed,
                  RandomState randomstate, BiomeManager biomemanager,
                  StructureManager structuremanager, @NotNull ChunkAccess chunkAccess)
    {
        // POPULATES BIOMES. IMPORTANT
        // (net.minecraft.world.level.biome.BiomeResolver,net.minecraft.world.level.biome.Climate$Sampler)
        // Use twBS as it is the biome provider that actually calculates biomes.
        // The other one only returns river/plains
        chunkAccess.fillBiomesFromNoise(this.twBS, null); // This can be null as its passed into twBS

        // Call delegate applyCarvers to apply spigot ChunkGenerator;
        delegate.applyCarvers(worldGenRegion, seed, randomstate, biomemanager,structuremanager, chunkAccess);
    }

    @Override // getGenDepth
    public int getGenDepth() {
        return delegate.getGenDepth();
    }

    /**
     * Overridden to allow VanillaStructurePopulator to work.
     * The code comes from createStructures, but with a lot of the in-built
     * checks cut out and replaced with TFG code.
     */
    @Override
    public void createStructures(RegistryAccess registryAccess, @NotNull ChunkGeneratorStructureState chunkgeneratorstructurestate, StructureManager structuremanager, @NotNull ChunkAccess ChunkAccess, StructureTemplateManager structuretemplatemanager, ResourceKey<Level> resourcekey) {
        ChunkPos ChunkPos = ChunkAccess.getPos(); // getPos
        SectionPos sectionPos = SectionPos.bottomOf(ChunkAccess); // bottomOf
        RandomState randomstate = chunkgeneratorstructurestate.randomState(); // randomState
        MegaChunk mc = new MegaChunk(ChunkPos.x, ChunkPos.z);
        SingleMegaChunkStructurePopulator[] spops = StructureRegistry.getLargeStructureForMegaChunk(tw, mc);
        int[] centerCoords = mc.getCenterBiomeSectionChunkCoords();
        if(spops == null) return;
        for(SingleMegaChunkStructurePopulator pop:spops)
        {
            if(!(pop instanceof VanillaStructurePopulator vpop)) continue;
            // possibleStructureSets
            possibleStructureSets
                .stream().filter((resourceLoc)->{
                    return vpop.structureRegistryKey.equals(resourceLoc.getPath()); // ResourceLocation.getPath()
                })
                //Registries.STRUCTURE_SET
                .map((resourceLoc)-> MinecraftServer.getServer().registryAccess().lookup(Registries.STRUCTURE_SET).orElseThrow().getValue(resourceLoc))
                .forEach((structureSet) -> {
                StructurePlacement structureplacement = structureSet.placement(); // placement()
                List<StructureSet.StructureSelectionEntry> list = structureSet.structures(); // structures()

                // This will be true depending on the structure manager
                if (centerCoords[0] == ChunkPos.x
                        && centerCoords[1] == ChunkPos.z) {

                    // d() -> getLevelSeed()
                    try{
                        Object retVal = tryGenerateStructure.method.invoke(this, list.getFirst(), structuremanager, registryAccess, randomstate,
                                structuretemplatemanager, chunkgeneratorstructurestate.getLevelSeed(),
                                ChunkAccess, ChunkPos, sectionPos, resourcekey);
                        TerraformGeneratorPlugin.logger.info(ChunkPos.x + "," + ChunkPos.z + " will spawn a vanilla structure, with tryGenerateStructure == " + retVal);
                    }
                    catch(Throwable t)
                    {
                        TerraformGeneratorPlugin.logger.info(ChunkPos.x + "," + ChunkPos.z + " Failed to generate a vanilla structure");
                        TerraformGeneratorPlugin.logger.stackTrace(t);
                    }
                }
            });
        }
    }
    @Override // createReferences. Structure related
    public void createReferences(WorldGenLevel gas,StructureManager manager,ChunkAccess ica)
    {
        delegate.createReferences(gas, manager, ica);
    }

    @Override // getSpawnHeight
    public int getSpawnHeight(LevelHeightAccessor levelheightaccessor) {
        return 64;
    }

    @Override // fillFromNoise
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender,
                                             RandomState randomstate, StructureManager structuremanager,
                                             ChunkAccess ChunkAccess) {
        return delegate.fillFromNoise(blender,
                randomstate, structuremanager,
                ChunkAccess);
    }

    @Override // buildSurface. Used to be buildBase
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structuremanager, RandomState randomstate, ChunkAccess ChunkAccess)
    {
        delegate.buildSurface(worldGenRegion, structuremanager, randomstate, ChunkAccess);
    }


    @Override // getBaseColumn
    public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
        return this.delegate.getBaseColumn(i,j,levelheightaccessor,randomstate);
    }

    // spawnOriginalMobs
    public void spawnOriginalMobs(WorldGenRegion WorldGenRegion) {
        this.delegate.spawnOriginalMobs(WorldGenRegion);
    }


    // getSeaLevel
    @Override
    public int getSeaLevel() {
        return TerraformGenerator.seaLevel;
    }

    // getMinY
    @Override
    public int getMinY() {
        return this.delegate.getMinY();
    }

    @Override // getFirstFreeHeight
    public int getFirstFreeHeight(int i, int j, Heightmap.Types heightmap_type,
                 LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
        return this.getFirstFreeHeight(i, j, heightmap_type, levelheightaccessor, randomstate);
    }


    @Override // getFirstOccupiedHeight
    public int getFirstOccupiedHeight(int i, int j, Heightmap.Types heightmap_type,
                 LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
        return this.getFirstOccupiedHeight(i, j, heightmap_type, levelheightaccessor, randomstate) - 1;
    }

    @Override // getBaseHeight
    public int getBaseHeight(int i, int j, Heightmap.Types heightmap_type, LevelHeightAccessor levelheightaccessor, RandomState randomstate) {
        // return delegate.a(x, z, var2, var3);
        return 100;
        // return org.terraform.coregen.HeightMap.getBlockHeight(tw, x, z);
    }

    // private static boolean biomeDebug = false;


    @Override // addDebugScreenInfo
    public void addDebugScreenInfo(List<String> list, RandomState randomstate, BlockPos BlockPos) {

    }

}
