package io.github.liuziyuan.retrofit.demo.api;


import io.github.liuziyuan.retrofit.annotation.RetrofitInterceptor;
import io.github.liuziyuan.retrofit.demo.MyRetrofitInterceptor3;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * @author liuziyuan
 * @date 1/5/2022 4:57 PM
 */
public interface TestInheritApi2 extends TestMiddleApi {
    @GET("/v1/test2/")
    Call<String> test2();
}
