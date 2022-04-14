package com.itheima.rpc.client.boot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class RpcBootstrap {

    @Autowired
    private RpcClientRunner rpcClientRunner;

    @PostConstruct
    public void initRpcClient() {
        rpcClientRunner.run();
    }
}
