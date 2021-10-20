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
import io.apimap.api.rest.jsonapi.JsonApiRootObject;
import io.apimap.client.RestClientConfiguration;
import io.apimap.client.client.query.ApiQuery;
import io.apimap.client.client.query.CreateApiQuery;
import io.apimap.client.exception.ApiRequestFailedException;
import io.apimap.client.exception.IllegalApiContentException;
import io.apimap.client.exception.IncorrectTokenException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

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
        return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    protected CloseableHttpClient defaultCloseableHttpClient() {
        if(this.httpClient != null) return this.httpClient;
        return HttpClients.createDefault();
    }

    protected void addApiQuery(ApiQuery query) {
        queries.add(query);
    }

    protected URI performQueries(CloseableHttpClient client) throws IOException, ApiRequestFailedException, IllegalApiContentException, IncorrectTokenException {
        if(configuration.isDebugMode()){ System.out.println("Run " + this.queries.size() + " http queries"); }
        if(configuration.isDebugMode()){ System.out.println("Using configuration: " + this.configuration); }
        if(configuration.isDebugMode()){ System.out.println("Http Client: " + client); }

        return enumerateQueries(
                new HttpGet(configuration.getServiceRootEndpointUrl()),
                queries,
                client,
                configuration.getQueryCallstackDepth());
    }

    protected URI enumerateQueries(HttpGet request, ArrayList<ApiQuery> remainingQueries, CloseableHttpClient client, int queryCallstackDepth) throws IOException, ApiRequestFailedException, IllegalApiContentException, IncorrectTokenException {
        if(configuration.isDebugMode()){ System.out.println("Enumerating http queries"); }
        if(configuration.isDebugMode()){ System.out.println("Http Client: " + client); }
        if(configuration.isDebugMode()){ System.out.println("Http Request: " + request); }
        if(configuration.isDebugMode()){ System.out.println("Callstack depth left: " + queryCallstackDepth); }

        if (queryCallstackDepth < MINIMUM_CALLSTACK_DEPTH || queryCallstackDepth > ABSOLUTE_MAX_CALLSTACK_DEPTH) {
            if(configuration.isDebugMode()){ System.out.println("Max callstack depth reached, quitting"); }
            return null;
        }

        if (remainingQueries.size() <= 0) {
            if(configuration.isDebugMode()){ System.out.println("Last query reached, returning url " + request.getURI()); }
            return request.getURI();
        }

        ApiQuery query = remainingQueries.get(0);
        if(configuration.isDebugMode()){ System.out.println("Running query " + query.getType()); }

        if (query.getType() == ApiQuery.TYPE.CREATE) {
            return enumerateQueries(
                    request,
                    new ArrayList<ApiQuery>(remainingQueries.subList(1, remainingQueries.size())),
                    client,
                    --queryCallstackDepth
            );
        }

        CloseableHttpResponse response = client.execute(request);
        JsonApiRootObject element = defaultObjectMapper().readValue(response.getEntity().getContent(), JsonApiRootObject.class);

        String url = query.urlFromContent(element);

        // Check if next query is a create query and current request failed
        if (remainingQueries.size() > 1 && url == null) {
            if (remainingQueries.get(1).getType() == ApiQuery.TYPE.CREATE) {
                HttpPost postRequest = new HttpPost(request.getURI());

                CreateApiQuery createQuery = (CreateApiQuery) remainingQueries.get(1);
                Object content = postResource(postRequest, createQuery.getObject(), createQuery.getResourceClassType());

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

                url = request.getURI().toString();

                return enumerateQueries(
                        new HttpGet(url),
                        remainingQueries,
                        client,
                        --queryCallstackDepth
                );
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
        try {
            if (this.configuration.getToken() != null) {
                deleteRequest.setHeader("Authorization", defaultAuthorizationHeaderValue());
            }

            CloseableHttpResponse response = defaultCloseableHttpClient().execute(deleteRequest);

            if(response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() > 299){
                throw new ApiRequestFailedException(response.getEntity().getContent().toString());
            }

            return responsStatusCode(response);
        } catch (Exception e) {
            throw new ApiRequestFailedException(e.getMessage());
        }
    }

    protected <T> T getResource(HttpGet getRequest, Class<T> resourceClassType) throws ApiRequestFailedException, IncorrectTokenException {
        try {
            CloseableHttpResponse response = defaultCloseableHttpClient().execute(getRequest);
            return responseResourceObject(response, resourceClassType);
        } catch (Exception e) {
            throw new ApiRequestFailedException(e.getMessage());
        }
    }

    protected <T> T putResource(HttpPut putRequest, Object content, Class<T> resourceClassType) throws ApiRequestFailedException, IncorrectTokenException {
        try {
            HttpEntity entity = new StringEntity(
                    defaultObjectMapper().writeValueAsString(content),
                    ContentType.create("application/json")
            );

            putRequest.setEntity(entity);

            if (this.configuration.getToken() != null) {
                putRequest.setHeader("Authorization", defaultAuthorizationHeaderValue());
            }

            CloseableHttpResponse response = defaultCloseableHttpClient().execute(putRequest);

            if(response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() > 299){
                throw new ApiRequestFailedException(response.getEntity().getContent().toString());
            }

            return responseResourceObject(response, resourceClassType);
        } catch (Exception e) {
            throw new ApiRequestFailedException(e.getMessage());
        }
    }

    protected <T> T postResource(HttpPost postRequest, Object content, Class<T> resourceClassType) throws IllegalApiContentException, IncorrectTokenException, HttpHostConnectException, ApiRequestFailedException {
        try {
            HttpEntity entity = new StringEntity(
                    defaultObjectMapper().writeValueAsString(content),
                    ContentType.create("application/json"));

            postRequest.setEntity(entity);

            if (this.configuration.getToken() != null) {
                postRequest.setHeader("Authorization", defaultAuthorizationHeaderValue());
            }

            CloseableHttpResponse response = defaultCloseableHttpClient().execute(postRequest);


            if(response.getStatusLine().getStatusCode() >= 400 && response.getStatusLine().getStatusCode() < 500){
                throw new IllegalApiContentException(response.getEntity().getContent().toString());
            }

            if(response.getStatusLine().getStatusCode() >= 500 && response.getStatusLine().getStatusCode() < 600){
                throw new ApiRequestFailedException(response.getEntity().getContent().toString());
            }

            return responseResourceObject(response, resourceClassType);
        } catch (HttpHostConnectException | IllegalApiContentException | ApiRequestFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalApiContentException(e.getMessage());
        }
    }

    protected int responsStatusCode(CloseableHttpResponse reponse) throws IOException, IncorrectTokenException {
        if(reponse.getStatusLine().getStatusCode() == 401){
            throw new IncorrectTokenException("Missing API token");
        }

        int returnValue = -1;

        try {
            returnValue = reponse.getStatusLine().getStatusCode();
        } finally {
            reponse.close();
        }

        return returnValue;
    }

    protected <T> T responseResourceObject(CloseableHttpResponse reponse, Class<T> resourceClassType) throws IOException, IncorrectTokenException {
        if(reponse.getStatusLine().getStatusCode() == 401){
            throw new IncorrectTokenException("Missing API token");
        }

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        T returnValue = null;

        try {
            JavaType type = mapper.getTypeFactory().constructParametricType(JsonApiRootObject.class, resourceClassType);
            JsonApiRootObject<T> element = mapper.readValue(reponse.getEntity().getContent(), type);
            returnValue = element.getData();
        } finally {
            reponse.close();
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
