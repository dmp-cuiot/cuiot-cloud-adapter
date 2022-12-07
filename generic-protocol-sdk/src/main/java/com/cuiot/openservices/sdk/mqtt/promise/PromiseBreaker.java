package com.cuiot.openservices.sdk.mqtt.promise;

import io.netty.util.concurrent.Promise;

/**
 * @author unicom
 */
public class PromiseBreaker {

    private final Throwable cause;

    public PromiseBreaker(Throwable cause) {
        this.cause = cause;
    }

    public <P extends Promise<?>> PromiseBreaker renege(P promise) {
        if (promise != null) {
            promise.tryFailure(cause);
        }
        return this;
    }

    public <P extends Promise<?>> PromiseBreaker renege(Iterable<P> promises) {
        for (P promise : promises) {
            renege(promise);
        }
        return this;
    }
}
