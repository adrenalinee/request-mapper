package malibu.requestmapper.util;

import java.util.HashMap;
import java.util.Map;

/**
 *  최대 크기가 정해진 map.
 *  다른 키가 계속해서 쌓여가기만 하는 상황을 막기 위해 사용될 수 있음.
 *
 * @param <K>
 * @param <V>
 */
public class FixedSizeCache<K, V> {
    private final Map<K, V> map = new HashMap<>();
    private final K[] keys;
    private int currIndex = 0;

    public FixedSizeCache(int size) {
        if(size < 1)
            throw new IllegalArgumentException("Cache size must be at least 1!");
        this.keys = (K[]) new Object[size];
    }

    public void put(K key, V value) {
        if(!contains(key)) {
            if(keys[currIndex] != null) {
                map.remove(keys[currIndex]);
            }
            keys[currIndex] = key;
            currIndex = (currIndex + 1) % keys.length;
        }
        map.put(key, value);
    }

    public boolean contains(K key) {
        return map.containsKey(key);
    }

    public V get(K key) {
        return map.get(key);
    }
}
