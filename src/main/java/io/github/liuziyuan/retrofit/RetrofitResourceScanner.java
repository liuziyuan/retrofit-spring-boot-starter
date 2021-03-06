package io.github.liuziyuan.retrofit;

import io.github.liuziyuan.retrofit.annotation.RetrofitBase;
import io.github.liuziyuan.retrofit.annotation.RetrofitBuilder;
import io.github.liuziyuan.retrofit.exception.ProxyTypeIsNotInterfaceException;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Scan retrofit API resources using @RetrofitBuilder
 *
 * @author liuziyuan
 */
@Slf4j
public class RetrofitResourceScanner {

    public Set<Class<?>> doScan(String... basePackages) {
        ConfigurationBuilder configuration;
        if (basePackages.length == 0) {
            configuration = new ConfigurationBuilder().forPackages("");
        } else {
            Pattern filterPattern = Pattern.compile(Arrays.stream(basePackages)
                    .map(s -> s.replace(".", "/"))
                    .collect(Collectors.joining("|", ".*?(", ").*?")));
            log.debug("Scanner Pattern : {}", filterPattern.pattern());
            configuration = new ConfigurationBuilder().forPackages(basePackages).filterInputsBy(s ->
            {
                log.debug("Filter inputs {}", s);
                return filterPattern.matcher(s).matches();
            });

        }
        Reflections reflections = new Reflections(configuration);
        final Set<Class<?>> retrofitBuilderClasses = getRetrofitResourceClasses(reflections, RetrofitBuilder.class);
        final Set<Class<?>> retrofitBaseApiClasses = getRetrofitResourceClasses(reflections, RetrofitBase.class);

        retrofitBuilderClasses.addAll(retrofitBaseApiClasses);
        return retrofitBuilderClasses;
    }

    private Set<Class<?>> getRetrofitResourceClasses(Reflections reflections, Class<? extends Annotation> annotationClass) {
        final Set<Class<?>> classSet = reflections.getTypesAnnotatedWith(annotationClass);
        for (Class<?> clazz : classSet) {
            if (!clazz.isInterface()) {
                throw new ProxyTypeIsNotInterfaceException("[" + clazz.getName() + "] requires an interface type");
            }
        }
        return classSet;
    }


}
