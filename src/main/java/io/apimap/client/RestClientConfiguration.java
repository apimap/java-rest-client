/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 */

package io.apimap.client;

import java.io.Serializable;

public class RestClientConfiguration implements Serializable {
    private static final int DEFAULT_CALLSTACK_MAX_DEPTH = 10;
    private static final boolean DEFAULT_LOGGER_ENABLED = false;

    private boolean dryRunMode = false;
    private boolean debugMode = DEFAULT_LOGGER_ENABLED;
    private String token;
    private String serviceRootEndpointUrl;
    private Integer queryCallstackDepth = DEFAULT_CALLSTACK_MAX_DEPTH;


    public RestClientConfiguration() {
    }

    public RestClientConfiguration(String serviceRootEndpointUrl) {
        this.serviceRootEndpointUrl = serviceRootEndpointUrl;
    }

    public RestClientConfiguration(String token, String serviceRootEndpointUrl) {
        this.token = token;
        this.serviceRootEndpointUrl = serviceRootEndpointUrl;
        this.queryCallstackDepth = DEFAULT_CALLSTACK_MAX_DEPTH;
    }

    public RestClientConfiguration(String token, String serviceRootEndpointUrl, Integer queryCallstackDepth) {
        this.token = token;
        this.serviceRootEndpointUrl = serviceRootEndpointUrl;
        this.queryCallstackDepth = queryCallstackDepth;
    }

    public RestClientConfiguration(String token, String serviceRootEndpointUrl, boolean enableLogger) {
        this.token = token;
        this.serviceRootEndpointUrl = serviceRootEndpointUrl;
        this.debugMode = enableLogger;
    }

    public boolean isDryRunMode() {
        return dryRunMode;
    }

    public void setDryRunMode(boolean dryRunMode) {
        this.dryRunMode = dryRunMode;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public String getToken() {
        return token;
    }

    public String getServiceRootEndpointUrl() {
        return serviceRootEndpointUrl;
    }

    public Integer getQueryCallstackDepth() {
        return queryCallstackDepth;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "RestClientConfiguration{" +
                "logger=" + debugMode +
                ", token='" + token + '\'' +
                ", endpoint='" + serviceRootEndpointUrl + '\'' +
                ", retries=" + queryCallstackDepth +
                '}';
    }
}
