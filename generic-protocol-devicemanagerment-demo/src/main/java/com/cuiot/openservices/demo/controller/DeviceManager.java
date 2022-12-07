package com.cuiot.openservices.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.cuiot.openservices.demo.config.ConfigLoadFactory;
import com.cuiot.openservices.demo.dto.*;
import com.cuiot.openservices.demo.util.ClusterRedisUtil;
import com.cuiot.openservices.demo.util.HttpClientUtil;
import com.cuiot.openservices.sdk.config.IAdapterConfig;
import com.cuiot.openservices.sdk.config.IDeviceConfig;
import com.cuiot.openservices.sdk.config.impl.AdapterFileConfig;
import com.cuiot.openservices.sdk.entity.Device;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.JedisCluster;

import java.util.*;

/**
 * 设备管理：创建设备、删除设备
 *
 * @author hqw
 */
@RestController
@RequestMapping("/device")
public class DeviceManager {

    @Autowired
    private IAdapterConfig adapterConfig;

    @Value("${cloud.platform.server}")
    private String platformServer;

    @Value("${cloud.platform.apiUrl.addDevice}")
    private String addUrl;

    @Value("${cloud.platform.apiUrl.deleteDevice}")
    private String deleteUrl;

    @Value("${cloud.platform.apiUrl.batchAddDevice}")
    private String batchAddUrl;

    @Value("${cloud.platform.apiUrl.batchDeleteDevice}")
    private String batchDeleteUrl;

    @Value("${cloud.platform.apiUrl.getDevice}")
    private String getDeviceUrl;

    private String appId;
    private String timestamp;
    private String transId;
    private String token;

    //刷新token等鉴权信息的入口，测试使用，具体获取方式，客户根据实际情况获取
    @PostMapping(value = "/refreshToken", produces = "application/json;charset=UTF-8")
    public void setTokenInfo(@RequestBody Map<String, String> param){
        appId = param.get("appId");
        timestamp = param.get("timestamp");
        transId = param.get("transId");
        token = param.get("token");
    }

    /**
     * 创建单个设备
     * @param param 入参格式如：{"productKey":"cu3eh2b6sx6u6dAV","deviceKey":"newdevicekey","deviceName":"newdeviceName","originalIdentity":"self-fanZLDev001","deviceId":"newdevicekey","signMethod":"0","authType":"0"}
     */
    @PostMapping(value = "/createSingleDevice", produces = "application/json;charset=UTF-8")
    public String createSingleDevice(@RequestBody Map<String, String> param) {
        //以下4个值由用户提供
        String appId = param.get("appId"); //用户可以通过自己的方式去获取，此处仅为示例
        String timestamp = param.get("timestamp"); //用户可以通过自己的方式去获取，此处仅为示例
        String transId = param.get("transId"); //用户可以通过自己的方式去获取，此处仅为示例
        String token = param.get("token"); //用户可以通过自己的方式去获取，此处仅为示例

        String url = platformServer + addUrl;
        Map<String, String> data = new HashMap();
        data.put("productKey",param.get("productKey"));
        data.put("deviceKey",param.get("deviceKey"));//deviceKey可以不设置，如果不设置，则云平台会设置一个随机字符串
        data.put("deviceName",param.get("deviceName"));//当deviceKey不设置时，deviceName也无需给定
        DataParam dataParam = DataParam.builder().appId(appId).timestamp(timestamp).transId(transId).token(token).data(data).build();
        try{
            String res = HttpClientUtil.doPostJson(url, JSONObject.toJSONString(dataParam));
            System.out.println(res);
            ResponseInfo result = JSONObject.parseObject(res,ResponseInfo.class);
            if(DemoConstant.SUCCESS_CODE.equals(result.getCode())){
                Device deviceInfo = result.getData();
                String deviceSecret = deviceInfo.getDeviceSecret();

                //自行组装设备信息相关内容,示例：
                Device device = new Device();
                device.setOriginalIdentity(param.get("originalIdentity"));
                device.setProductKey(param.get("productKey"));
                device.setDeviceKey(param.get("deviceKey"));
                device.setDeviceSecret(deviceSecret);
                device.setDeviceId(param.get("deviceId"));
                device.setSignMethod(param.get("signMethod"));
                device.setAuthType(param.get("authType"));

                //创建设备成功后，需要将用户侧的设备信息与平台侧设备关联关系配置存入至redis缓存中，sdk服务如选择动态配置加载方式后会读取缓存中的关联关系
                IDeviceConfig deviceConfig = ConfigLoadFactory.getConfigPattern(adapterConfig.getConfigType());
                deviceConfig.setDeviceEntity(device);
            }
            return res;
        }catch(Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }

    /**
     * 删除单个设备
     * @param param 入参格式如：{"productKey":"平台侧的产品key","deviceKey":"平台侧的设备key","originalIdentity":"用户侧的设备唯一标识"}
     */
    @PostMapping(value = "/deleteSingleDevice", produces = "application/json;charset=UTF-8")
    public String deleteSingleDevice(@RequestBody Map<String, String> param) {
        try{
            //以下4个值由用户提供
            String appId = param.get("appId"); //用户可以通过自己的方式去获取，此处仅为示例
            String timestamp = param.get("timestamp"); //用户可以通过自己的方式去获取，此处仅为示例
            String transId = param.get("transId"); //用户可以通过自己的方式去获取，此处仅为示例
            String token = param.get("token"); //用户可以通过自己的方式去获取，此处仅为示例

            String url = platformServer + deleteUrl;
            Map<String, String> data = new HashMap();
            data.put("productKey",param.get("productKey"));
            data.put("deviceKey",param.get("deviceKey"));
            DataParam dataParam = DataParam.builder().appId(appId).timestamp(timestamp).transId(transId).token(token).data(data).build();

            String res = HttpClientUtil.doPostJson(url, JSONObject.toJSONString(dataParam));
            System.out.println(res);
            ResponseInfo result = JSONObject.parseObject(res,ResponseInfo.class);
            if(DemoConstant.SUCCESS_CODE.equals(result.getCode())){
                //删除设备在配置库中的配置信息
                IDeviceConfig deviceConfig = ConfigLoadFactory.getConfigPattern(adapterConfig.getConfigType());
                deviceConfig.deleteDeviceEntity(param.get("originalIdentity"),param.get("productKey"),param.get("deviceKey"));
            }
            return res;
        }catch(Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }

    /**
     * 批量删除设备
     * @param param 入参格式如：{"productKey":"平台侧产品key","devices":[{“originalIdentity”:"用户侧设备唯一标识","deviceKey":"平台侧设备key"}]}
     */
    @PostMapping(value = "/batchDeleteDevice", produces = "application/json;charset=UTF-8")
    public String batchDeleteDevice(@RequestBody Map<String, Object> param){
        try{
            //以下4个值由用户提供
            String appId = param.get("appId").toString(); //用户可以通过自己的方式去获取，此处仅为示例
            String timestamp = param.get("timestamp").toString(); //用户可以通过自己的方式去获取，此处仅为示例
            String transId = param.get("transId").toString(); //用户可以通过自己的方式去获取，此处仅为示例
            String token = param.get("token").toString(); //用户可以通过自己的方式去获取，此处仅为示例

            String url = platformServer + batchDeleteUrl;

            Map<String, Object> data = new HashMap();
            data.put("productKey",param.get("productKey").toString());

            List<String> deviceKeys = new ArrayList<>();
            List<Map<String,String>> devices = (List<Map<String, String>>) param.get("devices");

            for(int i=0,len=devices.size();i<len;i++){
                deviceKeys.add(devices.get(i).get("deviceKey"));
            }
            data.put("deviceKey",deviceKeys);

            DataParam dataParam = DataParam.builder().appId(appId).timestamp(timestamp).transId(transId).token(token).data(data).build();
            String res = HttpClientUtil.doPostJson(url, JSONObject.toJSONString(dataParam));
            System.out.println(res);
            ResponseInfo result = JSONObject.parseObject(res,ResponseInfo.class);
            if(DemoConstant.SUCCESS_CODE.equals(result.getCode())){
                //删除设备在配置库中的配置信息
                IDeviceConfig deviceConfig = ConfigLoadFactory.getConfigPattern(adapterConfig.getConfigType());
                for(int i=0,len=devices.size();i<len;i++){
                    deviceConfig.deleteDeviceEntity(devices.get(i).get("originalIdentity"),param.get("productKey").toString(),devices.get(i).get("deviceKey"));
                }
            }
            return res;
        }catch(Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }

    /**
     * 在DMP平台侧创建设备后，通过规则引擎数据转发，将创建设备同步至用户侧，并将用户侧设备与平台侧设备关联关系存储至redis缓存
     * @param param
     */
    @PostMapping(value = "/syncDevice", produces = "application/json;charset=UTF-8")
    public void syncDevice(@RequestBody Map<String, String> param) {
        System.out.println(JSONObject.toJSONString(param));
        //以下4个值由用户提供
//        String appId = "760879521321713664"; //用户可以通过自己的方式去获取，此处仅为示例
//        String timestamp = "2021-12-31 10:46:52 508"; //用户可以通过自己的方式去获取，此处仅为示例
//        String transId = "20211231104652508116694"; //用户可以通过自己的方式去获取，此处仅为示例
//        String token = "3a2b398f89569786a4500e754ddf8116396862001fff1ca6915b55a145f10662"; //用户可以通过自己的方式去获取，此处仅为示例

        String lifeCycle = param.get("lifeCycle");
        String productKey = param.get("productKey");
        String deviceKey = param.get("deviceKey");
        Map<String, String> data = new HashMap();
        data.put("productKey",productKey);
        data.put("deviceKey",deviceKey);
        DataParam dataParam = DataParam.builder().appId(appId).timestamp(timestamp).transId(transId).token(token).data(data).build();
        try{
            IDeviceConfig deviceConfig = ConfigLoadFactory.getConfigPattern(adapterConfig.getConfigType());
            if(DemoConstant.DEVICE_ADD.equals(lifeCycle)){
                String res = HttpClientUtil.doPostJson(platformServer+getDeviceUrl, JSONObject.toJSONString(dataParam));
                System.out.println(res);
                ResponseInfo result = JSONObject.parseObject(res,ResponseInfo.class);
                if(DemoConstant.SUCCESS_CODE.equals(result.getCode())){
                    String deviceSecret = result.getData().getDeviceSecret();
                    //组装即将存储至redis缓存的用户侧与平台侧设备映射关系
                    Device device = new Device();
                    device.setOriginalIdentity(deviceKey);
                    device.setProductKey(productKey);
                    device.setDeviceKey(deviceKey);
                    device.setDeviceId(deviceKey);
                    device.setDeviceSecret(deviceSecret);
                    device.setSignMethod("0"); //由用户提供 设备签名方法，0：hmac_sha256  1：SM3
                    device.setAuthType("0"); //由用户提供 设备认证方式，0：一机一密  1：一型一密

                    //创建设备成功后，需要将用户侧的设备信息与平台侧设备关联关系配置存入至redis缓存中，sdk服务如选择动态配置加载方式后会读取缓存中的关联关系
                    deviceConfig.setDeviceEntity(device);
                }
            }else if(DemoConstant.DEVICE_DELETE.equals(lifeCycle)){
                //设备删除
                deviceConfig.deleteDeviceEntity(deviceKey,productKey,deviceKey);
                //具体逻辑，用户自行补充
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 查询所有设备信息
     * @return
     */
    @GetMapping(value = "/getAllDevices")
    public List<Device> getAllDevices(){
        IDeviceConfig deviceConfig = ConfigLoadFactory.getConfigPattern(adapterConfig.getConfigType());
        List<Device> devices = deviceConfig.getAllDevices();
        return devices;
    }

    @PostMapping(value = "/getDeviceEntity")
    public Device getDeviceEntity(@RequestBody Map<String, String> param){
        IDeviceConfig deviceConfig = ConfigLoadFactory.getConfigPattern(adapterConfig.getConfigType());
        return deviceConfig.getDeviceEntity(param.get("originalIdentity"));
    }
}
