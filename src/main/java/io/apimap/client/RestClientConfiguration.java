/*
Copyright 2021-2023 TELENOR NORGE AS

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package io.apimap.client;

import java.io.Serializable;

public class RestClientConfiguration implements Serializable {
    private static final int DEFAULT_CALLSTACK_MAX_DEPTH = 10;
    private static final boolean DEFAULT_LOGGER_ENABLED = false;

    private boolean dryRunMode = false;
    private boolean debugMode = DEFAULT_LOGGER_ENABLED;

    private String secret;
    private String account;
    private String zeroconfURL;

    private Integer queryCallstackDepth = DEFAULT_CALLSTACK_MAX_DEPTH;

    public RestClientConfiguration() {
    }

    public RestClientConfiguration(final String zeroconfURL) {
        this.zeroconfURL = zeroconfURL;
    }

    public RestClientConfiguration(final String account,
                                   final String secret,
                                   final String zeroconfURL) {
        this.secret = secret;
        this.zeroconfURL = zeroconfURL;
        this.queryCallstackDepth = DEFAULT_CALLSTACK_MAX_DEPTH;
    }

    public RestClientConfiguration(final String account,
                                   final String secret,
                                   final String zeroconfURL,
                                   final Integer queryCallstackDepth) {
        this.account = account;
        this.secret = secret;
        this.zeroconfURL = zeroconfURL;
        this.queryCallstackDepth = queryCallstackDepth;
    }

    public RestClientConfiguration(final String account,
                                   final String secret,
                                   final String zeroconfURL,
                                   final boolean enableLogger) {
        this.account = account;
        this.secret = secret;
        this.zeroconfURL = zeroconfURL;
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

    public String getSecret() {
        return secret;
    }

    public String getZeroconfURL() {
        return zeroconfURL;
    }

    public Integer getQueryCallstackDepth() {
        return queryCallstackDepth;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setZeroconfURL(String zeroconfURL) {
        this.zeroconfURL = zeroconfURL;
    }

    @Override
    public String toString() {
        return "RestClientConfiguration{" +
            "dryRunMode=" + dryRunMode +
            ", debugMode=" + debugMode +
            ", secret='" + secret + '\'' +
            ", account='" + account + '\'' +
            ", zeroconfURL='" + zeroconfURL + '\'' +
            ", queryCallstackDepth=" + queryCallstackDepth +
            '}';
    }
}
