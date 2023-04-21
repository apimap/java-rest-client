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

package io.apimap.client.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.apimap.api.rest.ApiDataRestEntity;
import io.apimap.client.RestClientConfiguration;
import io.apimap.client.client.query.ApiQuery;
import io.apimap.client.client.query.CreateResourceQuery;
import io.apimap.client.client.query.RelationshipTraversingQuery;
import io.apimap.client.exception.ApiRequestFailedException;
import io.apimap.client.exception.IllegalApiContentException;
import io.apimap.client.exception.IncorrectTokenException;
import io.apimap.client.exception.MissingAccessTokenException;
import io.apimap.oauth.TokenSuccessfulResponse;
import io.apimap.orchestra.rest.ZeroconfConfigurationResponse;
import io.apimap.rest.jsonapi.JsonApiRestRequestWrapper;
import io.apimap.rest.jsonapi.JsonApiRestResponseWrapper;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class BaseRestClient {
    private static final int ABSOLUTE_MAX_CALLSTACK_DEPTH = 20;
    private static final int MINIMUM_CALLSTACK_DEPTH = 0;

    protected Optional<RestClientConfiguration> configuration;

    protected CloseableHttpClient httpClient;

    protected ArrayList<ApiQuery> queries = new ArrayList<>();
    protected Optional<String> apiToken = Optional.empty();

    protected static class Endpoints {
        private String orchestra;
        private String api;

        public Endpoints(String orchestra, String api) {
            this.orchestra = orchestra;
            this.api = api;
        }

        public String getOrchestra() {
            return orchestra;
        }

        public void setOrchestra(String orchestra) {
            this.orchestra = orchestra;
        }

        public String getApi() {
            return api;
        }

        public void setApi(String api) {
            this.api = api;
        }

        @Override
        public String toString() {
            return "Endpoints{" +
                "orchestra='" + orchestra + '\'' +
                ", api='" + api + '\'' +
                '}';
        }
    }

    @SuppressFBWarnings
    public BaseRestClient(RestClientConfiguration configuration) {
        this.configuration = Optional.ofNullable(configuration);
    }

    @SuppressFBWarnings
    public BaseRestClient(RestClientConfiguration configuration, CloseableHttpClient httpClient) {
        this.configuration =  Optional.ofNullable(configuration);
        this.httpClient = httpClient;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = Optional.ofNullable(apiToken);
    }

    private Optional<String> getApiToken(){
        return this.apiToken;
    }

    protected ObjectMapper defaultObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    }

    protected CloseableHttpClient defaultCloseableHttpClient(UUID uuid) {
        if(this.httpClient != null) return this.httpClient;

        final ArrayList<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("X-Request-Id", uuid.toString()));

        return HttpClients.custom().setDefaultHeaders(headers).build();
    }

    protected void addApiQuery(ApiQuery query) {
        queries.add(query);
    }

    protected Optional<Endpoints> getEndpoints(CloseableHttpClient client) throws IOException {
        if(configuration.isPresent() && configuration.get().isDebugMode()){
            System.out.println("[ZEROCONF] Zeroconf from endpoint: " + configuration.get().getZeroconfURL());
        }

        if(!configuration.isPresent() || configuration.get().getZeroconfURL() == null){
            return Optional.empty();
        }

        CloseableHttpResponse response = null;

        Endpoints returnValue = null;

        try {
            response = client.execute(new HttpGet(configuration.get().getZeroconfURL()));

            ZeroconfConfigurationResponse configurationResponse = defaultObjectMapper().readValue(response.getEntity().getContent(), ZeroconfConfigurationResponse.class);

            returnValue = new Endpoints(
                configurationResponse.getEndpoint().getOrchestra(),
                configurationResponse.getEndpoint().getApi()
            );
        } finally {
            if (response != null) {
                response.close();
            }
        }

        return Optional.of(returnValue);
    }

    protected Optional<String> getJwtToken(CloseableHttpClient client, String url) throws IOException, MissingAccessTokenException {
        if(configuration.isPresent() && configuration.get().isDebugMode()){
            System.out.println("[JWT] JWT from endpoint : " + url);
        }

        if(url == null){
            return Optional.empty();
        }

        CloseableHttpResponse response = null;

        String returnValue = null;

        try {
            if(configuration.isPresent() && configuration.get().isDebugMode()){
                System.out.println("[JWT] Request: " + url + "?client_id=" + this.configuration.get().getAccount() +"&client_secret=" + this.configuration.get().getSecret());
            }

            response = client.execute(new HttpPost(url + "?client_id=" + this.configuration.get().getAccount() +"&client_secret=" + this.configuration.get().getSecret()));

            TokenSuccessfulResponse tokenResponse = defaultObjectMapper().readValue(response.getEntity().getContent(), TokenSuccessfulResponse.class);

            returnValue = tokenResponse.getAccessToken();
        } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
            throw new MissingAccessTokenException("Missing access token, client not authorized.");
        } finally {
            if (response != null) {
                response.close();
            }
        }

        if(configuration.isPresent() && configuration.get().isDebugMode()){
            System.out.println("[JWT] Token received: " + returnValue);
        }

        return Optional.of(returnValue);
    }

    protected Optional<URI> performQueries(CloseableHttpClient client, String url, String jwt) throws IOException, ApiRequestFailedException, IllegalApiContentException, IncorrectTokenException, URISyntaxException {
        if(configuration.isPresent() && configuration.get().isDebugMode()){ System.out.println("[ENUMERATING] Run " + this.queries.size() + " http queries"); }
        if(configuration.isPresent() && configuration.get().isDebugMode()){ System.out.println("[ENUMERATING] Configuration: " + this.configuration); }
        if(configuration.isPresent() && configuration.get().isDebugMode()){ System.out.println("[ENUMERATING] Client: " + client); }

        if(configuration.isPresent()){
            return Optional.of(enumerateQueries(
                new HttpGet(url),
                jwt,
                queries,
                client,
                configuration.get().getQueryCallstackDepth()));
        }

        return Optional.empty();
    }

    protected URI enumerateQueries(HttpGet request, String jwt, ArrayList<ApiQuery> remainingQueries, CloseableHttpClient client, int queryCallstackDepth) throws IOException, ApiRequestFailedException, IllegalApiContentException, IncorrectTokenException, URISyntaxException {
        if(configuration.isPresent() && configuration.get().isDebugMode()){ System.out.println("[ENUMERATING] Enumerating http queries"); }
        if(configuration.isPresent() && configuration.get().isDebugMode()){ System.out.println("[ENUMERATING] Client: " + client); }
        if(configuration.isPresent() && configuration.get().isDebugMode()){ System.out.println("[ENUMERATING] Request: " + request); }
        if(configuration.isPresent() && configuration.get().isDebugMode()){ System.out.println("[ENUMERATING] JWT: " + jwt); }
        if(configuration.isPresent() && configuration.get().isDebugMode()){ System.out.println("[ENUMERATING] Callstack depth left: " + queryCallstackDepth); }

        if (queryCallstackDepth < MINIMUM_CALLSTACK_DEPTH || queryCallstackDepth > ABSOLUTE_MAX_CALLSTACK_DEPTH) {
            if(configuration.isPresent() && configuration.get().isDebugMode()){ System.out.println("[ENUMERATING] Max callstack depth reached, quitting"); }
            return null;
        }

        if (remainingQueries.size() <= 0) {
            if(configuration.isPresent() && configuration.get().isDebugMode()){ System.out.println("[ENUMERATING] Last query reached, returning url " + request.getUri()); }
            return request.getUri();
        }

        ApiQuery query = remainingQueries.get(0);
        if(configuration.isPresent() && configuration.get().isDebugMode()){ System.out.println("[ENUMERATING] Running query " + query.getType()); }

        if (query.getType() == ApiQuery.TYPE.CREATE_RESOURCE) {
            return enumerateQueries(
                    request,
                    jwt,
                    new ArrayList<ApiQuery>(remainingQueries.subList(1, remainingQueries.size())),
                    client,
                    --queryCallstackDepth
            );
        }

        request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
        CloseableHttpResponse response = client.execute(request);
        String url = null;

        if(response != null
                && response.getEntity() != null
                && (response.getCode() < 299 && response.getCode() >= 200)) {
            JsonApiRestResponseWrapper element = defaultObjectMapper().readValue(response.getEntity().getContent(), JsonApiRestResponseWrapper.class);
            url = query.urlFromContent(element);
        }else{
            if (configuration.isPresent() && configuration.get().isDebugMode()) {
                System.out.println("[ENUMERATING] CloseableHttpResponse returned unusable response");
            }
        }

        // Check if next query is a create query and current request failed
        if (remainingQueries.size() > 1 && url == null) {
            if (remainingQueries.get(1).getType() == ApiQuery.TYPE.CREATE_RESOURCE) {
                HttpPost postRequest = new HttpPost(request.getUri());
                postRequest.setHeader("Authorization", "Bearer " + jwt);

                CreateResourceQuery createQuery = (CreateResourceQuery) remainingQueries.get(1);
                Object content = postResource(postRequest, createQuery.getObject(), createQuery.getResourceClassType(), createQuery.getContentType(), client);

                if (content == null) {
                    return null; //Todo throw exception
                }

                if (createQuery.getCallback() != null) {
                    if(configuration.isPresent() && configuration.get().isDebugMode()){ System.out.println("[ENUMERATING] New resource created, calling callback"); }

                    if (createQuery.getResourceClassType() == ApiDataRestEntity.class) {
                        // Store api key
                        if(((ApiDataRestEntity) content).getMeta() != null) {
                            this.setApiToken(((ApiDataRestEntity) content).getMeta().getToken());
                        }
                        createQuery.getCallback().accept(content);
                    } else {
                        createQuery.getCallback().accept(content);
                    }
                }else{
                    if(configuration.isPresent() && configuration.get().isDebugMode()){ System.out.println("[ENUMERATING] New resource created, no callback found"); }
                }

                // Check if next query is a relationship query that works on the content received
                if(remainingQueries.size() > 1 && remainingQueries.get(0).getType() == ApiQuery.TYPE.RELATIONSHIP_TRAVERSING) {
                    url = ((RelationshipTraversingQuery) remainingQueries.get(0)).urlFromEntity(((ApiDataRestEntity) content).getRelationships());

                    return enumerateQueries(
                            new HttpGet(url),
                            jwt,
                            new ArrayList<ApiQuery>(remainingQueries.subList(1, remainingQueries.size())),
                            client,
                            --queryCallstackDepth
                    );
                } else {
                    url = request.getUri().toString();

                    return enumerateQueries(
                            new HttpGet(url),
                            jwt,
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
                    jwt,
                    new ArrayList<ApiQuery>(remainingQueries.subList(1, remainingQueries.size())),
                    client,
                    --queryCallstackDepth
            );
        }

        return null;
    }

    protected int deleteResource(HttpDelete deleteRequest, CloseableHttpClient client) throws ApiRequestFailedException, IncorrectTokenException {
        CloseableHttpResponse response = null;

        try {

            if(this.getApiToken().isPresent()) {
                deleteRequest.setHeader("Apimap-Api-Token", defaultAuthorizationHeaderValue());
            }

            if(configuration.isPresent() && configuration.get().isDebugMode()) {
                System.out.println("[DELETE] " + deleteRequest.getHeader("Authorization"));
                System.out.println("[DELETE] " + deleteRequest.getHeader("Apimap-Api-Token"));
            }

            response = client.execute(deleteRequest);

            if(response.getCode() < 200 || response.getCode() > 299){
                throw new ApiRequestFailedException(String.format(
                        "[DELETE] Status Code: %s, Content: %s, URL: %s",
                        response.getCode(),
                        EntityUtils.toString(response.getEntity(), "UTF-8"),
                        deleteRequest.getUri().toString()
                ));
            }

            return responseStatusCode(response);
        } catch (Exception e) {
            if(configuration.isPresent() && configuration.get().isDebugMode()){
                System.out.println(Arrays.toString(e.getStackTrace()));
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

    protected <T> T getResource(HttpGet getRequest, Class<T> resourceClassType, ContentType contentType, CloseableHttpClient client) throws ApiRequestFailedException, IncorrectTokenException {
        CloseableHttpResponse response = null;

        try {
            response = client.execute(getRequest);
            return responseResourceObject(response, resourceClassType, contentType);
        } catch (Exception e) {
            if(configuration.isPresent() && configuration.get().isDebugMode()){
                System.out.println(Arrays.toString(e.getStackTrace()));
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

    protected <T> T putResource(HttpPut putRequest, Object content, Class<T> resourceClassType, ContentType contentType, CloseableHttpClient client) throws ApiRequestFailedException, IncorrectTokenException {
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

            if(configuration.isPresent() && configuration.get().isDebugMode()) {
                System.out.println("[PUT] PUT resource: " + content.toString());
            }

            putRequest.setEntity(entity);

            if (this.getApiToken().isPresent()) {
                putRequest.setHeader("Apimap-Api-Token", defaultAuthorizationHeaderValue());
            }

            if(configuration.isPresent() && configuration.get().isDebugMode()) {
                System.out.println("[PUT] " + putRequest.getHeader("Authorization"));
                System.out.println("[PUT] " + putRequest.getHeader("Apimap-Api-Token"));
            }

            response = client.execute(putRequest);

            if(response.getCode() < 200 || response.getCode() > 299){
                throw new ApiRequestFailedException(String.format(
                        "[PUT] Status Code: %s, Content: %s, URL: %s",
                        response.getCode(),
                        EntityUtils.toString(response.getEntity(), "UTF-8"),
                        putRequest.getUri().toString()
                ));
            }

            return responseResourceObject(response, resourceClassType, contentType);
        } catch (ProtocolException | IOException | URISyntaxException e) {
            if(configuration.isPresent() && configuration.get().isDebugMode()){
                System.out.println(Arrays.toString(e.getStackTrace()));
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

    protected <T> T postResource(HttpPost postRequest, Object content, Class<T> resourceClassType, ContentType contentType, CloseableHttpClient client) throws IllegalApiContentException, IncorrectTokenException, HttpHostConnectException, ApiRequestFailedException {
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

            if (this.getApiToken().isPresent()) {
                postRequest.setHeader("Apimap-Api-Token", defaultAuthorizationHeaderValue());
            }

            if(configuration.isPresent() && configuration.get().isDebugMode()) {
                System.out.println("[POST] " + postRequest.getHeader("Authorization"));
                System.out.println("[POST] " + postRequest.getHeader("Apimap-Api-Token"));
            }

            response = client.execute(postRequest);

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
                        "[POST] Status Code: %s, Content: %s, URL: %s",
                        response.getCode(),
                        EntityUtils.toString(response.getEntity(), "UTF-8"),
                        postRequest.getUri().toString()
                ));
            }

            return responseResourceObject(response, resourceClassType, contentType);
        } catch (HttpHostConnectException | IllegalApiContentException | ApiRequestFailedException e) {
            throw e;
        } catch (Exception e) {
            if(configuration.isPresent() && configuration.get().isDebugMode()){
                System.out.println(Arrays.toString(e.getStackTrace()));
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

    protected int responseStatusCode(CloseableHttpResponse response) throws IOException, IncorrectTokenException {
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
                try(InputStreamReader inputStreamReader = new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader)){
                        returnValue = (T) bufferedReader
                                .lines()
                                .collect(Collectors.joining("\n"));
                }
            }
        } finally {
            response.close();
        }

        return returnValue;
    }

    protected String defaultAuthorizationHeaderValue(){
        if(getApiToken().isPresent()){
            return getApiToken().get();
        }

        return null;
    }

    @Override
    public String toString() {
        return "BaseRestClient{" +
                "configuration=" + configuration +
                ", queries=" + queries +
                '}';
    }
}
