package com.cuiot.openservices.sdk.mqtt.promise;

import com.cuiot.openservices.sdk.mqtt.result.MqttPingResult;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.util.Timeout;
import io.netty.util.concurrent.EventExecutor;

import java.util.concurrent.TimeoutException;

/**
 * @author unicom
 */
public class MqttPingPromise extends MqttPromise<MqttPingResult> {

    public MqttPingPromise(EventExecutor executor) {
        super(executor);
    }

    @Override
    public final MqttMessageType messageType() {
        return MqttMessageType.PINGREQ;
    }

    @Override
    public void run(Timeout timeout) {
        tryFailure(new TimeoutException("No response message: expected=PINGRESP"));
    }
}
