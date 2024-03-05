package io.github.liuziyuan.retrofit.spring.boot;

import io.github.liuziyuan.retrofit.core.Env;
import io.github.liuziyuan.retrofit.core.Extension;
import io.github.liuziyuan.retrofit.core.RetrofitResourceContextBuilder;
import io.github.liuziyuan.retrofit.spring.cloud.RetrofitCloudServiceExtension;

import java.util.List;

public class SpringBootRetrofitResourceContextBuilder extends RetrofitResourceContextBuilder {

    @Override
    public List<Extension> registerExtension(List<Extension> extensions) {
//        extensions.add(new RetrofitCloudServiceExtension());
        return extensions;
    }

    public SpringBootRetrofitResourceContextBuilder(Env env) {
        super(env);
    }


}
