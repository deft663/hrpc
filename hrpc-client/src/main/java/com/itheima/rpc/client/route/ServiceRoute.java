package com.itheima.rpc.client.route;

import com.itheima.rpc.provider.ServiceProvider;

import java.util.List;

/**
 * @description
 * @author: ts
 * @create:2021-05-11 18:19
 */
public interface ServiceRoute {

    List<ServiceProvider> route(List<ServiceProvider> serviceProviderList);
}
