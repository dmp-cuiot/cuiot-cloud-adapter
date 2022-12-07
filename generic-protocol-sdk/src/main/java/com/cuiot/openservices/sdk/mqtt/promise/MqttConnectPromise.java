package com.cuiot.openservices.sdk.mqtt.promise;

import com.cuiot.openservices.sdk.mqtt.result.MqttConnectResult;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.util.Timeout;
import io.netty.util.concurrent.EventExecutor;

import java.util.concurrent.TimeoutException;

/**
 * @author unicom
 */
public class MqttConnectPromise extends MqttPromise<MqttConnectResult> {

    private byte[] clientId;
    private String username;
    private byte[] password;

    public MqttConnectPromise(MqttConnect connect, EventExecutor executor) {
        super(executor);
        this.clientId = connect.getClientId();
        this.username = connect.getUsername();
        this.password = connect.getPassword() == null ? null : connect.getPassword().getBytes();
    }

    public String clientId() {
        return new String(clientId);
    }

    public String username() {
        return username;
    }

    public byte[] password() {
        return password;
    }

    @Override
    public final MqttMessageType messageType() {
        return MqttMessageType.CONNECT;
    }

    @Override
    public void run(Timeout timeout) {
        tryFailure(new TimeoutException("No response message: expected=CONNACK"));
    }

    public byte[] getClientId() {
        return clientId;
    }

    public void setClientId(byte[] clientId) {
        this.clientId = clientId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
    }
}
