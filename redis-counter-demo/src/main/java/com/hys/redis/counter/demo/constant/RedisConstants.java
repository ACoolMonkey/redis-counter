package com.hys.redis.counter.demo.constant;

/**
 * Redis常量类
 *
 * @author Robert Hou
 * @date 2020年05月01日 04:17
 **/
public class RedisConstants {

    private RedisConstants() {
    }

    /**
     * 累计用户人数key
     */
    public static final String TOTAL_KEY = "totalKey";
    /**
     * 日均活跃用户人数key
     */
    public static final String ACTIVE_KEY = "activeKey:";
}
