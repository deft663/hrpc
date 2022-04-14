package com.itheima.rpc.client.discovery.zk;

import com.itheima.rpc.cache.ServiceProviderCache;
import com.itheima.rpc.client.boot.RpcServiceDiscovery;
import com.itheima.rpc.provider.ServiceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ZKServiceDiscovery implements RpcServiceDiscovery {

    @Autowired
    private ClientZKit clientZKit;

    @Autowired
    private ServiceProviderCache providerCache;

    @Override
    public void serviceDiscovery() {
        //1、拉取所有服务接口列表
        List<String> allService = clientZKit.getServiceList();
        if (!allService.isEmpty()) {
            for (String serviceName : allService) {
                //2、获取该接口下的节点列表
                List<ServiceProvider> providers = clientZKit.getServiceInfos(serviceName);
                //3、缓存该服务的所有节点信息
                log.info("订阅的服务名为={},服务提供者有={}",serviceName,providers);
                providerCache.put(serviceName,providers);
                //4、监听该服务下是所有节点信息变化
                clientZKit.subscribeZKEvent(serviceName);
            }
        }
    }
}
