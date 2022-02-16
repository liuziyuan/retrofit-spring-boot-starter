package io.github.liuziyuan.retrofit.generator;

import io.github.liuziyuan.retrofit.Generator;
import io.github.liuziyuan.retrofit.RetrofitResourceContext;
import io.github.liuziyuan.retrofit.annotation.RetrofitBuilder;
import io.github.liuziyuan.retrofit.annotation.RetrofitInterceptor;
import io.github.liuziyuan.retrofit.extension.BaseInterceptor;
import io.github.liuziyuan.retrofit.extension.BaseOkHttpClientBuilder;
import io.github.liuziyuan.retrofit.extension.UrlOverWriteInterceptor;
import io.github.liuziyuan.retrofit.resource.RetrofitClientBean;
import lombok.SneakyThrows;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.CollectionUtils;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Generate RetrofitBuilder instance
 *
 * @author liuziyuan
 */
public class RetrofitBuilderGenerator implements Generator<Retrofit.Builder> {
    private RetrofitClientBean clientBean;
    private RetrofitResourceContext context;

    public RetrofitBuilderGenerator(RetrofitClientBean clientBean, RetrofitResourceContext context) {
        this.clientBean = clientBean;
        this.context = context;
    }

    @Override
    public Retrofit.Builder generate() {
        Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl(clientBean.getRealHostUrl());
        setRetrofitCallAdapterFactory(clientBean, builder);
        setRetrofitConverterFactory(clientBean, builder);
        setRetrofitOkHttpClient(clientBean, builder);
        return builder;
    }

    private void setRetrofitCallAdapterFactory(RetrofitClientBean clientBean, Retrofit.Builder builder) {
        final RetrofitBuilder retrofitBuilder = clientBean.getRetrofitBuilder();
        final List<CallAdapter.Factory> callAdapterFactories = getCallAdapterFactories(retrofitBuilder.addCallAdapterFactory());
        if (!CollectionUtils.isEmpty(callAdapterFactories)) {
            callAdapterFactories.forEach(builder::addCallAdapterFactory);
        }
    }

    private void setRetrofitConverterFactory(RetrofitClientBean clientBean, Retrofit.Builder builder) {
        final RetrofitBuilder retrofitBuilder = clientBean.getRetrofitBuilder();
        final List<Converter.Factory> converterFactories = getConverterFactories(retrofitBuilder.addConverterFactory());
        if (!CollectionUtils.isEmpty(converterFactories)) {
            converterFactories.forEach(builder::addConverterFactory);
        }
    }

    @SneakyThrows
    private void setRetrofitOkHttpClient(RetrofitClientBean clientBean, Retrofit.Builder builder) {
        final RetrofitBuilder retrofitBuilder = clientBean.getRetrofitBuilder();
        final List<RetrofitInterceptor> interceptors = clientBean.getInterceptors();
        OkHttpClient.Builder okHttpClientBuilder;
        OkHttpClientBuilderGenerator okHttpClientBuilderGenerator;
        if (retrofitBuilder.client() != null) {
            BaseOkHttpClientBuilder baseOkHttpClientBuilder = null;
            OkHttpClient.Builder componentOkHttpClientBuilder = null;
            try {
                baseOkHttpClientBuilder = context.getApplicationContext().getBean(retrofitBuilder.client());
            } catch (NoSuchBeanDefinitionException ex) {

            }
            if (baseOkHttpClientBuilder != null) {
                componentOkHttpClientBuilder = baseOkHttpClientBuilder.build();
            }
            okHttpClientBuilderGenerator = new OkHttpClientBuilderGenerator(retrofitBuilder.client(), componentOkHttpClientBuilder);
            okHttpClientBuilder = okHttpClientBuilderGenerator.generate();
        } else {
            okHttpClientBuilder = new OkHttpClient.Builder();
        }
        okHttpClientBuilder.addInterceptor(new UrlOverWriteInterceptor(context));
        final List<Interceptor> okHttpInterceptors = getOkHttpInterceptors(interceptors);

        okHttpInterceptors.forEach(okHttpClientBuilder::addInterceptor);
        builder.client(okHttpClientBuilder.build());
    }

    @SneakyThrows
    private List<Interceptor> getOkHttpInterceptors(List<RetrofitInterceptor> interceptors) {
        List<Interceptor> interceptorList = new ArrayList<>();
        OkHttpInterceptorGenerator okHttpInterceptorGenerator;
        interceptors.sort(Comparator.comparing(RetrofitInterceptor::sort));
        for (RetrofitInterceptor interceptor : interceptors) {
            BaseInterceptor componentInterceptor;
            try {
                componentInterceptor = context.getApplicationContext().getBean(interceptor.handler());
            } catch (NoSuchBeanDefinitionException ex) {
                componentInterceptor = null;
            }
            okHttpInterceptorGenerator = new OkHttpInterceptorGenerator(interceptor, context, componentInterceptor);
            final Interceptor generateInterceptor = okHttpInterceptorGenerator.generate();
            interceptorList.add(generateInterceptor);
        }
        return interceptorList;
    }

    @SneakyThrows
    private List<CallAdapter.Factory> getCallAdapterFactories(Class<? extends CallAdapter.Factory>[] callAdapterFactoryClasses) {
        List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>();
        CallAdapterFactoryGenerator adapterFactoryHandler;
        for (Class<? extends CallAdapter.Factory> callAdapterFactoryClass : callAdapterFactoryClasses) {
            adapterFactoryHandler = new CallAdapterFactoryGenerator(callAdapterFactoryClass);
            callAdapterFactories.add(adapterFactoryHandler.generate());
        }
        return callAdapterFactories;
    }

    @SneakyThrows
    private List<Converter.Factory> getConverterFactories(Class<? extends Converter.Factory>[] converterFactoryClasses) {
        List<Converter.Factory> converterFactories = new ArrayList<>();
        ConverterFactoryGenerator converterFactoryGenerator;
        for (Class<? extends Converter.Factory> converterFactoryClass : converterFactoryClasses) {
            converterFactoryGenerator = new ConverterFactoryGenerator(converterFactoryClass);
            converterFactories.add(converterFactoryGenerator.generate());
        }
        return converterFactories;
    }


}