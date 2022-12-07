package com.cuiot.openservices.sdk.mqtt.promise;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;

import java.util.concurrent.ConcurrentMap;

/**
 * @author unicom
 */
public class PromiseRemover<V, P extends Promise<?>> implements FutureListener<V> {

    private final ConcurrentMap<Integer, P> promises;
    private final Integer packetId;
    private final P promise;

    public PromiseRemover(ConcurrentMap<Integer, P> promises, int packetId, P promise) {
        this.promises = promises;
        this.packetId = packetId;
        this.promise = promise;
    }

    @Override
    public void operationComplete(Future<V> future) throws Exception {
        if (!future.isSuccess()) {
            promises.remove(packetId, promise);
        }
    }
}
