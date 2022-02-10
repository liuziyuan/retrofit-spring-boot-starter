package io.github.liuziyuan.retrofit.demo.api;

import io.github.liuziyuan.retrofit.annotation.RetrofitBuilder;
import io.github.liuziyuan.retrofit.annotation.RetrofitInterceptor;
import io.github.liuziyuan.retrofit.demo.MyOkHttpClient;
import io.github.liuziyuan.retrofit.demo.MyRetrofitInterceptor2;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * @author liuziyuan
 * @date 1/10/2022 2:10 PM
 */

@RetrofitBuilder(baseUrl = "${app.hello-inherit.base-url}",
        addConverterFactory = {GsonConverterFactory.class, JacksonConverterFactory.class},
        addCallAdapterFactory = {RxJavaCallAdapterFactory.class},
        client = MyOkHttpClient.class)
@RetrofitInterceptor(handler = MyRetrofitInterceptor2.class)
public interface HelloInheritApi {
}