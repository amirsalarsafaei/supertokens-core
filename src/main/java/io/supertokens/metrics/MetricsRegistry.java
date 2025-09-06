/*
 *    Copyright (c) 2025, VRAI Labs and/or its affiliates. All rights reserved.
 *
 *    This software is licensed under the Apache License, Version 2.0 (the
 *    "License") as published by the Apache Software Foundation.
 *
 *    You may not use this file except in compliance with the License. You may
 *    obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 */

package io.supertokens.metrics;

import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmInfoMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.supertokens.Main;
import io.supertokens.ResourceDistributor;
import io.supertokens.pluginInterface.metrics.MetricsProvider;
import io.supertokens.pluginInterface.multitenancy.TenantIdentifier;
import io.supertokens.pluginInterface.multitenancy.exceptions.TenantOrAppNotFoundException;

import java.util.List;

public class MetricsRegistry extends ResourceDistributor.SingletonResource implements MetricsProvider {
    private final PrometheusMeterRegistry registry;

    public static synchronized MetricsRegistry getInstance(Main main) {
        MetricsRegistry instance = null;
        try {
            instance = (MetricsRegistry) main.getResourceDistributor()
                    .getResource(TenantIdentifier.BASE_TENANT, RESOURCE_ID);
        } catch (TenantOrAppNotFoundException ignored) {
        }
        return instance;
    }

    public static void initialize(Main main) {
        main.getResourceDistributor()
                .setResource(TenantIdentifier.BASE_TENANT, RESOURCE_ID, new MetricsRegistry());
    }

    private MetricsRegistry() {
        this.registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        registerDefaultMetrics();
    }


    private void registerDefaultMetrics() {
        List.of(
                new JvmInfoMetrics(),
                new JvmMemoryMetrics(),
                new JvmGcMetrics(),
                new JvmThreadMetrics(),
                new ProcessorMetrics()
        ).forEach(binder -> binder.bindTo(registry));
    }

    public PrometheusMeterRegistry getRegistry() {
        return registry;
    }

    public String scrape() {
        return registry.scrape();
    }

    public void registerMetrics(MeterBinder... binders) {
        for (MeterBinder binder : binders) {
            binder.bindTo(registry);
        }
    }
}

