/**
 * Copyright 2020 Pivotal Software, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.core.instrument.binder.httpcomponents;

import io.micrometer.core.annotation.Incubating;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Incubating(since = "1.4.0")
public class MicrometerHttpClientInterceptor {
    private static final String METER_NAME = "httpcomponents.httpclient.request";

    private final Map<HttpContext, Timer.Sample> timerByHttpContext = new ConcurrentHashMap<>();

    private final HttpRequestInterceptor requestInterceptor;
    private final HttpResponseInterceptor responseInterceptor;

    public MicrometerHttpClientInterceptor(MeterRegistry meterRegistry,
                                           Function<HttpRequest, String> uriMapper,
                                           Iterable<Tag> extraTags,
                                           boolean exportTagsForRoute) {
        this.requestInterceptor = (request, context) -> timerByHttpContext.put(context, Timer.start(meterRegistry)
                .tags("method", request.getRequestLine().getMethod(), "uri", uriMapper.apply(request)));

        this.responseInterceptor = (response, context) -> {
            Timer.Sample sample = timerByHttpContext.remove(context);
            sample.stop(meterRegistry, Timer.builder(METER_NAME)
                    .tags(exportTagsForRoute ? HttpContextUtils.generateTagsForRoute(context) : Tags.empty())
                    .tags(extraTags));
        };
    }

    public HttpRequestInterceptor getRequestInterceptor() {
        return requestInterceptor;
    }

    public HttpResponseInterceptor getResponseInterceptor() {
        return responseInterceptor;
    }
}
