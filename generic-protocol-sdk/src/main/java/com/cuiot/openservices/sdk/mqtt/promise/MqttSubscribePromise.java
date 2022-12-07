package com.cuiot.openservices.sdk.mqtt.promise;

import com.cuiot.openservices.sdk.mqtt.MqttSubscribe;
import com.cuiot.openservices.sdk.mqtt.MqttSubscribeFuture;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.Timeout;
import io.netty.util.concurrent.EventExecutor;

import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author unicom
 */
public class MqttSubscribePromise extends MqttPromise<MqttQoS[]> implements MqttSubscribeFuture {

    private final List<MqttSubscribe> subscriptions;
    private Integer packetId;
    private MqttQoS qosLevel;

    private int successes = -1;
    /**
     * 订阅主题的qos是否被mqtt broker降级
     */
    private int downgrades = -1;
    private int failNum = -1;

    public MqttSubscribePromise(EventExecutor executor, List<MqttSubscribe> subscriptions) {
        super(executor);
        this.subscriptions = subscriptions;
    }

    public Integer getPacketId() {
        return packetId;
    }

    public void setPacketId(Integer packetId) {
        this.packetId = packetId;
    }

    public MqttQoS getQosLevel() {
        return qosLevel;
    }

    public void setQosLevel(MqttQoS qosLevel) {
        this.qosLevel = qosLevel;
    }

    @Override
    public final MqttMessageType messageType() {
        return MqttMessageType.SUBSCRIBE;
    }

    @Override
    public List<MqttSubscribe> subscriptions() {
        return subscriptions;
    }

    @Override
    public boolean isAllSuccess() {
        if (successes < 0 && isDone()) {
            successes = downgrades = 0;
            if (isSuccess()) {
                final List<MqttSubscribe> requests = subscriptions();
                final MqttQoS[] results = getNow();
                final int size = Math.min(requests.size(), results.length);
                for (int index = 0; index < size; index++) {
                    MqttQoS granted = results[index];
                    if (granted == MqttQoS.FAILURE) {
                        continue;
                    }
                    MqttQoS request = requests.get(index).qos();
                    if (granted.compareTo(request) < 0) {
                        downgrades++;
                    }
                    successes++;
                }
            }
        }
        return successes == subscriptions().size();
    }

    @Override
    public boolean isCompleteSuccess() {
        return isAllSuccess() && downgrades == 0;
    }

    @Override
    public void run(Timeout timeout) {
        tryFailure(new TimeoutException("No response message: expected=SUBACK"));
    }

    @Override
    public String toString() {
        return "MqttSubscribePromise{" +
                "subscriptions=" + subscriptions +
                ", packetId=" + packetId +
                ", qosLevel=" + qosLevel +
                ", successes=" + successes +
                ", downgrades=" + downgrades +
                ", failNum=" + failNum +
                '}';
    }
}
