package com.itheima.rpc.netty.codec;

import com.itheima.rpc.data.RpcRequest;
import com.itheima.rpc.util.ProtostuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RpcRequestDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf msg, List<Object> list) throws Exception {
        try {
            int length = msg.readableBytes();
            byte[] bytes = new byte[length];
            msg.readBytes(bytes);
            RpcRequest rpcRequest = ProtostuffUtil.deserialize(bytes, RpcRequest.class);
            list.add(rpcRequest);
        } catch (Exception e) {
            log.error("RpcRequestDecoder decode error,msg={}",e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
