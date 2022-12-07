package com.cuiot.openservices.demo.util;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.LinkedHashSet;
import java.util.Set;

public class ClusterRedisUtil {

    private static JedisCluster jedisCluster = null;

    public static JedisCluster getJedisCluster(String redisServer, String pd) {
        if (jedisCluster == null) {
            synchronized(ClusterRedisUtil.class) {
                if (jedisCluster == null) {
                    JedisPoolConfig config = new JedisPoolConfig();
                    config.setMaxTotal(20);
                    config.setMaxIdle(20);
                    config.setMaxWaitMillis(3000L);
                    config.setTestOnBorrow(true);
                    Set<HostAndPort> nodes = new LinkedHashSet();
                    String[] servers = redisServer.split(",");

                    for(int i = 0; i < servers.length; ++i) {
                        String[] ipPort = servers[i].split(":");
                        HostAndPort hostAndPort = new HostAndPort(ipPort[0], Integer.parseInt(ipPort[1]));
                        nodes.add(hostAndPort);
                    }

                    jedisCluster = new JedisCluster(nodes, 5000, 3000, 10, pd.length() == 0 ? null : pd, config);
                }
            }
        }

        return jedisCluster;
    }

}
