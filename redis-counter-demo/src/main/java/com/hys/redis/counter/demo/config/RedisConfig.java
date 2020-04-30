package com.hys.redis.counter.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * Redis配置类
 *
 * @author Robert Hou
 * @date 2020年05月01日 04:29
 **/
@Configuration
public class RedisConfig {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 获取指定key中位值为1的个数
     *
     * @param key 键
     * @return 位值为1的个数
     */
    public Long bitCount(String key) {
        return redisTemplate.execute((RedisCallback<Long>) con -> con.bitCount(key.getBytes()));
    }

    /**
     * 对多个BitMap进行操作并将结果保存在saveKey中
     *
     * @param op      and（交集）、or（并集）、not（非）、xor（异或）
     * @param destKey 保存的键
     * @param srcKeys 进行操作的key的集合
     */
    public void bitOp(RedisStringCommands.BitOperation op, String destKey, List<String> srcKeys) {
        byte[][] bytes = new byte[srcKeys.size()][];
        for (int i = 0; i < srcKeys.size(); i++) {
            bytes[i] = srcKeys.get(i).getBytes();
        }
        redisTemplate.execute((RedisCallback<Long>) con -> con.bitOp(op, destKey.getBytes(), bytes));
    }
}
