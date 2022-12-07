package com.cuiot.openservices.sdk.entity;

/**
 * 消息类型
 *
 * @author unicom
 */
public enum MessageType {
    /**
     * 直连设备、网关上线
     */
    DEVICE_LOGIN,
    /**
     * 直连设备、网关下线
     */
    DEVICE_LOGOUT,
    /**
     * 子设备上线
     */
    SUB_DEVICE_LOGIN,
    /**
     * 子设备下线
     */
    SUB_DEVICE_LOGOUT,

    /**
     * 子设备批量上线
     */
    SUB_DEVICE_BATCH_LOGIN,
    /**
     * 子设备批量下线
     */
    SUB_DEVICE_BATCH_LOGOUT,

    /**
     * 属性上报
     */
    PROPERTY_PUB,
    PROPERTY_BATCH,
    /**
     * 事件上报
     */
    EVENT_PUB,
    EVENT_BATCH,
    /**
     * 子设备属性事件上报
     */
    PROPERTY_EVENT_PUB,

    /**
     * 子设备添加拓扑
     */
    SUB_DEVICE_ADD_TOPO,

    /**
     * 子设备删除拓扑
     */
    SUB_DEVICE_DELETE_TOPO,
    /**
     * 设置属性reply
     */
    PROPERTY_SET_REPLY,
    /**
     * 同步服务调用reply
     */
    SYNC_PUB_REPLY,
    /**
     * 异步服务调用reply
     */
    SERVICE_PUB_REPLY,
    /**
     * 设备影子：获取
     */
    DEVICE_SHADOW_GET,
    /**
     * 设备影子：响应平台下发设备影子
     */
    DEVICE_SHADOW_COMMAND_REPLY,
    /**
     * 设备影子：更新
     */
    DEVICE_SHADOW_UPDATE,
}
