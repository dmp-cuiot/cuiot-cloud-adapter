package com.cuiot.openservices.sdk.mqtt.promise;

import com.cuiot.openservices.sdk.mqtt.MqttArticle;
import com.cuiot.openservices.sdk.mqtt.MqttPublishFuture;
import com.cuiot.openservices.sdk.mqtt.result.MqttPublishResult;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.util.Timeout;
import io.netty.util.concurrent.EventExecutor;

import java.util.concurrent.TimeoutException;

/**
 * @author unicom
 */
public class MqttPublishPromise extends MqttPromise<MqttPublishResult> implements MqttPublishFuture {

    private final MqttArticle article;
    private final boolean duplicate;
    private int packetId;

    public MqttPublishPromise(EventExecutor executor, MqttArticle article, int packetId) {
        super(executor);
        this.article = article;
        this.duplicate = packetId > 0;
        this.packetId = packetId;
    }

    @Override
    public final MqttMessageType messageType() {
        return MqttMessageType.PUBLISH;
    }

    @Override
    public MqttArticle article() {
        return article;
    }

    @Override
    public int packetId() {
        return packetId;
    }

    public void packetId(int packetId) {
        this.packetId = packetId;
    }

    @Override
    public boolean isDuplicate() {
        return duplicate;
    }

    @Override
    public void run(Timeout timeout) {
        switch (article.qos()) {
            case AT_LEAST_ONCE:
                tryFailure(new TimeoutException("No response message: expected=PUBACK"));
                break;
            case EXACTLY_ONCE:
                tryFailure(new TimeoutException("No response message: expected=PUBREC"));
                break;
            default:
                tryFailure(new TimeoutException("Incomplete write message"));
                break;
        }
    }
}
