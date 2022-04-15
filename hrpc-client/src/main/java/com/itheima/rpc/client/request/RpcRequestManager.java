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
import com.itheima.rpc.netty.request.ChannelMapping;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.ExecutionException;

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
        Channel channel =null;
        //Channel channel1 = RpcRequestHolder.getChannel(serviceProvider.getServerIp(), serviceProvider.getRpcPort());
        if(!RpcRequestHolder.channelExist(serviceProvider.getServerIp(),serviceProvider.getRpcPort())){
            try {
                EventLoopGroup worker = new NioEventLoopGroup(0, new DefaultThreadFactory("worker"));
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
                                pipeline.addLast("RpcResponseHandler", new RpcResponseHandler());
                            }
                        });
                ChannelFuture future = bootstrap.connect(serviceProvider.getServerIp(), serviceProvider.getRpcPort()).sync();
                //如果连接成功
                if (future.isSuccess()) {
                    //连接建立成功
                    channel = future.channel();
                    ChannelMapping channelMapping = new ChannelMapping(serviceProvider.getServerIp(), serviceProvider.getRpcPort(), channel);
                    RpcRequestHolder.addChannelMapping(channelMapping);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //建立连接
        try {
            channel = RpcRequestHolder.getChannel(serviceProvider.getServerIp(), serviceProvider.getRpcPort());

            //向对端发送数据
            //创建promise
            RequestPromise requestPromise = new RequestPromise(channel.eventLoop());
            //建立映射
            RpcRequestHolder.addRequestPromise(request.getRequestId(),requestPromise);
            // 发送数据
            ChannelFuture f = channel.writeAndFlush(request);

            //等待promise返回结果
            try {
                RpcResponse response = (RpcResponse) requestPromise.get();
                return response;
            } catch (ExecutionException e) {
                e.printStackTrace();
            }finally {
                RpcRequestHolder.removeRequestPromise(request.getRequestId());
            }
        } catch (Exception e) {
            log.error("remote rpc request error,msg={}", e.getCause());
            throw new RpcException(e);
        }
        return new RpcResponse();
    }
}
