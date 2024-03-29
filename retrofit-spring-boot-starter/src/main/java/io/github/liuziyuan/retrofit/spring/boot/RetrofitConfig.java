package io.github.liuziyuan.retrofit.spring.boot;

import io.github.liuziyuan.retrofit.spring.boot.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 全局配置，随@EnableRetrofit 启动
 *
 * @author liuziyuan
 */
@Configuration
public class RetrofitConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean
    public static RetrofitResourceDefinitionRegistry retrofitResourceDefinitionRegistry() {
        return new RetrofitResourceDefinitionRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public static SpringContextUtil springContextUtil() {
        return new SpringContextUtil();
    }

}
