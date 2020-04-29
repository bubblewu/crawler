package com.bubble.crawler.util.db;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import redis.clients.jedis.*;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.*;

/**
 * @author wugang
 * date: 2020-04-03 15:31
 **/
public class RedisTest {

    public static void main(String[] args) {
        Jedis jedis = new Jedis("localhost", 6379);
        for (int i = 0; i < 100; i++) {
            Pipeline pipeline = jedis.pipelined();
            for (int j = i * 100; j < (i + 1) * 100; j++) {
                pipeline.hset("hashkey", "field-" + j, "value-" + i);
            }
            // pipelined.sync() 表示一次性的异步发送到redis，不关注执行结果。
            // pipelined.syncAndReturnAll()程序会阻塞，等到所有命令执行完之后返回一个List集合。
            // pipeline不适合组装特别多的命令，因此如果是成千上万的这种命令，要进行命令的拆分。
            List<Object> list = pipeline.syncAndReturnAll();
            List<String> strList = list.stream().map(Object::toString).collect(Collectors.toList());
            System.out.println(String.join(",", strList));
        }
    }

    /**
     * Redis Sentinel Failover故障选举测试：死循环对redis哨兵主从进行读写
     * 故障转移恢复案例：
     * - 执行该死循环程序；
     * - 将其中的7000节点进行强制宕机；这时程序会大量的报错：Connection refused。
     * - 过了n秒后，sentinel自动进行完故障转移后，程序就会正常执行打印；
     */
    private void testSentinelFailover() {
        Set<String> sentinelSet = Sets.newHashSet("127.0.0.1:26379", "127.0.0.1:26380", "127.0.0.1:26381");
        JedisSentinelPool jedisSentinelPool = new JedisSentinelPool("masterName", sentinelSet);
        int count = 0;
        while (true) {
            count++;
            Jedis jedis = null;
            try {
                jedis = jedisSentinelPool.getResource();
                int index = new Random().nextInt(10 * 10000);
                String key = "k-" + index;
                String value = "v-" + index;
                jedis.set(key, value);
                if (count % 100 == 0) {
                    System.out.println(String.format("%s value: %s", key, jedis.get(key)));
                }
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }
    }


}
