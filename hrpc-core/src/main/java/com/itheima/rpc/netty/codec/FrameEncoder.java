package com.itheima.rpc.netty.codec;

import io.netty.handler.codec.LengthFieldPrepender;

public class FrameEncoder extends LengthFieldPrepender {
    public FrameEncoder() {
        super(2);
    }
}
