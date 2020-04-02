package com.bubble.crawler.util.db;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;

/**
 * Redis工具类
 *
 * @author wugang
 * date: 2020-03-31 18:05
 **/
public class RedisUtil {

    private JedisPool jedisPool;

    public RedisUtil() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(10);
        poolConfig.setMaxTotal(100);
        poolConfig.setMaxWaitMillis(10000);
        poolConfig.setTestOnBorrow(true);
        jedisPool = new JedisPool(poolConfig, "localhost", 6379);
    }

    /* Redis之List列表类型操作 */

    /**
     * 列表：左侧插入
     *
     * @param key   key
     * @param value value
     */
    public void add(String key, String value) {
        Jedis jedis = jedisPool.getResource();
        jedis.lpush(key, value);
        jedisPool.returnResourceObject(jedis);
    }

    /**
     * 列表：根据起始位置左侧查询
     *
     * @param key   key
     * @param start 开始位置
     * @param end   结束位置（包含end）
     * @return 符合条件的元素列表
     */
    public List<String> query(String key, int start, int end) {
        Jedis jedis = jedisPool.getResource();
        List<String> list = jedis.lrange(key, start, end);
        jedisPool.returnResourceObject(jedis);
        return list;
    }

    /**
     * 列表：从右弹出list中的一个元素
     *
     * @param key key
     * @return 弹出的元素
     */
    public String poll(String key) {
        Jedis jedis = jedisPool.getResource();
        String result = jedis.rpop(key);
        jedisPool.returnResourceObject(jedis);
        return result;
    }

    /* Redis之Set集合类型操作 */

    /**
     * 集合：添加元素
     *
     * @param Key   key
     * @param value value
     */
    public void addSet(String Key, String value) {
        Jedis jedis = jedisPool.getResource();
        jedis.sadd(Key, value);
        jedisPool.returnResourceObject(jedis);
    }

    /**
     * 集合：随机获取key下的某个元素
     *
     * @param key key
     */
    public String getSet(String key) {
        Jedis jedis = jedisPool.getResource();
        String value = jedis.srandmember(key);
        jedisPool.returnResourceObject(jedis);
        return value;
    }

    /**
     * 集合：删除key的value
     *
     * @param key   key
     * @param value value
     */
    public void deleteSet(String key, String value) {
        Jedis jedis = jedisPool.getResource();
        jedis.srem(key, value);
        jedisPool.returnResourceObject(jedis);
    }


}
