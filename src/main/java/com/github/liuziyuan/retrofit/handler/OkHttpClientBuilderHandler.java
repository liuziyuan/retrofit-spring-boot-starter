package com.github.liuziyuan.retrofit.handler;

import com.github.liuziyuan.retrofit.extension.OkHttpClientBuilder;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;

/**
 * @author liuziyuan
 * @date 1/14/2022 11:16 AM
 */
public class OkHttpClientBuilderHandler implements Handler<OkHttpClient.Builder> {
    private Class<? extends OkHttpClientBuilder> okHttpClientBuilder;

    public OkHttpClientBuilderHandler(Class<? extends OkHttpClientBuilder> okHttpClientBuilder) {
        this.okHttpClientBuilder = okHttpClientBuilder;
    }

    @SneakyThrows
    @Override
    public OkHttpClient.Builder generate() {
        final OkHttpClientBuilder okHttpClientBuilder = this.okHttpClientBuilder.newInstance();
        return okHttpClientBuilder.executeBuild();
    }
}
