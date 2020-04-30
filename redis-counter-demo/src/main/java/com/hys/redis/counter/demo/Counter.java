package com.hys.redis.counter.demo;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.BitOP;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 统计累计和日均活跃用户人数
 *
 * @author Robert Hou
 * @date 2020年05月01日 03:50
 **/
public class Counter {


    /**
     * ip地址
     */
    private static final String IP_ADDRESS = "192.168.253.129";
    /**
     * 端口号
     */
    private static final int PORT = 6379;
    /**
     * jedis客户端
     */
    private Jedis jedis;
    /**
     * 累计用户人数key
     */
    private static final String TOTAL_KEY = "totalKey";
    /**
     * 日均活跃用户人数key
     */
    private static final String ACTIVE_KEY = "activeKey:";

    public Counter() {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(50);
        poolConfig.setMaxIdle(50);
        poolConfig.setMaxWaitMillis(1000);
        JedisPool jedisPool = new JedisPool(poolConfig, IP_ADDRESS, PORT);
        jedis = jedisPool.getResource();
    }

    /**
     * 更新累计和日均活跃用户人数
     *
     * @param userId 用户id
     * @param time   当前日期
     */
    private void updateUser(long userId, String time) {
        if (StringUtils.isBlank(time)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            time = sdf.format(new Date());
        }
        Pipeline pipeline = jedis.pipelined();
        pipeline.setbit(TOTAL_KEY, userId, true);
        pipeline.setbit(ACTIVE_KEY + time, userId, true);
        pipeline.syncAndReturnAll();
    }

    /**
     * 获取累计用户人数
     *
     * @return 累计用户人数
     */
    private Long getTotalUserCount() {
        Pipeline pipeline = jedis.pipelined();
        pipeline.bitcount(TOTAL_KEY);
        List<Object> totalKeyCountList = pipeline.syncAndReturnAll();
        return (Long) totalKeyCountList.get(0);
    }

    /**
     * 获取指定天数内的日均活跃人数
     *
     * @param dayNum 指定天数
     * @return
     */
    private Long getActiveUserCount(int dayNum) {
        if (dayNum < 1) {
            return (long) 0;
        }
        List<String> pastDaysKey = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dayNum; i++) {
            //保存距今dayNum天数的key的集合
            sb.append(ACTIVE_KEY).append(sdf.format(DateUtils.addDays(new Date(), -i)));
            pastDaysKey.add(sb.toString());
            sb.delete(0, sb.length());
        }
        if (pastDaysKey.isEmpty()) {
            return (long) 0;
        }
        String lastDaysKey = "last" + dayNum + "DaysActive";
        Pipeline pipeline = jedis.pipelined();
        pipeline.bitop(BitOP.AND, lastDaysKey, pastDaysKey.toArray(new String[pastDaysKey.size()]));
        pipeline.bitcount(lastDaysKey);
        //设置过期时间为5分钟
        pipeline.expire(lastDaysKey, 300);
        List<Object> activeKeyCountList = pipeline.syncAndReturnAll();
        return (Long) activeKeyCountList.get(1);
    }

    public static void main(String[] args) {
        Counter c = new Counter();
        //这里假设当前日期为2020年5月1日，测试的时候需要更改为当前日期的前几天
        for (int i = 6; i < 15; i++) {
            c.updateUser(i, "20200501");
        }
        for (int i = 0; i < 15; i++) {
            c.updateUser(i, "20200430");
        }
        System.out.println("累计用户数：" + c.getTotalUserCount());
        System.out.println("两天内的活跃人数：" + c.getActiveUserCount(2));
    }
}
