package com.cuiot.openservices.sdk.mqtt.promise;

/**
 * @author unicom
 */
public class MqttConnect {
    private byte[] clientId;
    private String username;
    private String password;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
