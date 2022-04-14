package com.itheima.rpc.client.cluster;

import com.itheima.rpc.provider.ServiceProvider;
import java.util.List;

/**
 * @description
 * @author: ts
 * @create:2021-05-13 11:46
 */
public interface LoadBalanceStrategy {

    /**
     * 根据对应的策略进行负载均衡
     * @param serviceProviders
     * @return
     */
    ServiceProvider select(List<ServiceProvider> serviceProviders);

}
