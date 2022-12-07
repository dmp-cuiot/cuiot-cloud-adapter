package com.cuiot.openservices.sdk.util;

import com.cuiot.openservices.sdk.entity.Device;
import com.cuiot.openservices.sdk.entity.SubscribeTopic;
import io.netty.handler.codec.mqtt.MqttQoS;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Topic工具类
 *
 * @author yht
 */
public final class TopicUtils {

    public final static String SPLIT_STR = "/";

    //-----------------------------------------TOPIC-------------------------------------------
    /**
     * 直连设备：上下线
     */
    public final static String DEVICE_LOGIN_TOPIC_FORMAT = "$proxy/%s/%s/%s/device/login";
    public final static String DEVICE_LOGOUT_TOPIC_FORMAT = "$proxy/%s/%s/%s/device/logout";
    /**
     * 子设备：上下线
     */
    public final static String SUB_DEVICE_LOGIN_TOPIC_FORMAT = "$proxy/%s/%s/%s/subdevice/login";
    public final static String SUB_DEVICE_LOGOUT_TOPIC_FORMAT = "$proxy/%s/%s/%s/subdevice/logout";
    /**
     * 子设备：批量上下线
     */
    public final static String SUB_DEVICE_BATCH_LOGIN_TOPIC_FORMAT = "$proxy/%s/%s/%s/subdevice/batchlogin";
    public final static String SUB_DEVICE_BATCH_LOGOUT_TOPIC_FORMAT = "$proxy/%s/%s/%s/subdevice/batchlogout";
    /**
     * 直连设备：上下行数据
     */
    public final static String PROPERTY_PUB_TOPIC_FORMAT = "$proxy/%s/%s/%s/property/pub";
    public final static String PROPERTY_BATCH_TOPIC_FORMAT = "$proxy/%s/%s/%s/property/batch";
    public final static String EVENT_PUB_TOPIC_FORMAT = "$proxy/%s/%s/%s/event/pub";
    public final static String EVENT_BATCH_TOPIC_FORMAT = "$proxy/%s/%s/%s/event/batch";
    public final static String PROPERTY_SET_REPLY_TOPIC_FORMAT = "$proxy/%s/%s/%s/property/set_reply";
    public final static String SYNC_PUB_REPLY_TOPIC_FORMAT = "$proxy/%s/%s/%s/sync/pub_reply";
    public final static String SERVICE_PUB_REPLY_TOPIC_FORMAT = "$proxy/%s/%s/%s/service/pub_reply";
    public final static String DEVICE_SHADOW_GET_TOPIC_FORMAT = "$proxy/%s/%s/%s/deviceShadow/get";
    public final static String DEVICE_SHADOW_COMMAND_REPLY_TOPIC_FORMAT = "$proxy/%s/%s/%s/deviceShadow/command_reply";
    public final static String DEVICE_SHADOW_UPDATE_TOPIC_FORMAT = "$proxy/%s/%s/%s/deviceShadow/update";
    /**
     * 子设备相关
     */
    public final static String SUB_PROPERTY_EVENT_TOPIC_FORMAT = "$proxy/%s/%s/%s/subdevice/propertyEvent/pub";
    public final static String SUB_TOPO_ADD_TOPIC_FORMAT = "$proxy/%s/%s/%s/topo/add";
    public final static String SUB_TOPO_DELETE_TOPIC_FORMAT = "$proxy/%s/%s/%s/topo/delete";


    //-----------------------------------------TOPIC POSTFIX-------------------------------------------
    /**
     * 上下线响应
     */
    public final static String DEVICE_LOGIN_REPLY = "/device/login_reply";
    public final static String DEVICE_LOGOUT_REPLY = "/device/logout_reply";
    public final static String SUB_DEVICE_LOGIN_REPLY = "/subdevice/login_reply";
    public final static String SUB_DEVICE_LOGOUT_REPLY = "/subdevice/logout_reply";
    public final static String SUB_DEVICE_BATCH_LOGIN_REPLY = "/subdevice/batchlogin_reply";
    public final static String SUB_DEVICE_BATCH_LOGOUT_REPLY = "/subdevice/batchlogout_reply";
    /**
     * 属性上报、事件上报响应
     */
    public final static String PROPERTY_PUB_REPLY = "/property/pub_reply";
    public final static String EVENT_PUB_REPLY = "/event/pub_reply";
    /**
     * 设置属性
     */
    public final static String PROPERTY_SET = "/property/set";
    /**
     * 服务调用
     */
    public final static String SYNC_PUB = "/sync/pub";
    public final static String SERVICE_PUB = "/service/pub";

    /**
     * 服务调用
     */
    public final static String SUB_DEVICE_TOPO_ADD_REPLY = "/topo/add_reply";
    public final static String SUB_DEVICE_TOPO_DELETE_REPLY = "/topo/delete_reply";
    /**
     * 设备影子
     */
    public final static String DEVICE_SHADOW_GET_REPLY = "/deviceShadow/get_reply";
    public final static String DEVICE_SHADOW_COMMAND = "/deviceShadow/command";
    public final static String DEVICE_SHADOW_UPDATE_REPLY = "/deviceShadow/update_reply";

    private static Pattern TOPIC_PATTERN = Pattern.compile("\\$proxy/(\\d+)/(\\w+)/(\\w+)/(\\S+)");

    /**
     * @param productKey 产品Key
     * @param deviceKey  设备Key
     * @return String
     */
    static String createDeviceLoginTopic(String serviceId, String productKey, String deviceKey) {
        return String.format(DEVICE_LOGIN_TOPIC_FORMAT, serviceId, productKey, deviceKey);
    }

    /**
     * @param productKey 产品Key
     * @param deviceKey  设备Key
     * @return String
     */
    static String createDeviceLogoutTopic(String serviceId, String productKey, String deviceKey) {
        return String.format(DEVICE_LOGOUT_TOPIC_FORMAT, serviceId, productKey, deviceKey);
    }

    /**
     * @param productKey 产品Key
     * @param deviceKey  设备Key
     * @return String
     */
    static String createPropertyPubTopic(String serviceId, String productKey, String deviceKey) {
        return String.format(PROPERTY_PUB_TOPIC_FORMAT, serviceId, productKey, deviceKey);
    }

    static String createEventPubTopic(String serviceId, String productKey, String deviceKey) {
        return String.format(EVENT_PUB_TOPIC_FORMAT, serviceId, productKey, deviceKey);
    }

    /**
     * @param productKey 产品Key
     * @param deviceKey  设备Key
     * @return String
     */
    static String createPropertySetReplyTopic(String serviceId, String productKey, String deviceKey) {
        return String.format(PROPERTY_SET_REPLY_TOPIC_FORMAT, serviceId, productKey, deviceKey);
    }

    /**
     * @param topic topic
     * @return String[]
     */
    public static String[] splitTopic(String topic) {
        return topic.split(SPLIT_STR);
    }

    public static String getPostfixFromTopic(String topic) {
        Matcher matcher = TOPIC_PATTERN.matcher(topic);
        if (matcher.matches()) {
            String postfix = matcher.group(4);
            return SPLIT_STR + postfix;
        }
        return null;
    }

    public static String createSubDevicePropertyEventBatch(String serviceId, String productKey, String deviceKey) {
        return String.format(SUB_PROPERTY_EVENT_TOPIC_FORMAT, serviceId, productKey, deviceKey);
    }

    /**
     * 创建泛协议服务指定设备可以订阅的所有主题
     *
     * @param serviceId 泛协议服务ID
     * @return List
     */
    public static List<SubscribeTopic> createSubscribeList(String serviceId, Device device) {
        List<SubscribeTopic> topics = new ArrayList<>();
        // 直连设备：上下线
        topics.add(new SubscribeTopic(String.format("$proxy/%s/%s/%s/device/login_reply", serviceId,
                device.getProductKey(), device.getDeviceKey())));
        topics.add(new SubscribeTopic(String.format("$proxy/%s/%s/%s/device/logout_reply", serviceId,
                device.getProductKey(), device.getDeviceKey())));
        ;
        // 直连设备：上下行数据
        topics.add(new SubscribeTopic(String.format("$proxy/%s/%s/%s/property/pub_reply", serviceId,
                device.getProductKey(), device.getDeviceKey())));
        topics.add(new SubscribeTopic(String.format("$proxy/%s/%s/%s/property/batch_reply", serviceId,
                device.getProductKey(), device.getDeviceKey())));
        topics.add(new SubscribeTopic(String.format("$proxy/%s/%s/%s/event/pub_reply", serviceId,
                device.getProductKey(), device.getDeviceKey())));
        topics.add(new SubscribeTopic(String.format("$proxy/%s/%s/%s/event/batch_reply", serviceId,
                device.getProductKey(), device.getDeviceKey())));
        topics.add(new SubscribeTopic(String.format("$proxy/%s/%s/%s/property/set", serviceId,
                device.getProductKey(), device.getDeviceKey())));
        topics.add(new SubscribeTopic(String.format("$proxy/%s/%s/%s/sync/pub", serviceId
                , device.getProductKey(), device.getDeviceKey()), MqttQoS.AT_LEAST_ONCE));
        topics.add(new SubscribeTopic(String.format("$proxy/%s/%s/%s/service/pub", serviceId
                , device.getProductKey(), device.getDeviceKey())));
        // 子设备：上下线
        topics.add(new SubscribeTopic(String.format("$proxy/%s/%s/%s/subdevice/login_reply", serviceId
                , device.getProductKey(), device.getDeviceKey())));
        topics.add(new SubscribeTopic(String.format("$proxy/%s/%s/%s/subdevice/logout_reply", serviceId
                , device.getProductKey(), device.getDeviceKey())));
        topics.add(new SubscribeTopic(String.format("$proxy/%s/%s/%s/subdevice/batchlogin_reply", serviceId
                , device.getProductKey(), device.getDeviceKey())));
        topics.add(new SubscribeTopic(String.format("$proxy/%s/%s/%s/subdevice/batchlogout_reply", serviceId
                , device.getProductKey(), device.getDeviceKey())));
        // 子设备：上下行数据
        topics.add(new SubscribeTopic(String.format("$proxy/%s/%s/%s/subdevice/propertyEvent/pub_reply", serviceId
                , device.getProductKey(), device.getDeviceKey())));
        // 子设备，修改拓扑关系
        topics.add(new SubscribeTopic(String.format("$proxy/%s/%s/%s/topo/add_reply", serviceId
                , device.getProductKey(), device.getDeviceKey())));
        topics.add(new SubscribeTopic(String.format("$proxy/%s/%s/%s/topo/delete_reply", serviceId
                , device.getProductKey(), device.getDeviceKey())));

        // 直连设备：设备影子
        topics.add(new SubscribeTopic(String.format("$proxy/%s/%s/%s/deviceShadow/get_reply", serviceId
                , device.getProductKey(), device.getDeviceKey())));
        topics.add(new SubscribeTopic(String.format("$proxy/%s/%s/%s/deviceShadow/command", serviceId
                , device.getProductKey(), device.getDeviceKey())));
        topics.add(new SubscribeTopic(String.format("$proxy/%s/%s/%s/deviceShadow/update_reply", serviceId
                , device.getProductKey(), device.getDeviceKey())));
        return topics;
    }

    /**
     * 创建泛协议服务指定设备可以取消订阅的所有主题
     *
     * @param serviceId 泛协议服务ID
     * @return List
     */
    public static List<String> createUnSubscribeList(String serviceId, Device device) {
        List<String> topics = new ArrayList<>();
        // 直连设备：上下线
        topics.add(String.format("$proxy/%s/%s/%s/device/login_reply", serviceId,
                device.getProductKey(), device.getDeviceKey()));
        topics.add(String.format("$proxy/%s/%s/%s/device/logout_reply", serviceId,
                device.getProductKey(), device.getDeviceKey()));
        ;
        // 直连设备：上下行数据
        topics.add(String.format("$proxy/%s/%s/%s/property/pub_reply", serviceId,
                device.getProductKey(), device.getDeviceKey()));
        topics.add(String.format("$proxy/%s/%s/%s/property/batch_reply", serviceId,
                device.getProductKey(), device.getDeviceKey()));
        topics.add(String.format("$proxy/%s/%s/%s/event/pub_reply", serviceId,
                device.getProductKey(), device.getDeviceKey()));
        topics.add(String.format("$proxy/%s/%s/%s/event/batch_reply", serviceId,
                device.getProductKey(), device.getDeviceKey()));
        topics.add(String.format("$proxy/%s/%s/%s/property/set", serviceId,
                device.getProductKey(), device.getDeviceKey()));
        topics.add(String.format("$proxy/%s/%s/%s/sync/pub", serviceId
                , device.getProductKey(), device.getDeviceKey()));
        topics.add(String.format("$proxy/%s/%s/%s/service/pub", serviceId
                , device.getProductKey(), device.getDeviceKey()));
        // 子设备：上下线
        topics.add(String.format("$proxy/%s/%s/%s/subdevice/login_reply", serviceId
                , device.getProductKey(), device.getDeviceKey()));
        topics.add(String.format("$proxy/%s/%s/%s/subdevice/logout_reply", serviceId
                , device.getProductKey(), device.getDeviceKey()));
        topics.add(String.format("$proxy/%s/%s/%s/subdevice/batchlogin_reply", serviceId
                , device.getProductKey(), device.getDeviceKey()));
        topics.add(String.format("$proxy/%s/%s/%s/subdevice/batchlogout_reply", serviceId
                , device.getProductKey(), device.getDeviceKey()));
        // 子设备：上下行数据
        topics.add(String.format("$proxy/%s/%s/%s/subdevice/propertyEvent/pub_reply", serviceId
                , device.getProductKey(), device.getDeviceKey()));
        // 子设备，修改拓扑关系
        topics.add(String.format("$proxy/%s/%s/%s/topo/add_reply", serviceId
                , device.getProductKey(), device.getDeviceKey()));
        topics.add(String.format("$proxy/%s/%s/%s/topo/delete_reply", serviceId
                , device.getProductKey(), device.getDeviceKey()));
        // 直连设备：设备影子
        topics.add(String.format("$proxy/%s/%s/%s/deviceShadow/get_reply", serviceId
                , device.getProductKey(), device.getDeviceKey()));
        topics.add(String.format("$proxy/%s/%s/%s/deviceShadow/command", serviceId
                , device.getProductKey(), device.getDeviceKey()));
        topics.add(String.format("$proxy/%s/%s/%s/deviceShadow/update_reply", serviceId
                , device.getProductKey(), device.getDeviceKey()));
        return topics;
    }
}
