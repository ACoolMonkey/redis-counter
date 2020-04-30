package com.hys.redis.counter.demo;

import com.hys.redis.counter.demo.config.RedisConfig;
import com.hys.redis.counter.demo.constant.RedisConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 统计累计和日均活跃用户人数
 *
 * @author Robert Hou
 * @date 2020年05月01日 03:50
 **/
@Component
public class Counter {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedisConfig redisConfig;

    /**
     * 更新累计和日均活跃用户人数
     *
     * @param userId 用户id
     * @param time   当前日期
     */
    public void updateUser(long userId, String time) {
        if (StringUtils.isBlank(time)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            time = sdf.format(new Date());
        }
        redisTemplate.opsForValue().setBit(RedisConstants.TOTAL_KEY, userId, true);
        redisTemplate.opsForValue().setBit(RedisConstants.ACTIVE_KEY + time, userId, true);
    }

    /**
     * 获取累计用户人数
     *
     * @return 累计用户人数
     */
    public Long getTotalUserCount() {
        return redisConfig.bitCount(RedisConstants.TOTAL_KEY);
    }

    /**
     * 获取指定天数内的日均活跃人数
     *
     * @param dayNum 指定天数
     * @return 日均活跃人数
     */
    public Long getActiveUserCount(int dayNum) {
        if (dayNum < 1) {
            return (long) 0;
        }
        List<String> pastDaysKey = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dayNum; i++) {
            //保存距今dayNum天数的key的集合
            sb.append(RedisConstants.ACTIVE_KEY).append(sdf.format(DateTime.now().minusDays(i).toDate()));
            pastDaysKey.add(sb.toString());
            sb.delete(0, sb.length());
        }
        if (pastDaysKey.isEmpty()) {
            return (long) 0;
        }
        String lastDaysKey = "last" + dayNum + "DaysActive";
        redisConfig.bitOp(RedisStringCommands.BitOperation.AND, lastDaysKey, pastDaysKey);
        //设置过期时间为5分钟
        redisTemplate.expire(lastDaysKey, 5, TimeUnit.MINUTES);
        return redisConfig.bitCount(lastDaysKey);
    }
}
