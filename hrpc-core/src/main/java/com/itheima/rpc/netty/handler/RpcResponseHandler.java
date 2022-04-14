package com.itheima.rpc.netty.handler;

import com.itheima.rpc.data.RpcResponse;
import com.itheima.rpc.netty.request.RequestPromise;
import com.itheima.rpc.netty.request.RpcRequestHolder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        // 处理响应结果
        log.info("---rpc调用成功，返回结果为:{}",response);
        RequestPromise requestPromise = RpcRequestHolder.getRequestPromise(response.getRequestId());
        if (requestPromise!=null) {
            //通知回调监听
            requestPromise.setSuccess(response);
        }
    }
}
