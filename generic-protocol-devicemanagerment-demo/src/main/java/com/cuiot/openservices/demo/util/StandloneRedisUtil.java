package com.cuiot.openservices.demo.util;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class StandloneRedisUtil {

    private volatile static JedisPool jedisPool = null;

    public static JedisPool getJedisCluster(String redisServer, String pd) {
        if (jedisPool == null) {
            synchronized(StandloneRedisUtil.class) {
                if (jedisPool == null) {
                    JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
                    jedisPoolConfig.setMaxIdle(20);
                    jedisPoolConfig.setMaxWaitMillis(3000L);

                    String[] ipPort = redisServer.split(":");
                    jedisPool = new JedisPool(jedisPoolConfig, ipPort[0], Integer.parseInt(ipPort[1]), 3000, pd.length() == 0 ? null : pd);
                }
            }
        }

        return jedisPool;
    }
}
