package com.cuiot.openservices.sdk.config.impl;

import com.alibaba.fastjson.JSONObject;
import com.cuiot.openservices.sdk.config.IDeviceConfig;
import com.cuiot.openservices.sdk.entity.Device;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisCluster;
import java.util.ArrayList;
import java.util.List;

public class DeviceCacheClusterConfig implements IDeviceConfig{
    private JedisCluster jedisCluster;
    private Logger logger = LoggerFactory.getLogger(DeviceFileConfig.class);

    public DeviceCacheClusterConfig(JedisCluster jedisCluster){
        this.jedisCluster = jedisCluster;
    }

    @Override
    public Device getDeviceEntity(String originalIdentity) {
        if(jedisCluster != null){
            String deviceInfo = jedisCluster.hget("all_device_ref",originalIdentity);
            if(StringUtils.isNotBlank(deviceInfo)){
                Device device = JSONObject.parseObject(deviceInfo,Device.class);
                return device;
            }
        }
        return null;
    }

    @Override
    public List<Device> getAllDevices() {
        List<Device> devices = new ArrayList<>();
        try{
            if(jedisCluster != null){
                List<String> deviceInfo = jedisCluster.hvals("all_device_ref");
                for(int i=0,len= deviceInfo.size();i<len;i++){
                    Device device = JSONObject.parseObject(deviceInfo.get(i),Device.class);
                    devices.add(device);
                }
            }
        }catch(Exception e){
            logger.error("get all devices exception {}", e);
        }
        return devices;
    }

    @Override
    public String getOriginalIdentity(String productKey, String deviceKey) {
        String originalIdentity = "";
        try {
            if(jedisCluster != null){
                String deviceInfo = jedisCluster.hget("platform_device_ref",productKey+"-"+deviceKey);
                Device device = JSONObject.parseObject(deviceInfo,Device.class);
                originalIdentity = device.getOriginalIdentity();
            }
        }catch(Exception e){
            logger.error("get originalIdentity exception {}", e);
        }
        return originalIdentity;
    }

    @Override
    public boolean setDeviceEntity(Device device) {
        try{
            if(jedisCluster != null){
                jedisCluster.hset("all_device_ref",device.getOriginalIdentity(),JSONObject.toJSONString(device));
                jedisCluster.hset("platform_device_ref",device.getProductKey()+"-"+device.getDeviceKey(),JSONObject.toJSONString(device));
                return true;
            }
        }catch(Exception e){
            logger.error("set config cache by entity exception {}", e);
        }
        return false;
    }

    @Override
    public boolean setDeviceEntitys(List<Device> devices) {
        try{
            if(jedisCluster != null){
                for(int i=0,len=devices.size();i<len;i++){
                    jedisCluster.hset("all_device_ref",devices.get(i).getOriginalIdentity(),JSONObject.toJSONString(devices.get(i)));
                    jedisCluster.hset("platform_device_ref",devices.get(i).getProductKey()+"-"+devices.get(i).getDeviceKey(),JSONObject.toJSONString(devices.get(i)));
                }
                return true;
            }
        }catch(Exception e){
            logger.error("set config cache by entity exception {}", e);
        }
        return false;
    }

    @Override
    public boolean deleteDeviceEntity(String originalIdentity, String productKey, String deviceKey) {
        try{
            if(jedisCluster != null){
                jedisCluster.hdel("all_device_ref",originalIdentity);
                jedisCluster.hdel("platform_device_ref",productKey+"-"+deviceKey);
                return true;
            }
        }catch(Exception e){
            logger.error("delete config entity cache exception {}", e);
        }
        return false;
    }
}
