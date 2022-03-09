package io.github.liuziyuan.retrofit.generator;

import io.github.liuziyuan.retrofit.Generator;
import io.github.liuziyuan.retrofit.RetrofitResourceContext;
import io.github.liuziyuan.retrofit.annotation.InterceptorType;
import io.github.liuziyuan.retrofit.annotation.RetrofitBuilder;
import io.github.liuziyuan.retrofit.annotation.RetrofitInterceptor;
import io.github.liuziyuan.retrofit.extension.*;
import io.github.liuziyuan.retrofit.resource.RetrofitClientBean;
import lombok.SneakyThrows;
import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.CollectionUtils;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.util.*;
import java.util.concurrent.Executor;

/**
 * Generate RetrofitBuilder instance
 *
 * @author liuziyuan
 */
public class RetrofitBuilderGenerator implements Generator<Retrofit.Builder> {
    private RetrofitClientBean clientBean;
    private RetrofitResourceContext context;
    private Retrofit.Builder builder;

    public RetrofitBuilderGenerator(RetrofitClientBean clientBean, RetrofitResourceContext context) {
        builder = new Retrofit.Builder();
        this.clientBean = clientBean;
        this.context = context;
    }

    @Override
    public Retrofit.Builder generate() {
        setBaseUrl();
        setRetrofitCallAdapterFactory();
        setRetrofitConverterFactory();
        setCallBackExecutor();
        setValidateEagerly();
        setRetrofitOkHttpClient();
        setCallFactory();
        return builder;
    }


    private void setBaseUrl() {
        builder.baseUrl(clientBean.getRealHostUrl());
    }

    private void setValidateEagerly() {
        final RetrofitBuilder retrofitBuilder = clientBean.getRetrofitBuilder();
        final boolean validateEagerly = retrofitBuilder.validateEagerly();
        builder.validateEagerly(validateEagerly);
    }

    private void setCallFactory() {
        final RetrofitBuilder retrofitBuilder = clientBean.getRetrofitBuilder();
        final Class<? extends BaseCallFactoryBuilder> callFactoryBuilderClazz = retrofitBuilder.callFactory();
        BaseCallFactoryBuilder baseCallFactoryBuilder;
        CallFactoryGenerator callFactoryGenerator;
        try {
            baseCallFactoryBuilder = context.getApplicationContext().getBean(callFactoryBuilderClazz);
            callFactoryGenerator = new CallFactoryGenerator(callFactoryBuilderClazz, baseCallFactoryBuilder.executeBuild());
        } catch (NoSuchBeanDefinitionException ex) {
            callFactoryGenerator = new CallFactoryGenerator(callFactoryBuilderClazz, null);
        }
        final Call.Factory factory = callFactoryGenerator.generate();
        if (factory != null) {
            builder.callFactory(factory);
        }
    }

    private void setCallBackExecutor() {
        final RetrofitBuilder retrofitBuilder = clientBean.getRetrofitBuilder();
        final Class<? extends BaseCallBackExecutorBuilder> callbackExecutorBuilderClazz = retrofitBuilder.callbackExecutor();
        BaseCallBackExecutorBuilder baseCallBackExecutorBuilder;
        CallBackExecutorGenerator callBackExecutorGenerator;
        try {
            baseCallBackExecutorBuilder = context.getApplicationContext().getBean(callbackExecutorBuilderClazz);
            callBackExecutorGenerator = new CallBackExecutorGenerator(callbackExecutorBuilderClazz, baseCallBackExecutorBuilder.executeBuild());
        } catch (NoSuchBeanDefinitionException ex) {
            callBackExecutorGenerator = new CallBackExecutorGenerator(callbackExecutorBuilderClazz, null);
        }
        final Executor executor = callBackExecutorGenerator.generate();
        if (executor != null) {
            builder.callbackExecutor(executor);
        }
    }

    private void setRetrofitCallAdapterFactory() {
        final RetrofitBuilder retrofitBuilder = clientBean.getRetrofitBuilder();
        final List<CallAdapter.Factory> callAdapterFactories = getCallAdapterFactories(retrofitBuilder.addCallAdapterFactory());
        if (!CollectionUtils.isEmpty(callAdapterFactories)) {
            callAdapterFactories.forEach(builder::addCallAdapterFactory);
        }
    }

    private void setRetrofitConverterFactory() {
        final RetrofitBuilder retrofitBuilder = clientBean.getRetrofitBuilder();
        final List<Converter.Factory> converterFactories = getConverterFactories(retrofitBuilder.addConverterFactory());
        if (!CollectionUtils.isEmpty(converterFactories)) {
            converterFactories.forEach(builder::addConverterFactory);
        }
    }

    @SneakyThrows
    private void setRetrofitOkHttpClient() {
        final RetrofitBuilder retrofitBuilder = clientBean.getRetrofitBuilder();
        Set<RetrofitInterceptor> allInterceptors = new LinkedHashSet<>();
        allInterceptors.addAll(clientBean.getInterceptors());
        allInterceptors.addAll(clientBean.getInheritedInterceptors());
        final List<RetrofitInterceptor> interceptors = new ArrayList<>(allInterceptors);
        OkHttpClient.Builder okHttpClientBuilder;
        OkHttpClientBuilderGenerator okHttpClientBuilderGenerator;
        if (retrofitBuilder.client() != null) {
            BaseOkHttpClientBuilder baseOkHttpClientBuilder;
            OkHttpClient.Builder componentOkHttpClientBuilder = null;
            try {
                baseOkHttpClientBuilder = context.getApplicationContext().getBean(retrofitBuilder.client());
            } catch (NoSuchBeanDefinitionException ex) {
                baseOkHttpClientBuilder = null;
            }
            if (baseOkHttpClientBuilder != null) {
                componentOkHttpClientBuilder = baseOkHttpClientBuilder.build();
            }
            okHttpClientBuilderGenerator = new OkHttpClientBuilderGenerator(retrofitBuilder.client(), componentOkHttpClientBuilder);
            okHttpClientBuilder = okHttpClientBuilderGenerator.generate();
        } else {
            okHttpClientBuilder = new OkHttpClient.Builder();
        }
        okHttpClientBuilder.addInterceptor(new DynamicBaseUrlInterceptor(context));
        okHttpClientBuilder.addInterceptor(new UrlOverWriteInterceptor(context));
        final List<Interceptor> okHttpDefaultInterceptors = getOkHttpInterceptors(interceptors, InterceptorType.DEFAULT);
        final List<Interceptor> okHttpNetworkInterceptors = getOkHttpInterceptors(interceptors, InterceptorType.NETWORK);
        okHttpDefaultInterceptors.forEach(okHttpClientBuilder::addInterceptor);
        okHttpNetworkInterceptors.forEach(okHttpClientBuilder::addNetworkInterceptor);
        builder.client(okHttpClientBuilder.build());
    }

    @SneakyThrows
    private List<Interceptor> getOkHttpInterceptors(List<RetrofitInterceptor> interceptors, InterceptorType type) {
        List<Interceptor> interceptorList = new ArrayList<>();
        OkHttpInterceptorGenerator okHttpInterceptorGenerator;
        interceptors.sort(Comparator.comparing(RetrofitInterceptor::sort));
        for (RetrofitInterceptor interceptor : interceptors) {
            if (interceptor.type() == type) {
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
        }
        return interceptorList;
    }

    @SneakyThrows
    private List<CallAdapter.Factory> getCallAdapterFactories(Class<? extends BaseCallAdapterFactoryBuilder>[] callAdapterFactoryClasses) {
        List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>();
        CallAdapterFactoryGenerator callAdapterFactoryGenerator;
        for (Class<? extends BaseCallAdapterFactoryBuilder> callAdapterFactoryClazz : callAdapterFactoryClasses) {
            BaseCallAdapterFactoryBuilder baseCallAdapterFactoryBuilder;
            try {
                baseCallAdapterFactoryBuilder = context.getApplicationContext().getBean(callAdapterFactoryClazz);
                callAdapterFactoryGenerator = new CallAdapterFactoryGenerator(callAdapterFactoryClazz, baseCallAdapterFactoryBuilder.executeBuild());
            } catch (NoSuchBeanDefinitionException ex) {
                callAdapterFactoryGenerator = new CallAdapterFactoryGenerator(callAdapterFactoryClazz, null);
            }
            callAdapterFactories.add(callAdapterFactoryGenerator.generate());
        }
        return callAdapterFactories;
    }

    @SneakyThrows
    private List<Converter.Factory> getConverterFactories(Class<? extends BaseConverterFactoryBuilder>[] converterFactoryBuilderClasses) {
        List<Converter.Factory> converterFactories = new ArrayList<>();
        ConverterFactoryGenerator converterFactoryGenerator;
        for (Class<? extends BaseConverterFactoryBuilder> converterFactoryBuilderClazz : converterFactoryBuilderClasses) {
            BaseConverterFactoryBuilder baseCallBackExecutorBuilder;
            try {
                baseCallBackExecutorBuilder = context.getApplicationContext().getBean(converterFactoryBuilderClazz);
                converterFactoryGenerator = new ConverterFactoryGenerator(converterFactoryBuilderClazz, baseCallBackExecutorBuilder.executeBuild());
            } catch (NoSuchBeanDefinitionException ex) {
                converterFactoryGenerator = new ConverterFactoryGenerator(converterFactoryBuilderClazz, null);
            }
            converterFactories.add(converterFactoryGenerator.generate());
        }
        return converterFactories;
    }

}
