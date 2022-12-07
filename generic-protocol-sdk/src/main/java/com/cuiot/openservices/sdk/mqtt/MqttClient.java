package com.cuiot.openservices.sdk.mqtt;

import com.cuiot.openservices.sdk.config.ConfigFactory;
import com.cuiot.openservices.sdk.config.IAdapterConfig;
import com.cuiot.openservices.sdk.constant.CommonConstant;
import com.cuiot.openservices.sdk.handler.DownlinkHandler;
import com.cuiot.openservices.sdk.mqtt.handler.MqttHandler;
import com.cuiot.openservices.sdk.mqtt.handler.MqttPingHandler;
import com.cuiot.openservices.sdk.mqtt.handler.ProtocolMessageHandler;
import com.cuiot.openservices.sdk.mqtt.promise.*;
import com.cuiot.openservices.sdk.mqtt.result.MqttConnectResult;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttUnsubAckMessage;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.cuiot.openservices.sdk.mqtt.MqttFixedHeaders.DISCONNECT_HEADER;

/**
 * Mqtt Client，用于与平台的通信
 *
 * @author yht
 */
public final class MqttClient {
    private static Logger logger = LoggerFactory.getLogger(MqttClient.class);

    private EventLoopGroup eventLoopGroup;
    private Channel channel;

    public MqttClient(DownlinkHandler downlinkHandler, IMqttConnection iMqttConnection) {
        this.channel = initNettyClient(downlinkHandler, iMqttConnection);
    }

    private Channel initNettyClient(DownlinkHandler downlinkHandler, IMqttConnection iMqttConnection) {
        IAdapterConfig adapterConfig = ConfigFactory.getAdapterConfig();
        InetSocketAddress broker = new InetSocketAddress(adapterConfig.getConnectionHost(), adapterConfig.getConnectionPort());
        // TODO 配置
        final DefaultEventExecutorGroup businessGroup = new DefaultEventExecutorGroup(adapterConfig.getHandlerThreads());
        Class<? extends AbstractChannel> channelClass;
        if (Epoll.isAvailable()) {
            eventLoopGroup = new EpollEventLoopGroup();
            channelClass = EpollSocketChannel.class;
        } else {
            eventLoopGroup = new NioEventLoopGroup();
            channelClass = NioSocketChannel.class;
        }
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup);
        bootstrap.channel(channelClass);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("idleStateHandler", new IdleStateHandler(60, 60, 0));
                pipeline.addLast("mqttDecoder", new MqttDecoder());
                pipeline.addLast("mqttPingHandler", new MqttPingHandler(CommonConstant.MQTT_KEEPALIVE_TIME));
                pipeline.addLast("mqttEncoder", MqttEncoder.INSTANCE);
                pipeline.addLast("mqttHandler", new MqttHandler());
                pipeline.addLast(businessGroup, new ProtocolMessageHandler(downlinkHandler, iMqttConnection));
                if (adapterConfig.enableTls()) {
                    pipeline.addFirst("ssl", SslContextFactory.createSslContext().newHandler(ch.alloc()));
                }
            }
        });

        try {
            return bootstrap.connect(broker.getAddress(), broker.getPort()).sync().channel();
        } catch (InterruptedException e) {
            logger.error("connect exception", e);
            return null;
        }
    }

    public MqttConnectResult connect(String clientId, String userName, String psw) throws ExecutionException, InterruptedException {
        MqttConnect mqttConnect = new MqttConnect();
        mqttConnect.setClientId(clientId.getBytes());
        mqttConnect.setUsername(userName);
        mqttConnect.setPassword(psw);
        Future<MqttConnectResult> future = writeAndFlush(new MqttConnectPromise(mqttConnect, channel.eventLoop()));
        // TODO 超时时间
        return future.get();
    }

    public void disconnectSilent() {
        try {
            disconnect();
        } catch (Exception e) {
            logger.error("");
        }
    }

    public Future<Void> disconnect() throws InterruptedException {
        ChannelFuture channelFuture = channel.writeAndFlush(new MqttMessage(DISCONNECT_HEADER));
        channel.closeFuture().sync();
        channel.eventLoop().shutdownGracefully();
        // TODO
        eventLoopGroup.shutdownGracefully();
        eventLoopGroup = null;
        channel = null;
        return channelFuture;
    }

    public MqttSubscribePromise subscribe(List<MqttSubscribe> subscriptions) {
        return writeAndFlush(new MqttSubscribePromise(channel.eventLoop(), subscriptions));
    }

    public Future<MqttUnsubAckMessage> unsubscribe(List<String> topicFilters) {
        return writeAndFlush(new MqttUnsubscribePromise(channel.eventLoop(), topicFilters));
    }

    private synchronized <P extends Promise<V>, V> P writeAndFlush(P promise) {
        channel.writeAndFlush(promise).addListener(new PromiseCanceller<>(promise));
        return promise;
    }

    public Channel getChannel() {
        return channel;
    }

}
