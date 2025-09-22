package com.hp.imagebackend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RedisStringTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate; // 注入Redis操作模板

    @Test
    public void testRedisStringOperations() {
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue(); // 获取字符串操作对象
        String key = "testKey";
        String value = "testValue";

        // 增
        valueOps.set(key, value);
        String storedValue = valueOps.get(key);
        assertEquals(value, storedValue, "存储的值与预期不一致");

        // 改
        String updatedValue = "updatedValue";
        valueOps.set(key, updatedValue);
        storedValue = valueOps.get(key);
        assertEquals(updatedValue, storedValue, "更新后的值与预期不一致");

        // 查
        storedValue = valueOps.get(key);
        assertNotNull(storedValue, "查询的值为空");
        assertEquals(updatedValue, storedValue, "查询的值与预期不一致");

        // 删
        stringRedisTemplate.delete(key);
        storedValue = valueOps.get(key);
        assertNull(storedValue, "删除后的值不为空");
    }
}