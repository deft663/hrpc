package com.itheima.rpc.netty.codec;

import com.itheima.rpc.data.RpcRequest;
import com.itheima.rpc.util.ProtostuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RpcRequestEncoder extends MessageToMessageEncoder<RpcRequest> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcRequest request, List<Object> list) throws Exception {
        try {
            byte[] bytes = ProtostuffUtil.serialize(request);
            ByteBuf buf = channelHandlerContext.alloc().buffer(bytes.length);
            buf.writeBytes(bytes);
            list.add(buf);
        } catch (Exception e) {
            log.error("RpcRequestEncoder error,msg={}",e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
