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

import io.apimap.client.client.BaseRestClient;
import io.apimap.client.client.query.CollectionApiQuery;
import io.apimap.client.client.query.CreateApiQuery;
import io.apimap.client.client.query.RelationshipApiQuery;
import io.apimap.client.client.query.ResourceApiQuery;
import io.apimap.client.exception.IncorrectTokenException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

public class RestClient extends BaseRestClient {

    protected Consumer<String> errorHandler;

    public RestClient(RestClientConfiguration configuration) {
        super(configuration);
    }

    public static RestClient withConfiguration(RestClientConfiguration configuration) {
        return new RestClient(configuration);
    }

    public RestClient(RestClientConfiguration configuration, CloseableHttpClient httpClient) {
        super(configuration, httpClient);
    }

    public RestClient(RestClientConfiguration configuration, CloseableHttpClient httpClient, Consumer<String> errorHandler) {
        super(configuration, httpClient);
        this.errorHandler = errorHandler;
    }

    public RestClient withErrorHandler(Consumer<String> handler){
        this.errorHandler = handler;
        return this;
    }

    public RestClient followCollection(String relationshipId, String key) {
        addApiQuery(new RelationshipApiQuery(key, relationshipId));
        return this;
    }

    public RestClient followResource(String key) {
        addApiQuery(new ResourceApiQuery(key));
        return this;
    }

    public RestClient onMissingCreate(String key, Object object, Consumer<Object> callback, Class resourceClassType) {
        addApiQuery(new CreateApiQuery(key, object, callback, resourceClassType));
        return this;
    }

    public RestClient followCollection(String key) {
        addApiQuery(new CollectionApiQuery(key));
        return this;
    }

    public int deleteResource() throws IOException, IncorrectTokenException {

        if(configuration.isDryRunMode()) {
            return 204;
        }

        CloseableHttpClient httpClient = defaultCloseableHttpClient();

        int returnValue = -1;

        try {
            URI contentURI = performQueries(httpClient);
            returnValue = deleteResource(new HttpDelete(contentURI));
        } catch (Exception e) {
            if(this.errorHandler != null){
                this.errorHandler.accept(e.getMessage());
            }
        } finally {
            httpClient.close();
        }

        return returnValue;
    }

    public <T> T getResource(Class<T> resourceClassType) throws IOException, IncorrectTokenException {
        if(configuration.isDryRunMode()) {
            try {
                return resourceClassType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                if(this.errorHandler != null){
                    this.errorHandler.accept(e.getMessage());
                }
                return null;
            }
        }

        CloseableHttpClient httpClient = defaultCloseableHttpClient();

        T returnValue = null;

        try {
            URI contentURI = performQueries(httpClient);
            returnValue = getResource(new HttpGet(contentURI), resourceClassType);
        } catch (Exception e) {
            if(this.errorHandler != null){
                this.errorHandler.accept(e.getMessage());
            }
        } finally {
            httpClient.close();
        }

        return returnValue;
    }

    public <T> T createResource(Object object, Class<T> resourceClassType) throws IOException, IncorrectTokenException {
        if(configuration.isDryRunMode()) {
            try {
                return resourceClassType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                if(this.errorHandler != null){
                    this.errorHandler.accept(e.getMessage());
                }
                return null;
            }
        }

        CloseableHttpClient httpClient = defaultCloseableHttpClient();

        T returnValue = null;

        try {
            URI contentURI = performQueries(httpClient);
            returnValue = postResource(new HttpPost(contentURI), object, resourceClassType);
        } catch (Exception e) {
            if(this.errorHandler != null){
                this.errorHandler.accept(e.getMessage());
            }
        } finally {
            httpClient.close();
        }

        return returnValue;
    }

    public <T> T createOrUpdateResource(Object object, Class<T> resourceClassType) throws IOException, IncorrectTokenException {
        if(configuration.isDryRunMode()) {
            try {
                return resourceClassType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                if(this.errorHandler != null){
                    this.errorHandler.accept(e.getMessage());
                }
                return null;
            }
        }

        CloseableHttpClient httpClient = defaultCloseableHttpClient();

        T returnValue = null;

        try {
            URI contentURI = performQueries(httpClient);
            returnValue = putResource(new HttpPut(contentURI), object, resourceClassType);
        } catch (Exception e) {
            if(this.errorHandler != null){
                this.errorHandler.accept(e.getMessage());
            }
        } finally {
            httpClient.close();
        }

        return returnValue;
    }

    @Override
    public String toString() {
        return "RestClient{" +
                "errorHandler=" + errorHandler +
                ", configuration=" + configuration +
                ", queries=" + queries +
                '}';
    }
}



