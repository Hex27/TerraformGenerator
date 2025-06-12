package org.terraform.utils.datastructs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.main.TerraformGeneratorPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * A concurrent cache which protects the duplicate creation of values V.
 * V should NOT be mutated by users. After a safe rewrite it should extend Record.
 * This record should not get mutated in any way.
 * <br>
 * The point of this cache is to store REPEATABLE CALCULATIONS. It uses a very relaxed
 * lock system and WILL NOT protect concurrent writes to the object instances V -
 * it will only guarantee that one unique V exists for each K associated with it.
 * @param <K>
 * @param <V>
 */
public final class ConcurrentLRUCache<K,V> {
    private final ThreadLocal<HashMap<K,LRUNode<K,V>>> localCache = ThreadLocal.withInitial(HashMap::new);
    private final int maxSize;
    private final int localCacheSize;
    private final ReadWriteLock rwlock = new ReentrantReadWriteLock();
    private final Lock readLock = rwlock.readLock();
    private final Lock writeLock = rwlock.writeLock();
    private final HashMap<K,LRUNode<K,V>> keyToValue;
    private final Function<@NotNull K,@Nullable V> generator;

    //Benchmarking things. Comment out when not in use, as accessing them
    // is not necessarily cheap.
    //private int misses = 0;
    //private int hits = 0;
    private final String name;
    //private final AtomicInteger localMisses = new AtomicInteger(0);
    //private final AtomicInteger localHits = new AtomicInteger(0);

    public ConcurrentLRUCache(String name, int maxSize, Function<@NotNull K,@Nullable V> generator) {
        this.name = name;
        this.maxSize = maxSize;
        this.localCacheSize = 5;
        this.generator = generator;
        this.keyToValue = new HashMap<>(maxSize);
    }
    public ConcurrentLRUCache(String name, int maxSize, int localMaxSize, Function<@NotNull K,@Nullable V> generator) {
        this.name = name;
        this.maxSize = maxSize;
        this.localCacheSize = localMaxSize;
        this.generator = generator;
        this.keyToValue = new HashMap<>(maxSize);
    }

    /**
     * Starts acquiring locks and interacting with the global cache
     */
    private LRUNode<K,V> slowPathGet(K key){
        LRUNode<K,V> node = null;
        readLock.lock();
        try{
            node = keyToValue.get(key);
            if(node != null){
                //hits++;
                //I don't really know if set needs to be used here.
                //In theory, this only matters if someone is trying to write NOW,
                // but that can't happen because this method holds a read lock.
                node.lastAccess.lazySet(System.nanoTime());
            }
        }finally{
            readLock.unlock();
            if(node == null) //handle cache miss
                node = calculateAndInsert(key);
        }
        return node;
    }

    /**
     * Checks the ThreadLocal hashmap first.
     */
    public V get(K key){
        var localMap = localCache.get();
        LRUNode<K,V> node = localMap.get(key);
        if(node == null) {
            //localMisses.incrementAndGet();
            node = slowPathGet(key);
            if(localMap.size() >= localCacheSize)
                localMap.clear();
            localMap.put(key,node);
        }else {
            //localHits.incrementAndGet();
            node.lastAccess.lazySet(System.nanoTime());
        }
        return node.value;
    }

    /**
     * Assumes that the key was not already present
     */
    private LRUNode<K,V> calculateAndInsert(K key){
        LRUNode<K,V> node;
        writeLock.lock();
        try{
            //It's possible for two threads to miss the same key.
            // One of the threads will win in writing it first, so
            // other threads can just check and release their write lock.
            node = keyToValue.get(key);
            if(node == null){
                //misses++;
                if(keyToValue.size() >= maxSize) pruneLRU();
                node = new LRUNode<>(key, generator.apply(key));
                keyToValue.put(key, node);
            }else {
                //hits++;
            }
        }finally{
            writeLock.unlock();
        }
        return node;
    }

    /**
     * WARNING: USE ONLY WHEN THE WRITE LOCK IS ACQUIRED.
     * <br>
     * This method will remove nodes that have a similar timestamp as the
     * least-recently-used node
     */
    private void pruneLRU(){
        ArrayList<LRUNode<K,V>> nodes = new ArrayList<>(keyToValue.values());

        //A sort was replaced with this here, as Java collections yells about
        // unstable sorting (the timestamp reordered objects).
        //This stupid shit is O(2n), which is probably better than O(nlogn) for
        // sorting, so i guess this is as good as it gets.
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        for (LRUNode<K, V> node : nodes) {
            long cmp = node.snap(); //Takes a snapshot of all times NOW
            min = Math.min(cmp,min);
            max = Math.max(cmp,max);
        }

        //Find the midpoint between the oldest and newest entry
        long midPoint = (min+max)/2;

        //Aggressively delete references before the midpoint
        //int pruned = 0;
        for (LRUNode<K, V> node : nodes) {
            if(node.snapshot < midPoint) {
                keyToValue.remove(node.key);
                //pruned++;
            }
        }

        //TerraformGeneratorPlugin.logger.info(name + " pruned " + (pruned) + " entries.");
        //TerraformGeneratorPlugin.logger.info(name + " pruned " + (index) + " entries."
        //                                     + "\n\t Current Hit Ratio: " + (((double)hits)/((double)(hits+misses+1)))
        //                                     + "\n\t Local Hit Ratio: " + (((double)localHits.get())/((double)(localHits.get()+localMisses.get()+1))));
        //hits = 0;
        //misses = 0;
    }

    private static final class LRUNode<K,V> {
        public final @NotNull K key;
        public final @Nullable V value;
        public final @NotNull AtomicLong lastAccess = new AtomicLong(System.nanoTime());

        private long snapshot;
        public LRUNode(@NotNull K k, @Nullable V v){
            key = k;
            value = v;
        }
        public long snap(){
            snapshot = lastAccess.get();
            return snapshot;
        }
    }
}
