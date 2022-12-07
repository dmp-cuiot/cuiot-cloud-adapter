package com.cuiot.openservices.sdk.mqtt.promise;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/**
 * @author unicom
 */
public class PromiseCanceller<V> implements GenericFutureListener<Future<V>> {

    private static final Logger logger = LoggerFactory.getLogger(PromiseCanceller.class);

    private final Promise<?> promise;
    private final boolean mayInterruptIfRunning;

    public PromiseCanceller(Promise<?> promise) {
        this(promise, false);
    }

    public PromiseCanceller(Promise<?> promise, boolean mayInterruptIfRunning) {
        this.promise = requireNonNull(promise, "promise");
        this.mayInterruptIfRunning = mayInterruptIfRunning;
    }

    @Override
    public void operationComplete(Future<V> future) throws Exception {
        if (future.isSuccess() || promise.isDone()) {
            // noop
        } else if (future.isCancelled() && promise.isCancellable()) {
            if (!promise.cancel(mayInterruptIfRunning)) {
                logger.warn("failed to cancel promise.");
            }
        } else {
            try {
                promise.setFailure(future.cause());
            } catch (IllegalStateException e) {
                logger.warn("failed to mark a promise as failure.", e);
            }
        }
    }
}
