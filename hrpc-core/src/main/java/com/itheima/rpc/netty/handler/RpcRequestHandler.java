package com.itheima.rpc.netty.handler;


import com.itheima.rpc.data.RpcRequest;
import com.itheima.rpc.data.RpcResponse;
import com.itheima.rpc.spring.SpringBeanFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@ChannelHandler.Sharable
@Component
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcRequest> {

    @Autowired
    private SpringBeanFactory springBeanFactory;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        log.info("rpc服务端接收到的请求为:{}",request);
        //1、获取请求参数
        RpcResponse response = new RpcResponse();
        String requestId = request.getRequestId();
        response.setRequestId(requestId);
        String className = request.getClassName();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();
        try {
            //2找到目标bean
            Object targetBean = springBeanFactory.getBean(Class.forName(className));
            Method targetMethod = targetBean.getClass().getMethod(methodName, parameterTypes);
            //3执行目标方法获取结果
            Object result = targetMethod.invoke(targetBean, parameters);
            //4 封装响应结果
            response.setResult(result);
        } catch (Throwable e) {
            response.setCause(e);
            log.error("rpc server invoke error,msg={}",e.getMessage());
        } finally {
            log.info("服务端执行成功，响应为:{}",response);
            ctx.channel().writeAndFlush(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("服务端出现异常,异常信息为:{}",cause.getCause());
        super.exceptionCaught(ctx, cause);
    }
}
