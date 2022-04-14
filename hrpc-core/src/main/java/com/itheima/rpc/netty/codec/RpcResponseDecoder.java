package com.itheima.rpc.netty.codec;

import com.itheima.rpc.data.RpcResponse;
import com.itheima.rpc.util.ProtostuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RpcResponseDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        try {
            int length = buf.readableBytes();
            byte[] bytes = new byte[length];
            buf.readBytes(bytes);
            RpcResponse response = ProtostuffUtil.deserialize(bytes, RpcResponse.class);
            out.add(response);
        } catch (Exception e) {
            log.error("RpcResponseDecoder decode error,msg={}",e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
