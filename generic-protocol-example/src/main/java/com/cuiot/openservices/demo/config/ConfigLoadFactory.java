package com.cuiot.openservices.demo.config;

import com.cuiot.openservices.demo.util.ClusterRedisUtil;
import com.cuiot.openservices.demo.util.StandloneRedisUtil;
import com.cuiot.openservices.sdk.config.IAdapterConfig;
import com.cuiot.openservices.sdk.config.IDeviceConfig;
import com.cuiot.openservices.sdk.config.impl.AdapterFileConfig;
import com.cuiot.openservices.sdk.config.impl.DeviceCacheClusterConfig;
import com.cuiot.openservices.sdk.config.impl.DeviceCacheStandloneConfig;
import com.cuiot.openservices.sdk.config.impl.DeviceFileConfig;
import com.cuiot.openservices.sdk.constant.CommonConstant;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

public class ConfigLoadFactory {

    public static IDeviceConfig getConfigPattern(String type){
        IAdapterConfig adapterConfig = new AdapterFileConfig();
        if(CommonConstant.CONF_CONFIG.equals(type)){
            //读取配置文件的时候，param为String类型，可以不传，也可以传conf文件的path路径
            return new DeviceFileConfig();
        }else if(CommonConstant.STATNDLONE_REDIS_CONFIG.equals(type)){
            JedisPool jedisPool = StandloneRedisUtil.getJedisCluster(adapterConfig.getRedisServer(),adapterConfig.getRedisPd());
            //读取单机redis时，param为JedisPool，必传
            return new DeviceCacheStandloneConfig(jedisPool);
        }else if(CommonConstant.CLUSTER_REDIS_CONFIG.equals(type)){
            //读取redis cluster集群时，param为JedisCluster，必传
            JedisCluster jedisCluster = ClusterRedisUtil.getJedisCluster(adapterConfig.getRedisServer(),adapterConfig.getRedisPd());
            return new DeviceCacheClusterConfig(jedisCluster);
        }
        return new DeviceFileConfig();
    }
}
