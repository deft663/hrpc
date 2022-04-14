package com.itheima.rpc.server.boot.nett;


import com.itheima.rpc.server.boot.RpcServerRnner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class RpcServerBootstrap {

    @Autowired
    private RpcServerRnner rpcServerRnner;

    @PostConstruct
    public void initRpcServer() {
        rpcServerRnner.run();
    }
}
