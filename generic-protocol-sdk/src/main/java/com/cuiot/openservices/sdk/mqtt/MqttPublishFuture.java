package com.cuiot.openservices.sdk.mqtt;

import com.cuiot.openservices.sdk.mqtt.result.MqttPublishResult;
import io.netty.util.concurrent.Future;

/**
 * @author unicom
 */
public interface MqttPublishFuture extends Future<MqttPublishResult> {
    /**
     * mqtt message
     *
     * @return MqttArticle
     */
    MqttArticle article();

    /**
     * mqtt: duplicate
     *
     * @return boolean
     */
    boolean isDuplicate();

    /**
     * mqtt: packet identifier
     *
     * @return int
     */
    int packetId();
}
