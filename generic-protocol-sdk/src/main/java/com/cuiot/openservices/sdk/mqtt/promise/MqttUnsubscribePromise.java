package com.cuiot.openservices.sdk.mqtt.promise;

import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.util.Timeout;
import io.netty.util.concurrent.EventExecutor;

import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author unicom
 */
public class MqttUnsubscribePromise extends MqttPromise<MqttUnsubAckMessage> {

    private final List<String> topicFilters;

    public MqttUnsubscribePromise(EventExecutor executor, List<String> topicFilters) {
        super(executor);
        this.topicFilters = topicFilters;
    }

    @Override
    public final MqttMessageType messageType() {
        return MqttMessageType.UNSUBSCRIBE;
    }

    public List<String> topicFilters() {
        return topicFilters;
    }

    @Override
    public void run(Timeout timeout) {
        tryFailure(new TimeoutException("No response message: expected=UNSUBACK"));
    }
}
