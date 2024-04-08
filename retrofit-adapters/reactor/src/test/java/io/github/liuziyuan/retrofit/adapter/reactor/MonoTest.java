/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.liuziyuan.retrofit.adapter.reactor;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import reactor.core.publisher.Mono;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static okhttp3.mockwebserver.SocketPolicy.DISCONNECT_AFTER_REQUEST;

public final class MonoTest {
  @Rule public final MockWebServer server = new MockWebServer();
  @Rule public final TestRule pluginsReset = new HooksResetRule();
  @Rule public final RecordingSubscriber.Rule subscriberRule = new RecordingSubscriber.Rule();

  interface Service {
    @GET("/") Mono<String> body();
    @GET("/") Mono<Response<String>> response();
    @GET("/") Mono<Result<String>> result();
  }

  private Service service;

  @Before public void setUp() {
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(server.url("/"))
        .addConverterFactory(new StringConverterFactory())
        .addCallAdapterFactory(ReactorCallAdapterFactory.create())
        .build();
    service = retrofit.create(Service.class);
  }

  @Test public void bodySuccess200() {
    server.enqueue(new MockResponse().setBody("Hi"));

    RecordingSubscriber<String> subscriber = subscriberRule.create();
    service.body().subscribe(subscriber);
    subscriber.assertValue("Hi").assertComplete();
  }

  @Test public void bodySuccess404() {
    server.enqueue(new MockResponse().setResponseCode(404));

    RecordingSubscriber<String> subscriber = subscriberRule.create();
    service.body().subscribe(subscriber);
    subscriber.assertError(HttpException.class, "HTTP 404 Client Error");
  }

  @Test public void bodyFailure() {
    server.enqueue(new MockResponse().setSocketPolicy(DISCONNECT_AFTER_REQUEST));

    RecordingSubscriber<String> subscriber = subscriberRule.create();
    service.body().subscribe(subscriber);
    subscriber.assertError(IOException.class);
  }

  @Test public void responseSuccess200() {
    server.enqueue(new MockResponse().setBody("Hi"));

    RecordingSubscriber<Response<String>> subscriber = subscriberRule.create();
    service.response().subscribe(subscriber);
    assertThat(subscriber.takeValue().body()).isEqualTo("Hi");
    subscriber.assertComplete();
  }

  @Test public void responseSuccess404() {
    server.enqueue(new MockResponse().setResponseCode(404));

    RecordingSubscriber<Response<String>> subscriber = subscriberRule.create();
    service.response().subscribe(subscriber);
    assertThat(subscriber.takeValue().code()).isEqualTo(404);
    subscriber.assertComplete();
  }

  @Test public void responseFailure() {
    server.enqueue(new MockResponse().setSocketPolicy(DISCONNECT_AFTER_REQUEST));

    RecordingSubscriber<Response<String>> subscriber = subscriberRule.create();
    service.response().subscribe(subscriber);
    subscriber.assertError(IOException.class);
  }

  @Test public void resultSuccess200() {
    server.enqueue(new MockResponse().setBody("Hi"));

    RecordingSubscriber<Result<String>> subscriber = subscriberRule.create();
    service.result().subscribe(subscriber);
    assertThat(subscriber.takeValue().response().body()).isEqualTo("Hi");
    subscriber.assertComplete();
  }

  @Test public void resultSuccess404() {
    server.enqueue(new MockResponse().setResponseCode(404));

    RecordingSubscriber<Result<String>> subscriber = subscriberRule.create();
    service.result().subscribe(subscriber);
    assertThat(subscriber.takeValue().response().code()).isEqualTo(404);
    subscriber.assertComplete();
  }

  @Test public void resultFailure() {
    server.enqueue(new MockResponse().setSocketPolicy(DISCONNECT_AFTER_REQUEST));

    RecordingSubscriber<Result<String>> subscriber = subscriberRule.create();
    service.result().subscribe(subscriber);
    assertThat(subscriber.takeValue().error()).isInstanceOf(IOException.class);
    subscriber.assertComplete();
  }
}
