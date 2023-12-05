package org.terraform.coregen.populatordata;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitTask;
import org.terraform.biome.BiomeBank;
import org.terraform.coregen.ChunkCache;
import org.terraform.coregen.TerraLootTable;
import org.terraform.coregen.bukkit.TerraformGenerator;
import org.terraform.data.MegaChunkKey;
import org.terraform.data.SimpleBlock;
import org.terraform.data.SimpleChunkLocation;
import org.terraform.data.TerraformWorld;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.structure.StructurePopulator;
import org.terraform.structure.StructurePregenRunnable;
import org.terraform.structure.StructurePregenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Simulates the existence of recursive population.
 * However, this will enforce a strict bound within the MegaChunk mck
 * The structure will generate in its own bukkit task pregeneratorThread.
 * When a StructurePopulator wants to write, it will call a method in
 * this object to force a wait for the pregeneratorThread to finish.
 */
public class PopulatorDataStructurePregen extends PopulatorDataAbstract implements IPopulatorDataPhysicsCapable{

    private static final BlockData WATER = Bukkit.createBlockData(Material.WATER);
    private static final BlockData STONE = Bukkit.createBlockData(Material.STONE);
    private static final BlockData DEEPSLATE = Bukkit.createBlockData(Material.DEEPSLATE);
    private static final BlockData AIR = Bukkit.createBlockData(Material.AIR);
    private static final int CHUNK_VOLUME = 16*16*(TerraformGeneratorPlugin.injector.getMaxY()-TerraformGeneratorPlugin.injector.getMinY());
    private final MegaChunkKey mck;
    private final TerraformWorld tw;
    private final int chunkX,chunkZ;
    private final Thread pregeneratorThread;
    private final ConcurrentHashMap<ChunkCache, ChunkCache> readOnlyCache;

    private final StructurePopulator structurePop;

    //Tweak these accordingly during block placement to ensure that iterations
    //are faster during writes
    private short minY = (short) TerraformGeneratorPlugin.injector.getMaxY();
    private short maxY = (short) TerraformGeneratorPlugin.injector.getMinY();

    //This will be used to store general blockdata changes
    private final HashMap<SimpleChunkLocation, BlockData[]> blockDataChanges = new HashMap<>();

    //This is the lambda function used to spawn entities or apply loot tables etc
    private final HashMap<SimpleChunkLocation, List<Consumer<PopulatorDataAbstract>>> lambdas = new HashMap<>();

    public PopulatorDataStructurePregen(MegaChunkKey mck, int chunkX, int chunkZ, StructurePopulator structurePop, ConcurrentHashMap<ChunkCache, ChunkCache> readOnlyCache, TerraformGenerator generator) {
        this.mck = mck;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.tw = mck.getTw();
        this.readOnlyCache = readOnlyCache;
        this.structurePop = structurePop;
        this.pregeneratorThread = new Thread(new StructurePregenRunnable(tw, mck.getMc(),this,structurePop,readOnlyCache,generator));
        this.pregeneratorThread.start();
    }

    public void flush(PopulatorDataSpigotAPI data)
    {
        SimpleChunkLocation scl = new SimpleChunkLocation(data.getTerraformWorld().getName(),data.getChunkX(),data.getChunkZ());
        BlockData[] changes = blockDataChanges.remove(scl);
        if(changes != null)
            for(int i = 0; i < changes.length; i++)
            {
                if(changes[i] == null) continue;
                int x = i % 16;
                int z = ((i-x) % 256)/16;
                int y = (22055-x-z*16)/256 + TerraformGeneratorPlugin.injector.getMinY();
                x += chunkX*16; z += chunkZ*16;
                data.setBlockData(x,y,z,changes[i]);
            }

        List<Consumer<PopulatorDataAbstract>> toRun = lambdas.remove(scl);
        if(toRun != null)
        {
            toRun.forEach((c)->c.accept(data));
        }

        if(blockDataChanges.size() == 0 && lambdas.size() == 0)
            StructurePregenerator.markFinished(mck);
    }

    public void spinlock(){
        try {
            pregeneratorThread.join();
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Material getType(int x, int y, int z) {
        return getBlockData(x,y,z).getMaterial();
    }

    private int linearize(int x, int y, int z){
        //Force y to be positive
        return (x&0xF) + 16*(z&0xF) + 256*(y-TerraformGeneratorPlugin.injector.getMinY());
    }

    private boolean isInBounds(int rawX, int rawZ)
    {
        return Math.abs(chunkX-(rawX>>4)) <= structurePop.getPregenBoundaryRadius()
                && Math.abs(chunkZ-(rawZ>>4)) <= structurePop.getPregenBoundaryRadius();
    }

    @Override
    public BlockData getBlockData(int x, int y, int z) {
        if(!isInBounds(x,z))
            throw new RuntimeException("OOB Read at " + x + "," + y + "," + z + " for MC " + mck.getMc().getCenterBlockCoords()[0] + "," + mck.getMc().getCenterBlockCoords()[1]);

        BlockData[] changes = blockDataChanges.getOrDefault(new SimpleChunkLocation(tw.getName(),x,z), new BlockData[CHUNK_VOLUME]);
        BlockData cached = changes[linearize(x,y,z)];
        if(cached != null) return cached;
        //Use the raw block coord constructor because this is just a key.
        ChunkCache cache = readOnlyCache.get(new ChunkCache(tw,x,y,z));
        BiomeBank bank = tw.getBiomeBank(x,y,z);
        short transformedHeight = cache.getTransformedHeight(x&0xF,z&0xF);
        double heightmapHeight = cache.getHeightMapHeight(x,z);

        BlockData calculated;
        if(y < TerraformGenerator.seaLevel && y > heightmapHeight)
            calculated = WATER;
        else if(y < transformedHeight)
            calculated = y < 3 ? DEEPSLATE : STONE;
        else if(y > transformedHeight)
            calculated = AIR;
        else //y == transformedHeight
            calculated = bank.getHandler().getSurfaceCrust(new Random())[0].createBlockData();

        changes[linearize(x,y,z)] = calculated;
        return calculated;
    }

    @Override
    public void setType(int x, int y, int z, Material type) {
        setType(x,y,z, type, false);
    }

    @Override
    public void setBlockData(int x, int y, int z, BlockData data) {
        setBlockData(x,y,z,data,false);
    }

    @Override
    public void setType(int x, int y, int z, Material type, boolean updatePhysics) {
        setBlockData(x,y,z,Bukkit.createBlockData(type),updatePhysics);
    }

    @Override
    public void setBlockData(int x, int y, int z, BlockData type, boolean updatePhysics) {
        if(!isInBounds(x,z))
            throw new RuntimeException("OOB Write at " + x + "," + y + "," + z + " for MC " + mck.getMc().getCenterBlockCoords()[0] + "," + mck.getMc().getCenterBlockCoords()[1]);
        if(y > maxY) maxY = (short) y;
        if(y < minY) minY = (short) y;
        BlockData[] changes = blockDataChanges.getOrDefault(new SimpleChunkLocation(tw.getName(),x,z), new BlockData[CHUNK_VOLUME]);
        changes[linearize(x,y,z)] = type;

        if(updatePhysics)
        {
            List<Consumer<PopulatorDataAbstract>> functions = lambdas.getOrDefault(new SimpleChunkLocation(getTerraformWorld().getName(),x>>4,z>>4),new ArrayList<>());
            functions.add((d)->{
                if(d instanceof IPopulatorDataPhysicsCapable c)
                    c.setBlockData(x,y,z,type,true);
            });
        }
    }

    @Override
    public Biome getBiome(int rawX, int rawZ) {
        return mck.getTw().getBiomeBank(rawX,rawZ).getHandler().getBiome();
    }

    @Override
    public void addEntity(int rawX, int rawY, int rawZ, EntityType type) {
        List<Consumer<PopulatorDataAbstract>> functions = lambdas.getOrDefault(new SimpleChunkLocation(getTerraformWorld().getName(),rawX>>4,rawZ>>4),new ArrayList<>());
        functions.add((d)->d.addEntity(rawX,rawY,rawZ,type));
    }

    @Override
    public int getChunkX() {
        return chunkX;
    }

    @Override
    public int getChunkZ() {
        return chunkZ;
    }

    @Override
    public void setSpawner(int rawX, int rawY, int rawZ, EntityType type) {
        setType(rawX,rawY,rawZ,Material.SPAWNER);
        List<Consumer<PopulatorDataAbstract>> functions = lambdas.getOrDefault(new SimpleChunkLocation(getTerraformWorld().getName(),rawX>>4,rawZ>>4),new ArrayList<>());
        functions.add((d)->d.setSpawner(rawX,rawY,rawZ,type));
    }

    @Override
    public void lootTableChest(int x, int y, int z, TerraLootTable table) {
        setType(x,y,z,Material.CHEST);
        List<Consumer<PopulatorDataAbstract>> functions = lambdas.getOrDefault(new SimpleChunkLocation(getTerraformWorld().getName(),x>>4,z>>4),new ArrayList<>());
        functions.add((d)->d.lootTableChest(x,y,z,table));
    }

    @Override
    public TerraformWorld getTerraformWorld() {
        return tw;
    }

    public StructurePopulator getStructurePop() {
        return structurePop;
    }
}
