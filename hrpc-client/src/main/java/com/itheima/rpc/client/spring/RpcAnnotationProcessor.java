package com.itheima.rpc.client.spring;

import com.itheima.rpc.annotation.HrpcRemote;
import com.itheima.rpc.proxy.ProxyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Slf4j
@Component
public class RpcAnnotationProcessor implements BeanFactoryPostProcessor, BeanPostProcessor, ApplicationContextAware {

    private ProxyFactory proxyFactory;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    /**
     * 扫描bean上的HrpcRemote注解，创建代理并注入
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                if (! field.isAccessible()) {
                    field.setAccessible(true);
                }
                HrpcRemote hrpcRemote = field.getAnnotation(HrpcRemote.class);
                if (hrpcRemote != null) {
                    //创建代理对象
                    Object proxy = proxyFactory.newProxyInstance(field.getType());
                    log.info("为HrpcRemote注解标注的属性生成的代理对象:{}",proxy);
                    if (proxy != null) {
                        //可以选择存入容器,可以通过autowired自动注入,这里可以直接注入
                        field.set(bean, proxy);
                    }
                }
            } catch (Throwable e) {
                log.error("Failed to init remote service reference at filed " + field.getName() + " in class " + bean.getClass().getName() + ", cause: " + e.getMessage(), e);
            }
        }
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.proxyFactory = applicationContext.getBean(ProxyFactory.class);
    }
}
