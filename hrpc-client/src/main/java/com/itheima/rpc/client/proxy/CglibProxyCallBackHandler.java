package com.itheima.rpc.client.proxy;

import com.itheima.rpc.client.request.RpcRequestManager;
import com.itheima.rpc.data.RpcRequest;
import com.itheima.rpc.data.RpcResponse;
import com.itheima.rpc.exception.RpcException;
import com.itheima.rpc.spring.SpringBeanFactory;
import com.itheima.rpc.util.RequestIdUtil;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * @description
 * @author: ts
 * @create:2021-05-12 00:11
 */
public class CglibProxyCallBackHandler implements MethodInterceptor {


    public Object intercept(Object o, Method method, Object[] parameters, MethodProxy methodProxy) throws Throwable {

        //放过toString,hashcode，equals等方法，采用spring工具类
        if ( ReflectionUtils.isObjectMethod(method)) {
            return method.invoke(method.getDeclaringClass().newInstance(),parameters);
        }
        //1、获取rpc请求所需的参数
        String requestId = RequestIdUtil.requestId();
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        //2、构建rpc请求对象
        RpcRequest request = RpcRequest.builder()
                .requestId(requestId)
                .className(className)
                .methodName(methodName)
                .parameterTypes(parameterTypes)
                .parameters(parameters)
                .build();
        //3、发送rpc请求获取响应
        RpcRequestManager rpcRequestManager = SpringBeanFactory.getBean(RpcRequestManager.class);
        if (rpcRequestManager==null) {
            throw new RpcException("spring ioc exception");
        }
        RpcResponse response = rpcRequestManager.sendRequest(request);
        //4、返回结果数据
        return response.getResult();
    }
}
