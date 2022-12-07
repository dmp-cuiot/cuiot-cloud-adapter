package com.cuiot.openservices.sdk.mqtt;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author unicom
 */
public final class MqttPacketId implements Serializable {

    private static final long serialVersionUID = -1608711409506773103L;

    public static final int MIN_VALUE = 1;
    public static final int MAX_VALUE = 65535;

    private final AtomicInteger id;

    public MqttPacketId() {
        this(MIN_VALUE);
    }

    public MqttPacketId(int initialValue) {
        this.id = new AtomicInteger(requireValidPacketId(initialValue, "initialValue"));
    }

    public int get() {
        return id.get();
    }

    public int getAndIncrement() {
        int prev;
        int next;
        do {
            next = (prev = id.get()) >= MAX_VALUE ? MIN_VALUE : prev + 1;
        } while (!id.compareAndSet(prev, next));
        return prev;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    public static boolean isValidPacketId(int id) {
        return id >= MIN_VALUE && id <= MAX_VALUE;
    }

    public static int requireValidPacketId(int id, String name) throws IllegalArgumentException {
        if (!isValidPacketId(id)) {
            throw new IllegalArgumentException(name + ": " + id + " (expected: 1–65535)");
        }
        return id;
    }
}
