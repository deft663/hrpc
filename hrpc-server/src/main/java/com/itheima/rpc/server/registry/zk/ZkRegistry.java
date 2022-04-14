package com.itheima.rpc.server.registry.zk;


import com.itheima.rpc.annotation.HrpcService;
import com.itheima.rpc.server.config.RpcServerConfiguration;
import com.itheima.rpc.server.registry.RpcRegistry;
import com.itheima.rpc.spring.SpringBeanFactory;
import com.itheima.rpc.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Component
@Slf4j
public class ZkRegistry implements RpcRegistry {


    @Autowired
    private SpringBeanFactory springBeanFactory;
    @Autowired
    private ServerZKit zKit;

    @Autowired
    private RpcServerConfiguration rpcServerConfiguration;


    @Override
    public void serviceRegistry() {
        // 1、找到所有标有注解HrpcService的类,将服务信息写入到zk中
        Map<String, Object> beanListByAnnotationClass = springBeanFactory.getBeanListByAnnotationClass(HrpcService.class);

        if(!CollectionUtils.isEmpty(beanListByAnnotationClass)){
            // 2、创建服务根节点
            zKit.createRootNode();
            String ip = IpUtil.getRealIp();
            beanListByAnnotationClass.forEach((name,bean)->{
                HrpcService hrpcService = bean.getClass().getAnnotation(HrpcService.class);
                Class<?> interfaceClass = hrpcService.interfaceClass();
                String serviceName = interfaceClass.getName();
                zKit.createPersistentNode(serviceName);

                String node=ip+":"+rpcServerConfiguration.getRpcPort();
                zKit.createNode(serviceName+"/"+node);
                log.info("服务{}-{}注册成功",serviceName,node);
            });

        }
    }
}
