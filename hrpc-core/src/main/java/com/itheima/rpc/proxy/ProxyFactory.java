package com.itheima.rpc.proxy;

/**
 * @description
 * @author: ts
 * @create:2021-05-11 16:48
 */
public interface ProxyFactory {

    public  <T> T newProxyInstance(Class<T> cls) ;
}
