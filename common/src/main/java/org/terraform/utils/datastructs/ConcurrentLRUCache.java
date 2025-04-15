package org.terraform.utils.datastructs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.terraform.main.TerraformGeneratorPlugin;

import java.util.ArrayList;
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
    private final int maxSize;
    private final ReadWriteLock rwlock = new ReentrantReadWriteLock();
    private final Lock readLock = rwlock.readLock();
    private final Lock writeLock = rwlock.writeLock();
    private final HashMap<K,LRUNode<K,V>> keyToValue;
    private final Function<@NotNull K,@Nullable V> generator;
    private int misses = 0;
    private int hits = 0;

    public ConcurrentLRUCache(int maxSize, Function<@NotNull K,@Nullable V> generator) {
        this.maxSize = maxSize;
        this.generator = generator;
        this.keyToValue = new HashMap<>(maxSize);
    }

    public V get(K key){
        V retVal = null;
        readLock.lock();
        try{
            LRUNode<K,V> node = keyToValue.get(key);
            if(node != null){
                hits++;
                retVal = node.value;
                //I don't really know if set needs to be used here.
                //In theory, this only matters if someone is trying to write NOW,
                // but that can't happen because this method holds a read lock.
                node.lastAccess.lazySet(System.nanoTime());
            }
        }finally{
            readLock.unlock();
            if(retVal == null) //handle cache miss
                retVal = calculateAndInsert(key);

        }
        return retVal;
    }

    /**
     * Assumes that the key was not already present
     */
    private V calculateAndInsert(K key){
        writeLock.lock();
        //It's possible for two threads to miss the same key.
        // One of the threads will win in writing it first, so
        // other threads can be just check and release their write lock.
        V val;
        LRUNode<K,V> node = keyToValue.get(key);
        try{
            if(node == null){
                misses++;
                if(keyToValue.size() >= maxSize) pruneLRU();
                val = generator.apply(key);
                LRUNode<K,V> newNode = new LRUNode<>(key, val);
                keyToValue.put(key, newNode);
            }else {
                hits++;
                val = node.value;
            }
        }finally{
            writeLock.unlock();
        }
        return val;
    }

    /**
     * WARNING: USE ONLY WHEN THE WRITE LOCK IS ACQUIRED.
     * <br>
     * This method will remove nodes that have a similar timestamp as the
     * least-recently-used node
     */
    private void pruneLRU(){
        ArrayList<LRUNode<K,V>> nodes = new ArrayList<>(keyToValue.values());
        nodes.sort(Comparator.comparingLong(LRUNode::snap));
        //Find the midpoint between the oldest and newest entry
        long midPoint = (nodes.get(nodes.size()-1).snapshot + nodes.get(0).snapshot)/2;

        //Aggressively delete references before the midpoint
        int index = 0;
        while(index < nodes.size() && nodes.get(index).snapshot < midPoint){
            keyToValue.remove(nodes.get(index).key);
            index++;
        }
        //TerraformGeneratorPlugin.logger.info("LRU pruned " + (index) + " entries. Current Hit Ratio: " + (hits/(hits+misses+1)));
        hits = 0;
        misses = 0;
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
