package com.hys.redis.counter.demo;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RedisCounterDemoApplicationTests {

    @Autowired
    private Counter counter;

    @Test
    void counterTest() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMdd");

        for (int i = 6; i < 15; i++) {
            counter.updateUser(i, DateTime.now().toString(dateTimeFormatter));
        }
        for (int i = 0; i < 15; i++) {
            counter.updateUser(i, DateTime.now().minusDays(1).toString(dateTimeFormatter));
        }
        System.out.println("累计用户数：" + counter.getTotalUserCount());
        System.out.println("两天内的活跃人数：" + counter.getActiveUserCount(2));
    }
}
