package io.github.liuziyuan.test.retrofit.spring.boot.inject.builder;

import io.github.liuziyuan.retrofit.core.OverrideRule;
import io.github.liuziyuan.retrofit.core.annotation.RetrofitBuilder;
import io.github.liuziyuan.retrofit.core.annotation.RetrofitInterceptor;

@RetrofitBuilder(baseUrl = "http://192.168.100.1:9000",
        addCallAdapterFactory = RxJavaCallAdapterFactoryBuilder.class,
        addConverterFactory = {GsonConverterFactoryBuilder.class, JacksonConverterFactoryBuilder.class},
        callbackExecutor = MyCallBackExecutorBuilder.class,
        client = MyOkHttpClient.class,
        validateEagerly = false, globalOverwriteRule = OverrideRule.LOCAL_ONLY)
@RetrofitInterceptor(handler = MyRetrofitInterceptor1.class)
@RetrofitInterceptor(handler = MyRetrofitInterceptor2.class)
@RetrofitInterceptor(handler = MyRetrofitInterceptor3.class)
public interface TestApi {
}
