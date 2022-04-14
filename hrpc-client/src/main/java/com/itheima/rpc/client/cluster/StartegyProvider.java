package com.itheima.rpc.client.cluster;


/**
 * @description
 * @author: ts
 * @create:2021-05-13 11:51
 */
public interface StartegyProvider {

    LoadBalanceStrategy getStrategy();
}
