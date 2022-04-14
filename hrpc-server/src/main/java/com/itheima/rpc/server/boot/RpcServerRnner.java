package com.itheima.rpc.server.boot;

import com.itheima.rpc.server.registry.RpcRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RpcServerRnner {
    @Autowired
    RpcRegistry registry;

    @Autowired
    private RpcServer rpcServer;

    /**
     * 启动rpc server
     */
    public void run() {
        //1 服务注册
        registry.serviceRegistry();
        //2 启动服务,监听端口,等待接收请求
        rpcServer.start();
    }
}
