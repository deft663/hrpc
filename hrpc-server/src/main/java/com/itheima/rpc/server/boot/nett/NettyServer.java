package com.itheima.rpc.server.boot.nett;

import com.itheima.rpc.netty.codec.FrameDecoder;
import com.itheima.rpc.netty.codec.FrameEncoder;
import com.itheima.rpc.netty.codec.RpcRequestDecoder;
import com.itheima.rpc.netty.codec.RpcResponseEncoder;
import com.itheima.rpc.netty.handler.RpcRequestHandler;
import com.itheima.rpc.server.boot.RpcServer;
import com.itheima.rpc.server.config.RpcServerConfiguration;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.UnorderedThreadPoolEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NettyServer implements RpcServer {

    @Autowired
    private RpcServerConfiguration rpcServerConfiguration;


    @Autowired
    private RpcRequestHandler rpcRequestHandler;
    @Override
    public void start() {
        EventLoopGroup boss = new NioEventLoopGroup(1,new DefaultThreadFactory("boss"));
        EventLoopGroup worker = new NioEventLoopGroup(0,new DefaultThreadFactory("worker"));
        EventExecutorGroup business = new UnorderedThreadPoolEventExecutor(NettyRuntime.availableProcessors()*2,new DefaultThreadFactory("business"));
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss,worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG,1024)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            //??????
                            pipeline.addLast("frameEncoder",new FrameEncoder());
                            pipeline.addLast("responseEncoder",new RpcResponseEncoder());
                            // ??????
                            pipeline.addLast("frameDecoder",new FrameDecoder());
                            pipeline.addLast("requestDecoder",new RpcRequestDecoder());
                            pipeline.addLast(business,"rpcRequestHandler",new RpcRequestHandler());
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(rpcServerConfiguration.getRpcPort()).sync();
            log.info("?????????????????????{}????????????",rpcServerConfiguration.getRpcPort());
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("?????????????????????,msg={}",e.getMessage());
        }finally {
            business.shutdownGracefully();
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }
}
