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

package io.apimap.client.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apimap.api.rest.ApiDataRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRestRequestWrapper;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import io.apimap.client.RestClientConfiguration;
import io.apimap.client.client.query.ApiQuery;
import io.apimap.client.client.query.CreateResourceQuery;
import io.apimap.client.client.query.RelationshipTraversingQuery;
import io.apimap.client.exception.ApiRequestFailedException;
import io.apimap.client.exception.IllegalApiContentException;
import io.apimap.client.exception.IncorrectTokenException;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Collectors;

public class BaseRestClient {
    private static final int ABSOLUTE_MAX_CALLSTACK_DEPTH = 20;
    private static final int MINIMUM_CALLSTACK_DEPTH = 0;

    protected RestClientConfiguration configuration;

    protected CloseableHttpClient httpClient;

    protected ArrayList<ApiQuery> queries = new ArrayList<>();

    public BaseRestClient(RestClientConfiguration configuration) {
        this.configuration = configuration;
    }

    public BaseRestClient(RestClientConfiguration configuration, CloseableHttpClient httpClient) {
        this.configuration = configuration;
        this.httpClient = httpClient;
    }

    protected ObjectMapper defaultObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    }

    protected CloseableHttpClient defaultCloseableHttpClient() {
        if(this.httpClient != null) return this.httpClient;
        return HttpClients.createDefault();
    }

    protected void addApiQuery(ApiQuery query) {
        queries.add(query);
    }

    protected URI performQueries(CloseableHttpClient client) throws IOException, ApiRequestFailedException, IllegalApiContentException, IncorrectTokenException, URISyntaxException {
        if(configuration.isDebugMode()){ System.out.println("Run " + this.queries.size() + " http queries"); }
        if(configuration.isDebugMode()){ System.out.println("Using configuration: " + this.configuration); }
        if(configuration.isDebugMode()){ System.out.println("Http Client: " + client); }

        return enumerateQueries(
                new HttpGet(configuration.getServiceRootEndpointUrl()),
                queries,
                client,
                configuration.getQueryCallstackDepth());
    }

    protected URI enumerateQueries(HttpGet request, ArrayList<ApiQuery> remainingQueries, CloseableHttpClient client, int queryCallstackDepth) throws IOException, ApiRequestFailedException, IllegalApiContentException, IncorrectTokenException, URISyntaxException {
        if(configuration.isDebugMode()){ System.out.println("Enumerating http queries"); }
        if(configuration.isDebugMode()){ System.out.println("Http Client: " + client); }
        if(configuration.isDebugMode()){ System.out.println("Http Request: " + request); }
        if(configuration.isDebugMode()){ System.out.println("Callstack depth left: " + queryCallstackDepth); }

        if (queryCallstackDepth < MINIMUM_CALLSTACK_DEPTH || queryCallstackDepth > ABSOLUTE_MAX_CALLSTACK_DEPTH) {
            if(configuration.isDebugMode()){ System.out.println("Max callstack depth reached, quitting"); }
            return null;
        }

        if (remainingQueries.size() <= 0) {
            if(configuration.isDebugMode()){ System.out.println("Last query reached, returning url " + request.getUri()); }
            return request.getUri();
        }

        ApiQuery query = remainingQueries.get(0);
        if(configuration.isDebugMode()){ System.out.println("Running query " + query.getType()); }

        if (query.getType() == ApiQuery.TYPE.CREATE_RESOURCE) {
            return enumerateQueries(
                    request,
                    new ArrayList<ApiQuery>(remainingQueries.subList(1, remainingQueries.size())),
                    client,
                    --queryCallstackDepth
            );
        }

        /*
        JavaType type = objectMapper.getTypeFactory().constructParametricType(JsonApiRootObject.class, ApiDataRestEntity.class);
        JsonApiRootObject<ApiDataRestEntity> output = objectMapper.readValue(input, type);
        */
        CloseableHttpResponse response = client.execute(request);
        String url = null;

        if(response != null
                && response.getEntity() != null
                && (response.getCode() < 299 && response.getCode() >= 200)) {
            JsonApiRestResponseWrapper element = defaultObjectMapper().readValue(response.getEntity().getContent(), JsonApiRestResponseWrapper.class);
            url = query.urlFromContent(element);
        }else{
            if (configuration.isDebugMode()) {
                System.out.println("CloseableHttpResponse returned unusable response");

                if(response != null
                    && response.getEntity() != null
                    && response.getEntity().getContent() != null) {

                    Scanner sc = new Scanner(response.getEntity().getContent());
                    StringBuilder responseBody = new StringBuilder();
                    while(sc.hasNext()){
                        responseBody.append(sc.nextLine());
                    }
                }
            }
        }

        // Check if next query is a create query and current request failed
        if (remainingQueries.size() > 1 && url == null) {
            if (remainingQueries.get(1).getType() == ApiQuery.TYPE.CREATE_RESOURCE) {
                HttpPost postRequest = new HttpPost(request.getUri());

                CreateResourceQuery createQuery = (CreateResourceQuery) remainingQueries.get(1);
                Object content = postResource(postRequest, createQuery.getObject(), createQuery.getResourceClassType(), createQuery.getContentType());

                if (content == null) {
                    return null; //Todo throw exception
                }

                if (createQuery.getCallback() != null) {
                    if(configuration.isDebugMode()){ System.out.println("New resource created, calling callback"); }

                    if (createQuery.getResourceClassType() == ApiDataRestEntity.class) {
                        // Store api key
                        if(((ApiDataRestEntity) content).getMeta() != null) {
                            this.configuration.setToken(((ApiDataRestEntity) content).getMeta().getToken());
                        }
                        createQuery.getCallback().accept(content);
                    } else {
                        createQuery.getCallback().accept(content);
                    }
                }else{
                    if(configuration.isDebugMode()){ System.out.println("New resource created, no callback found"); }
                }

                // Check if next query is a relationship query that works on the content received
                if(remainingQueries.size() > 1 && remainingQueries.get(0).getType() == ApiQuery.TYPE.RELATIONSHIP_TRAVERSING) {
                    url = ((RelationshipTraversingQuery) remainingQueries.get(0)).urlFromEntity(((ApiDataRestEntity) content).getRelationships());

                    return enumerateQueries(
                            new HttpGet(url),
                            new ArrayList<ApiQuery>(remainingQueries.subList(1, remainingQueries.size())),
                            client,
                            --queryCallstackDepth
                    );
                } else {
                    url = request.getUri().toString();

                    return enumerateQueries(
                            new HttpGet(url),
                            remainingQueries,
                            client,
                            --queryCallstackDepth
                    );
                }
            }
        }

        if (url != null) {
            return enumerateQueries(
                    new HttpGet(url),
                    new ArrayList<ApiQuery>(remainingQueries.subList(1, remainingQueries.size())),
                    client,
                    --queryCallstackDepth
            );
        }

        return null;
    }

    protected int deleteResource(HttpDelete deleteRequest) throws ApiRequestFailedException, IncorrectTokenException {
        CloseableHttpResponse response = null;

        try {
            if (this.configuration.getToken() != null) {
                deleteRequest.setHeader("Authorization", defaultAuthorizationHeaderValue());
            }

            response = defaultCloseableHttpClient().execute(deleteRequest);

            if(response.getCode() < 200 || response.getCode() > 299){
                throw new ApiRequestFailedException(String.format(
                        "[DELETE] Status Code: %s, Content: %s, URL: %s",
                        response.getCode(),
                        EntityUtils.toString(response.getEntity(), "UTF-8"),
                        deleteRequest.getUri().toString()
                ));
            }

            return responsStatusCode(response);
        } catch (Exception e) {
            if(this.configuration.isDebugMode()){
                System.out.println(e.getStackTrace());
            }
            throw new ApiRequestFailedException(e.getMessage());
        } finally {
            if(response != null) {
                try {
                    response.close();
                }catch (Exception ignored){

                }
            }
        }
    }

    protected <T> T getResource(HttpGet getRequest, Class<T> resourceClassType, ContentType contentType) throws ApiRequestFailedException, IncorrectTokenException {
        CloseableHttpResponse response = null;

        try {
            response = defaultCloseableHttpClient().execute(getRequest);
            return responseResourceObject(response, resourceClassType, contentType);
        } catch (Exception e) {
            if(this.configuration.isDebugMode()){
                System.out.println(e.getStackTrace());
            }
            throw new ApiRequestFailedException(e.getMessage());
        } finally {
            if(response != null) {
                try {
                    response.close();
                }catch (Exception ignored){}
            }
        }
    }

    protected <T> T putResource(HttpPut putRequest, Object content, Class<T> resourceClassType, ContentType contentType) throws ApiRequestFailedException, IncorrectTokenException {
        CloseableHttpResponse response = null;

        try {
            HttpEntity entity = null;

            if(ContentType.APPLICATION_JSON.isSameMimeType(contentType) || contentType == null) {
                entity = new StringEntity(
                        defaultObjectMapper().writeValueAsString(new JsonApiRestRequestWrapper<>(content)),
                        ContentType.create("application/json"));
            }

            if(ContentType.create("text/markdown").isSameMimeType(contentType)){
                entity = new StringEntity(
                        (String) content,
                        ContentType.create("text/markdown"));
            }

            String text = new BufferedReader(
                    new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            putRequest.setEntity(entity);

            if (this.configuration.getToken() != null) {
                putRequest.setHeader("Authorization", defaultAuthorizationHeaderValue());
            }

            response = defaultCloseableHttpClient().execute(putRequest);

            if(response.getCode() < 200 || response.getCode() > 299){
                throw new ApiRequestFailedException(String.format(
                        "[PUT] Status Code: %s, Content: %s, URL: %s",
                        response.getCode(),
                        EntityUtils.toString(response.getEntity(), "UTF-8"),
                        putRequest.getUri().toString()
                ));
            }

            return responseResourceObject(response, resourceClassType, contentType);
        } catch (Exception e) {
            if(this.configuration.isDebugMode()){
                System.out.println(e.getStackTrace());
            }
            throw new ApiRequestFailedException(e.getMessage());
        } finally {
            if(response != null) {
                try {
                    response.close();
                }catch (Exception ignored){

                }
            }
        }
    }

    protected <T> T postResource(HttpPost postRequest, Object content, Class<T> resourceClassType, ContentType contentType) throws IllegalApiContentException, IncorrectTokenException, HttpHostConnectException, ApiRequestFailedException {
        CloseableHttpResponse response = null;

        try {
            HttpEntity entity = null;

            if(ContentType.APPLICATION_JSON.isSameMimeType(contentType) || contentType == null) {
                entity = new StringEntity(
                        defaultObjectMapper().writeValueAsString(new JsonApiRestRequestWrapper<>(content)),
                        ContentType.create("application/json"));
            }

            if(ContentType.create("text/markdown").isSameMimeType(contentType)){
                entity = new StringEntity(
                        (String) content,
                        ContentType.create("text/markdown"));
            }

            postRequest.setEntity(entity);

            if (this.configuration.getToken() != null) {
                postRequest.setHeader("Authorization", defaultAuthorizationHeaderValue());
            }

            response = defaultCloseableHttpClient().execute(postRequest);

            if(response.getCode() >= 400 && response.getCode() < 500){
                throw new IllegalApiContentException(String.format(
                        "[POST] Status Code: %s, Content: %s, URL: %s",
                        response.getCode(),
                        EntityUtils.toString(response.getEntity(), "UTF-8"),
                        postRequest.getUri().toString()
                ));
            }

            if(response.getCode() >= 500 && response.getCode() < 600){
                throw new ApiRequestFailedException(String.format(
                        "Status Code: %s, Content: %s, URL: %s",
                        response.getCode(),
                        EntityUtils.toString(response.getEntity(), "UTF-8"),
                        postRequest.getUri().toString()
                ));
            }

            return responseResourceObject(response, resourceClassType, contentType);
        } catch (HttpHostConnectException | IllegalApiContentException | ApiRequestFailedException e) {
            throw e;
        } catch (Exception e) {
            if(this.configuration.isDebugMode()){
                System.out.println(e.getStackTrace());
            }
            throw new ApiRequestFailedException(e.getMessage());
        } finally {
            if(response != null) {
                try {
                    response.close();
                }catch (Exception ignored){

                }
            }
        }
    }

    protected int responsStatusCode(CloseableHttpResponse response) throws IOException, IncorrectTokenException {
        if(response.getCode() == 401){
            throw new IncorrectTokenException("Missing API token");
        }

        int returnValue = -1;

        try {
            returnValue = response.getCode();
        } finally {
            response.close();
        }

        return returnValue;
    }

    protected <T> T responseResourceObject(CloseableHttpResponse response, Class<T> resourceClassType, ContentType contentType) throws IOException, IncorrectTokenException {
        if(response.getCode() == 401){
            throw new IncorrectTokenException("Missing API token");
        }

        T returnValue = null;

        try {
            if(ContentType.APPLICATION_JSON.isSameMimeType(contentType) || contentType == null) {
                JavaType type = defaultObjectMapper().getTypeFactory().constructParametricType(JsonApiRestResponseWrapper.class, resourceClassType);
                JsonApiRestResponseWrapper<T> element = defaultObjectMapper().readValue(response.getEntity().getContent(), type);
                returnValue = element.getData();
            }

            if(ContentType.create("text/markdown").isSameMimeType(contentType)){
                returnValue = (T) new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));
            }
        } finally {
            response.close();
        }

        return returnValue;
    }

    protected String defaultAuthorizationHeaderValue(){
        return "Bearer " + this.configuration.getToken();
    }

    @Override
    public String toString() {
        return "BaseRestClient{" +
                "configuration=" + configuration +
                ", queries=" + queries +
                '}';
    }
}
