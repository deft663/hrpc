package com.itheima.rpc.netty.request;

import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutor;

/**
 * @description
 * @author: ts
 * @create:2021-05-11 23:24
 */
public class RequestPromise extends DefaultPromise {

    public RequestPromise(EventExecutor executor) {
        super(executor);
    }
}
