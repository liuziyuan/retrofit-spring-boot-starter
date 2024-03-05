package io.github.liuziyuan.retrofit.spring.boot.config;

import io.github.liuziyuan.retrofit.spring.boot.RetrofitGlobalConfigProperties;
import io.github.liuziyuan.retrofit.spring.boot.RetrofitResourceDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liuziyuan
 */
@Configuration
public class RetrofitConfig {

    @Bean
    @ConditionalOnMissingBean
    public static RetrofitResourceDefinitionRegistry retrofitResourceDefinitionRegistry() {
        return new RetrofitResourceDefinitionRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public RetrofitGlobalConfigProperties retrofitGlobalConfigProperties() {
        return new RetrofitGlobalConfigProperties();
    }
}
