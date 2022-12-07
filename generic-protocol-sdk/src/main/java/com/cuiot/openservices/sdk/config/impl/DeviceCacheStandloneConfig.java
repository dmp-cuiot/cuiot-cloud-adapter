package com.cuiot.openservices.sdk.config.impl;

import com.alibaba.fastjson.JSONObject;
import com.cuiot.openservices.sdk.config.IDeviceConfig;
import com.cuiot.openservices.sdk.entity.Device;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

public class DeviceCacheStandloneConfig implements IDeviceConfig{
    private final ReentrantLock lock = new ReentrantLock();
    private JedisPool jedisPool;
    private final ConcurrentMap<String, String> originalIdentityCache = new ConcurrentHashMap<>(1000);
    private Logger logger = LoggerFactory.getLogger(DeviceFileConfig.class);

    public DeviceCacheStandloneConfig(JedisPool jedisPool){
        this.jedisPool = jedisPool;
    }

    @Override
    public Device getDeviceEntity(String originalIdentity) {
        Jedis jedis = null;
        try{
            if(jedisPool != null){
                jedis = jedisPool.getResource();
                String deviceInfo = jedis.hget("all_device_ref",originalIdentity);
                if(StringUtils.isNotBlank(deviceInfo)){
                    Device device = JSONObject.parseObject(deviceInfo,Device.class);
                    originalIdentityCache.put(device.getProductKey() + "-" + device.getDeviceKey(), originalIdentity);
                    return device;
                }
            }
        }catch(Exception e){
            logger.error("get device entity exception", e);
        }finally {
            if(jedis != null){
                jedis.close();
            }
        }
        return null;
    }

    @Override
    public List<Device> getAllDevices() {
        List<Device> devices = new ArrayList<>();
        Jedis jedis = null;
        try{
            if(jedisPool != null){
                jedis = jedisPool.getResource();
                List<String> deviceInfo = jedis.hvals("all_device_ref");
                for(int i=0,len= deviceInfo.size();i<len;i++){
                    Device device = JSONObject.parseObject(deviceInfo.get(i),Device.class);
                    devices.add(device);
                }
            }
        }catch(Exception e){
            logger.error("get all devices exception {}", e);
        }finally {
            if(jedis != null){
                jedis.close();
            }
        }
        return devices;
    }

    @Override
    public String getOriginalIdentity(String productKey, String deviceKey) {
        String originalIdentity = "";
        Jedis jedis = null;
        try {
            if(jedisPool != null){
                jedis = jedisPool.getResource();
                String deviceInfo = jedis.hget("platform_device_ref",productKey+"-"+deviceKey);
                Device device = JSONObject.parseObject(deviceInfo,Device.class);
                originalIdentity = device.getOriginalIdentity();
            }
        }catch(Exception e){
            logger.error("get originalIdentity exception {}", e);
        }finally {
            if(jedis != null){
                jedis.close();
            }
        }
        return originalIdentity;
    }

    @Override
    public boolean setDeviceEntity(Device device) {
        Jedis jedis = null;
        try{
            if(jedisPool != null){
                jedis = jedisPool.getResource();
                jedis.hset("all_device_ref",device.getOriginalIdentity(),JSONObject.toJSONString(device));
                jedis.hset("platform_device_ref",device.getProductKey()+"-"+device.getDeviceKey(),JSONObject.toJSONString(device));
                return true;
            }
        }catch(Exception e){
            logger.error("set config cache by entity exception", e);
        }finally {
            if(jedis != null){
                jedis.close();
            }
        }
        return false;
    }

    @Override
    public boolean setDeviceEntitys(List<Device> devices) {
        Jedis jedis = null;
        try{
            if(jedisPool != null){
                jedis = jedisPool.getResource();
                for(int i=0,len=devices.size();i<len;i++){
                    jedis.hset("all_device_ref",devices.get(i).getOriginalIdentity(),JSONObject.toJSONString(devices.get(i)));
                    jedis.hset("platform_device_ref",devices.get(i).getProductKey()+"-"+devices.get(i).getDeviceKey(),JSONObject.toJSONString(devices.get(i)));
                }
                return true;
            }
        }catch(Exception e){
            logger.error("set config cache by entity exception", e);
        }finally {
            if(jedis != null){
                jedis.close();
            }
        }
        return false;
    }

    @Override
    public boolean deleteDeviceEntity(String originalIdentity, String productKey, String deviceKey) {
        Jedis jedis = null;
        try{
            if(jedisPool != null){
                jedis = jedisPool.getResource();
                jedis.hdel("all_device_ref",originalIdentity);
                jedis.hdel("platform_device_ref",productKey+"-"+deviceKey);
                return true;
            }
        }catch(Exception e){
            logger.error("delete config entity cache exception {}", e);
        }finally {
            if(jedis != null){
                jedis.close();
            }
        }
        return false;
    }
}
