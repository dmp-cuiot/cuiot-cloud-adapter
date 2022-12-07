package com.cuiot.openservices.sdk.entity;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author unicom
 */
public interface CallableFuture<V> {

    /**
     * 获取执行结果
     *
     * @return 执行结果
     * @throws CancellationException 取消异常
     * @throws ExecutionException    执行异常
     * @throws InterruptedException  中断异常
     */
    V get() throws CancellationException, ExecutionException, InterruptedException;

    /**
     * 获取执行结果
     *
     * @param timeout 超时
     * @param unit    时间单位
     * @return 执行结果
     * @throws InterruptedException 中断异常
     * @throws ExecutionException   执行异常
     * @throws TimeoutException     超时异常
     */
    V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * 添加监听器
     *
     * @param listener 监听器
     * @return Promise
     */
    Promise<V> addListener(GenericFutureListener<? extends Future<? super V>> listener);

    /**
     * 添加监听器
     *
     * @param listeners 监听器
     * @return Promise
     */
    Future<V> addListeners(GenericFutureListener<? extends Future<? super V>>... listeners);

}
