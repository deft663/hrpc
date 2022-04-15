package com.itheima.rpc.netty.codec;

import com.itheima.rpc.data.RpcResponse;
import com.itheima.rpc.util.ProtostuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RpcResponseEncoder extends MessageToMessageEncoder<RpcResponse> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcResponse response, List<Object> list) throws Exception {
        try {
            byte[] bytes = ProtostuffUtil.serialize(response);
            ByteBuf buffer = ctx.alloc().buffer(bytes.length);
            buffer.writeBytes(bytes);
            list.add(buffer);
        } catch (Exception e) {
            log.error("RpcResponseEncoder encode error,msg={}",e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
