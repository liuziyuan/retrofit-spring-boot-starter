package io.github.liuziyuan.retrofit;

import io.github.liuziyuan.retrofit.resource.RetrofitClientBean;
import io.github.liuziyuan.retrofit.resource.RetrofitClientBeanGenerator;
import io.github.liuziyuan.retrofit.resource.RetrofitServiceBean;
import io.github.liuziyuan.retrofit.resource.RetrofitServiceBeanGenerator;
import org.springframework.core.env.Environment;

import java.util.*;

/**
 * the builder of Retrofit resource context, used to assemble all the retrofit resource
 *
 * @author liuziyuan
 */
public class RetrofitResourceContextBuilder {

    private List<RetrofitClientBean> retrofitClientBeanList;
    private List<RetrofitServiceBean> retrofitServiceBeanList;
    private final Map<String, RetrofitServiceBean> retrofitServiceBeanHashMap;
    private final Environment environment;

    public RetrofitResourceContextBuilder(Environment environment) {
        retrofitClientBeanList = new ArrayList<>();
        retrofitServiceBeanList = new ArrayList<>();
        retrofitServiceBeanHashMap = new HashMap<>();
        this.environment = environment;
    }

    public RetrofitResourceContextBuilder build(Set<Class<?>> retrofitBuilderClassSet) {
        setRetrofitServiceBeanList(retrofitBuilderClassSet);
        setRetrofitClientBeanList(retrofitServiceBeanList);
        setRetrofitServiceBeanHashMap();
        return this;
    }

    public List<RetrofitClientBean> getRetrofitClientBeanList() {
        return retrofitClientBeanList;
    }

    public Map<String, RetrofitServiceBean> getRetrofitServiceBeanHashMap() {
        return retrofitServiceBeanHashMap;
    }

    public List<RetrofitServiceBean> getRetrofitServiceBean() {
        return retrofitServiceBeanList;
    }

    private void setRetrofitServiceBeanHashMap() {
        for (RetrofitClientBean retrofitClient : getRetrofitClientBeanList()) {
            for (RetrofitServiceBean retrofitService : retrofitClient.getRetrofitServices()) {
                retrofitServiceBeanHashMap.put(retrofitService.getSelfClazz().getName(), retrofitService);
            }
        }
    }

    private void setRetrofitServiceBeanList(Set<Class<?>> retrofitBuilderClassSet) {
        RetrofitServiceBeanGenerator serviceBeanHandler;
        for (Class<?> clazz : retrofitBuilderClassSet) {
            serviceBeanHandler = new RetrofitServiceBeanGenerator(clazz, environment);
            final RetrofitServiceBean serviceBean = serviceBeanHandler.generate();
            if (serviceBean != null) {
                retrofitServiceBeanList.add(serviceBean);
            }
        }
    }

    private void setRetrofitClientBeanList(List<RetrofitServiceBean> serviceBeanList) {
        RetrofitClientBeanGenerator clientBeanHandler;
        for (RetrofitServiceBean serviceBean : serviceBeanList) {
            clientBeanHandler = new RetrofitClientBeanGenerator(retrofitClientBeanList, serviceBean);
            final RetrofitClientBean retrofitClientBean = clientBeanHandler.generate();
            if (retrofitClientBean != null && retrofitClientBeanList.stream().noneMatch(clientBean -> clientBean.getRetrofitInstanceName().equals(retrofitClientBean.getRetrofitInstanceName()))) {
                retrofitClientBeanList.add(retrofitClientBean);
            }
        }
    }


}
