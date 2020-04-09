package com.bubble.crawler.util.db;

import com.google.common.collect.Lists;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.List;
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


}
