package com.itheima.rpc.client.boot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RpcClientRunner {

    @Autowired
    private RpcServiceDiscovery serviceDiscovery;

    public void run() {
        //1、开启服务发现
        serviceDiscovery.serviceDiscovery();
        //2、客户端controller中如果有HrpcRemote注解，需要创建代理并注入,通过自定义RpcAnnotationProcessor

    }
}
