package com.itheima.rpc.client.request;

import com.itheima.rpc.cache.ServiceProviderCache;
import com.itheima.rpc.client.config.RpcClientConfiguration;
import com.itheima.rpc.data.RpcRequest;
import com.itheima.rpc.data.RpcResponse;
import com.itheima.rpc.enums.StatusEnum;
import com.itheima.rpc.exception.RpcException;
import com.itheima.rpc.netty.codec.FrameDecoder;
import com.itheima.rpc.netty.codec.FrameEncoder;
import com.itheima.rpc.netty.codec.RpcRequestEncoder;
import com.itheima.rpc.netty.codec.RpcResponseDecoder;
import com.itheima.rpc.netty.handler.RpcResponseHandler;
import com.itheima.rpc.netty.request.RequestPromise;
import com.itheima.rpc.netty.request.RpcRequestHolder;
import com.itheima.rpc.provider.ServiceProvider;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RpcRequestManager {
    @Autowired
    private ServiceProviderCache providerCache;

    @Autowired
    private RpcClientConfiguration clientConfiguration;

    public RpcResponse sendRequest(RpcRequest request) {
        // 1、获取可用发服务节点
        List<ServiceProvider> serviceProviders = providerCache.get(request.getClassName());
        if (CollectionUtils.isEmpty(serviceProviders)) {
            log.info("客户端没有发现可用发服务节点");
            throw new RpcException(StatusEnum.NOT_FOUND_SERVICE_PROVINDER);
        }
        ServiceProvider serviceProvider = serviceProviders.get(0);
        if (serviceProvider != null) {
            return requestByNetty(request, serviceProvider);
        } else {
            throw new RpcException(StatusEnum.NOT_FOUND_SERVICE_PROVINDER);
        }
    }

    private RpcResponse requestByNetty(RpcRequest request, ServiceProvider serviceProvider) {
        EventLoopGroup worker = new NioEventLoopGroup(0, new DefaultThreadFactory("worker"));
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(worker)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        RpcResponseHandler responseHandler = new RpcResponseHandler();//复用
                        Bootstrap bootstrap = new Bootstrap();
                        bootstrap.group(worker)
                                .channel(NioSocketChannel.class)
                                .handler(new ChannelInitializer<SocketChannel>() {
                                    @Override
                                    protected void initChannel(SocketChannel ch) throws Exception {
                                        ChannelPipeline pipeline = ch.pipeline();
                                        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                                        //添加请求编码器
                                        pipeline.addLast("FrameEncoder", new FrameEncoder());
                                        pipeline.addLast("RpcReqeustEncoder", new RpcRequestEncoder());

                                        pipeline.addLast("FrameDecoder", new FrameDecoder());
                                        pipeline.addLast("RpcResponseDecoder", new RpcResponseDecoder());
                                        pipeline.addLast("RpcResponseHandler", responseHandler);
                                    }
                                });
                    }
                });
        //建立连接
        try {
            ChannelFuture future = bootstrap.connect(serviceProvider.getServerIp(), serviceProvider.getRpcPort()).sync();
            //如果连接成功
            if (future.isSuccess()) {
                RequestPromise requestPromise = new RequestPromise(future.channel().eventLoop());
                RpcRequestHolder.addRequestPromise(request.getRequestId(),requestPromise);
                //向服务端发送数据
                ChannelFuture channelFuture = future.channel().writeAndFlush(request);
                //添加发数据结果回调监听
                channelFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        //如果没有发送成功,移除requetPromise
                        if (!future.isSuccess()) {
                            RpcRequestHolder.removeRequestPromise(request.getRequestId());
                        }
                    }
                });

                //设置结果回调监听
                requestPromise.addListener(new FutureListener<RpcResponse>() {
                    @Override
                    public void operationComplete(Future<RpcResponse> future) throws Exception {
                        if (!future.isSuccess()) {
                            RpcRequestHolder.removeRequestPromise(request.getRequestId());
                        }
                    }
                });
                //获取返回结果
                RpcResponse response = (RpcResponse) requestPromise.get(clientConfiguration.getConnectTimeout(), TimeUnit.SECONDS);
                RpcRequestHolder.removeRequestPromise(request.getRequestId());
                return response;
            }
        } catch (Exception e) {
            log.error("remote rpc request error,msg={}", e.getCause());
            throw new RpcException(e);
        }
        return new RpcResponse();
    }
}
