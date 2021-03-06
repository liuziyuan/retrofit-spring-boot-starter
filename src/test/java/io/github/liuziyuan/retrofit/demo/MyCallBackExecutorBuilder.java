package io.github.liuziyuan.retrofit.demo;

import io.github.liuziyuan.retrofit.extension.BaseCallBackExecutorBuilder;

import java.util.concurrent.Executor;

/**
 * @author liuziyuan
 */
public class MyCallBackExecutorBuilder extends BaseCallBackExecutorBuilder {

    @Override
    public Executor buildCallBackExecutor() {
        return command -> command.run();
    }
}
