package com.mpos.lottery.te.test;

import java.util.Set;

import redis.clients.jedis.Jedis;

public class JedisClient {

  public static void main(String args[]) {
    Jedis jedis = new Jedis("192.168.2.158", 6379);
    jedis.set("foo", "bar");
    String value = jedis.get("foo");
    System.out.println("Get by key['foo'], value[" + value + "]");
    Set<String> keys = jedis.keys("*");
    for (String key : keys) {
      jedis.del(key);
    }
  }
}
