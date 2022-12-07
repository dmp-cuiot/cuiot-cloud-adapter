package com.cuiot.openservices.sdk.entity.shadow;

/**
 * 设备影子
 *
 * @author yht
 */
public class DeviceShadow {

    /**
     * 设备影子的上报属性值和期望属性值数据
     */
    private State state;

    /**
     * 设备影子的元数据的信息
     */
    private Metadata metadata;

    /**
     * 时间戳
     */
    private String ts;

    /**
     * 设备影子json文档的版本号
     */
    private Integer version;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
