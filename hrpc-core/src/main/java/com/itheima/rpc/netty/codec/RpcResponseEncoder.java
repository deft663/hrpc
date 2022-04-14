package com.itheima.rpc.netty.codec;

import com.itheima.rpc.data.RpcResponse;
import com.itheima.rpc.util.ProtostuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RpcResponseEncoder extends MessageToMessageDecoder<RpcResponse> {
    @Override
    protected void decode(ChannelHandlerContext ctx, RpcResponse rpcResponse, List<Object> list) throws Exception {
        try {
            byte[] bytes = ProtostuffUtil.serialize(rpcResponse);
            ByteBuf byteBuf=ctx.alloc().buffer(bytes.length);
            byteBuf.writeBytes(bytes);
            list.add(byteBuf);
        } catch (Exception e) {
            log.error("RpcResponseEncoder encode error,msg={}",e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
