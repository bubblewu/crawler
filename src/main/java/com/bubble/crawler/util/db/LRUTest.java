package com.bubble.crawler.util.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU算法实现测试：
 * LRU（The Least Recently Used，最近最久未使用算法）是一种常见的缓存算法，在很多分布式缓存系统（如Redis, Memcached）中都有广泛使用。
 * LRU算法的思想是：如果一个数据在最近一段时间没有被访问到，那么可以认为在将来它被访问的可能性也很小。
 * 因此，当空间满时，最久没有访问的数据最先被置换（淘汰）。
 * * 实现：最朴素的思想就是用数组+时间戳的方式，不过这样做效率较低。
 * * 因此，我们可以用双向链表（LinkedList）+ 哈希表（HashMap）实现（链表用来表示位置，哈希表用来存储和查找），
 * * 在Java里有对应的数据结构LinkedHashMap。
 *
 * @author wugang
 * date: 2020-04-29 11:27
 **/
public class LRUTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(LRUTest.class);

    /**
     * 简单用LinkedHashMap来实现的LRU算法的缓存
     *
     * @param <K> key
     * @param <V> value
     */
    public static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private int cacheSize;

        public LRUCache(int cacheSize) {
            // LinkedHashMap默认的支持按照插入顺序保存数据，新的数据插入双向链表尾部。
            // 构造方法支持按照访问顺序，最新访问的数据放到双向链表尾部。
            // 所以实现LRU算法：只需要自动删除掉链表头的过期数据即可。表尾部为常访问热点数据；
            super(16, 0.75f, true);
            this.cacheSize = cacheSize;
        }

        /**
         * LinkedHashMap默认是不会自动删除链表头节点数据的，我们需要覆盖类的一个方法：removeEldestEntry
         * 重写LinkedHashMap中的removeEldestEntry方法，当LRU中元素多余cacheSize个时，删除最不经常使用的元素
         * @param eldest
         * @return
         */
        @Override
        protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
            return size() > cacheSize;
        }
    }

    private static LRUCache<String, Integer> cache = new LRUCache<>(10);
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            cache.put("NO." + i, i);
        }
        LOGGER.info("all cache: {}", cache);
        cache.get("NO.3");
        LOGGER.info("After get cache 3: {}", cache);
        cache.get("NO.4");
        LOGGER.info("After get cache 4: {}", cache);

        cache.put("NO.10", 10);
        LOGGER.info("LRU 执行后cache：{}", cache);
    }

}
