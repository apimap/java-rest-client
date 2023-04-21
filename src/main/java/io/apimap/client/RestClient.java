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

import io.apimap.client.client.BaseRestClient;
import io.apimap.client.client.query.CollectionTraversingQuery;
import io.apimap.client.client.query.CreateResourceQuery;
import io.apimap.client.client.query.RelationshipTraversingQuery;
import io.apimap.client.client.query.ResourceTraversingQuery;
import io.apimap.client.exception.IncorrectTokenException;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class RestClient extends BaseRestClient implements IRestClient {

    protected Consumer<String> errorHandler;

    public RestClient(RestClientConfiguration configuration) {
        super(configuration);
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

    public RestClient withApiToken(String token){
        this.apiToken = Optional.ofNullable(token);
        return this;
    }

    public RestClient followCollection(String relationshipId, String key) {
        addApiQuery(new RelationshipTraversingQuery(key, relationshipId));
        return this;
    }

    public RestClient followResource(String key) {
        addApiQuery(new ResourceTraversingQuery(key));
        return this;
    }

    public RestClient onMissingCreate(String key, Object object, Consumer<Object> callback) {
        addApiQuery(new CreateResourceQuery(key, object, callback, ContentType.APPLICATION_JSON));
        return this;
    }

    public RestClient followCollection(String key) {
        addApiQuery(new CollectionTraversingQuery(key));
        return this;
    }

    public int deleteResource() throws IOException, IncorrectTokenException {
        if(configuration.isPresent() && configuration.get().isDryRunMode()) {
            return 204;
        }

        CloseableHttpClient httpClient = defaultCloseableHttpClient(UUID.randomUUID());

        int returnValue = -1;

        try {
            Optional<Endpoints> endpoints = getEndpoints(httpClient);

            if(!endpoints.isPresent()){
                this.errorHandler.accept("Unable to get zeroconf");
                return returnValue;
            }

            Optional<String> jwt = getJwtToken(httpClient, endpoints.get().getOrchestra());

            if(!jwt.isPresent()){
                this.errorHandler.accept("Unable to get jwt");
                return returnValue;
            }

            Optional<URI> contentURI = performQueries(httpClient, endpoints.get().getApi(), jwt.get());

            if(!contentURI.isPresent()){
                this.errorHandler.accept("Unable to travers path to resource");
                return returnValue;
            }

            HttpDelete request = new HttpDelete(contentURI.get());
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwt.get());

            returnValue = deleteResource(request, httpClient);
        } catch (Exception e) {
            if(this.errorHandler != null){
                this.errorHandler.accept(e.getMessage());
            }
        } finally {
            httpClient.close();
        }

        return returnValue;
    }

    public <T> T getResource(Class<T> resourceClassType, ContentType contentType) throws IOException, IncorrectTokenException {
        if(configuration.isPresent() && configuration.get().isDryRunMode()) {
            try {
                return resourceClassType.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                if(this.errorHandler != null){
                    this.errorHandler.accept(e.getMessage());
                }
                return null;
            }
        }

        CloseableHttpClient httpClient = defaultCloseableHttpClient(UUID.randomUUID());

        T returnValue = null;

        try {
            Optional<Endpoints> endpoints = getEndpoints(httpClient);

            if(!endpoints.isPresent()){
                this.errorHandler.accept("[GET] Unable to get zeroconf");
                return returnValue;
            }

            Optional<String> jwt = getJwtToken(httpClient, endpoints.get().getOrchestra());

            if(!jwt.isPresent()){
                this.errorHandler.accept("[GET] Unable to get jwt");
                return returnValue;
            }

            Optional<URI> contentURI = performQueries(httpClient, endpoints.get().getApi(), jwt.get());

            if(!contentURI.isPresent()){
                this.errorHandler.accept("[GET] Unable to travers path to resource");
                return returnValue;
            }

            HttpGet request = new HttpGet(contentURI.get());
            request.setHeader("Authorization", "Bearer " + jwt.get());

            returnValue = getResource(request, resourceClassType, contentType, httpClient);
        } catch (Exception e) {
            if(this.errorHandler != null){
                this.errorHandler.accept(e.getMessage());
            }
        } finally {
            httpClient.close();
        }

        return returnValue;
    }

    public <T> T createResource(T object, ContentType contentType) throws IOException, IncorrectTokenException {
        if(configuration.isPresent() && configuration.get().isDryRunMode()) {
            try {
                return (T) object.getClass().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                if(this.errorHandler != null){
                    this.errorHandler.accept(e.getMessage());
                }
                return null;
            }
        }

        CloseableHttpClient httpClient = defaultCloseableHttpClient(UUID.randomUUID());

        T returnValue = null;

        try {
            Optional<Endpoints> endpoints = getEndpoints(httpClient);

            if(!endpoints.isPresent()){
                this.errorHandler.accept("[POST] Unable to get zeroconf");
                return returnValue;
            }

            Optional<String> jwt = getJwtToken(httpClient, endpoints.get().getOrchestra());

            if(!jwt.isPresent()){
                this.errorHandler.accept("[POST] Unable to get jwt");
                return returnValue;
            }

            Optional<URI> contentURI = performQueries(httpClient, endpoints.get().getApi(), jwt.get());

            if(!contentURI.isPresent()){
                this.errorHandler.accept("[POST] Unable to travers path to resource");
                return returnValue;
            }

            HttpPost request = new HttpPost(contentURI.get());
            request.setHeader("Authorization", "Bearer " + jwt.get());

            returnValue = (T) postResource(request, object, object.getClass(), contentType, httpClient);
        } catch (Exception e) {
            if(this.errorHandler != null){
                this.errorHandler.accept(e.getMessage());
            }
        } finally {
            httpClient.close();
        }

        return returnValue;
    }

    public <T> T createOrUpdateResource(T object, ContentType contentType) throws IOException, IncorrectTokenException {
        if(configuration.isPresent() && configuration.get().isDryRunMode()) {
            try {
                return (T) object.getClass().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                if(this.errorHandler != null){
                    this.errorHandler.accept(e.getMessage());
                }
                return null;
            }
        }

        CloseableHttpClient httpClient = defaultCloseableHttpClient(UUID.randomUUID());

        T returnValue = null;

        try {
            Optional<Endpoints> endpoints = getEndpoints(httpClient);

            if(!endpoints.isPresent()){
                this.errorHandler.accept("[PUT] Unable to get zeroconf");
                return returnValue;
            }

            Optional<String> jwt = getJwtToken(httpClient, endpoints.get().getOrchestra());

            if(!jwt.isPresent()){
                this.errorHandler.accept("[PUT] Unable to get jwt");
                return returnValue;
            }

            Optional<URI> contentURI = performQueries(httpClient, endpoints.get().getApi(), jwt.get());

            if(!contentURI.isPresent()){
                this.errorHandler.accept("[PUT] Unable to travers path to resource");
                return returnValue;
            }

            HttpPut request = new HttpPut(contentURI.get());
            request.setHeader("Authorization", "Bearer " + jwt.get());

            returnValue = (T) putResource(request, object, object.getClass(), contentType, httpClient);
        } catch (Exception e) {
            if(configuration.isPresent() && configuration.get().isDryRunMode()) {
                System.out.println("[PUT] Exception: " + e.getMessage());
            }
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



