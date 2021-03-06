/**
 * Copyright 2018 VMware, Inc.
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
package io.micrometer.stackdriver;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.monitoring.v3.MetricServiceSettings;
import io.micrometer.core.instrument.config.MissingRequiredConfigurationException;
import io.micrometer.core.instrument.step.StepRegistryConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * {@link StepRegistryConfig} for Stackdriver.
 *
 * @author Jon Schneider
 * @since 1.1.0
 */
public interface StackdriverConfig extends StepRegistryConfig {

    @Override
    default String prefix() {
        return "stackdriver";
    }

    default String projectId() {
        String v = get(prefix() + ".projectId");
        if (v == null)
            throw new MissingRequiredConfigurationException("projectId must be set to report metrics to Stackdriver");
        return v;
    }

    /**
     * Return resource labels.
     * @return resource labels.
     * @since 1.4.0
     */
    default Map<String, String> resourceLabels() {
        return Collections.emptyMap();
    }

    default String resourceType() {
        String resourceType = get(prefix() + ".resourceType");
        return resourceType == null ? "global" : resourceType;
    }

    /**
     * Return {@link CredentialsProvider} to use.
     * @return {@code CredentialsProvider} to use
     * @throws IOException if a specified file doesn't exist
     * @since 1.4.0
     */
    default CredentialsProvider credentials() throws IOException {
        String credentials = get(prefix() + ".credentials");
        return credentials == null ? MetricServiceSettings.defaultCredentialsProviderBuilder().build()
                : FixedCredentialsProvider.create(GoogleCredentials.fromStream(new FileInputStream(credentials))
                        .createScoped(MetricServiceSettings.getDefaultServiceScopes()));
    }
}
