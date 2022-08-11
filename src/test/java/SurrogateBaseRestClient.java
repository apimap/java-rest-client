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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.apimap.client.RestClientConfiguration;
import io.apimap.client.client.BaseRestClient;
import io.apimap.client.client.query.ApiQuery;
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
import org.apache.hc.core5.http.ContentType;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class SurrogateBaseRestClient extends BaseRestClient {

    public SurrogateBaseRestClient(RestClientConfiguration configuration, CloseableHttpClient httpClient) {
        super(configuration, httpClient);
    }

    public SurrogateBaseRestClient(RestClientConfiguration configuration) {
        super(configuration);
    }

    public ObjectMapper defaultObjectMapper() {
        return super.defaultObjectMapper();
    }

    public CloseableHttpClient defaultCloseableHttpClient() {
        return super.defaultCloseableHttpClient();
    }

    public void addApiQuery(ApiQuery query) {
        super.addApiQuery(query);
    }

    public URI performQueries(CloseableHttpClient client) throws IOException, ApiRequestFailedException, IllegalApiContentException, IncorrectTokenException, URISyntaxException {
        return super.performQueries(client);
    }

    public URI enumerateQueries(HttpGet request, ArrayList<ApiQuery> remainingQueries, CloseableHttpClient client, int queryCallstackDepth) throws IOException, ApiRequestFailedException, IllegalApiContentException, IncorrectTokenException, URISyntaxException {
        return super.enumerateQueries(request, remainingQueries, client, queryCallstackDepth);
    }

    public int deleteResource(HttpDelete deleteRequest) throws ApiRequestFailedException, IncorrectTokenException {
        return super.deleteResource(deleteRequest);
    }

    public <T> T getResource(HttpGet getRequest, Class<T> resourceClassType) throws ApiRequestFailedException, IncorrectTokenException {
        return super.getResource(getRequest, resourceClassType, ContentType.APPLICATION_JSON);
    }

    public <T> T putResource(HttpPut putRequest, Object content, Class<T> resourceClassType) throws ApiRequestFailedException, IncorrectTokenException {
        return super.putResource(putRequest, content, resourceClassType, ContentType.APPLICATION_JSON);
    }

    public <T> T postResource(HttpPost postRequest, Object content, Class<T> resourceClassType) throws IllegalApiContentException, IncorrectTokenException, HttpHostConnectException, ApiRequestFailedException {
        return super.postResource(postRequest, content, resourceClassType, ContentType.APPLICATION_JSON);
    }

    public int responsStatusCode(CloseableHttpResponse response) throws IOException, IncorrectTokenException {
        return super.responsStatusCode(response);
    }

    public <T> T responseResourceObject(CloseableHttpResponse response, Class<T> resourceClassType) throws IOException, IncorrectTokenException {
        return super.responseResourceObject(response, resourceClassType, ContentType.APPLICATION_JSON);
    }

    public String defaultAuthorizationHeaderValue(){
        return super.defaultAuthorizationHeaderValue();
    }
}
