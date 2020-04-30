package com.hys.redis.counter.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RedisCounterDemoApplicationTests {

    @Autowired
    private Counter counter;

    @Test
    void counterTest() {
        //这里假设当前日期为2020年5月1日，测试的时候需要更改为当前日期的前几天
        for (int i = 6; i < 15; i++) {
            counter.updateUser(i, "20200501");
        }
        for (int i = 0; i < 15; i++) {
            counter.updateUser(i, "20200430");
        }
        System.out.println("累计用户数：" + counter.getTotalUserCount());
        System.out.println("两天内的活跃人数：" + counter.getActiveUserCount(2));
    }
}
