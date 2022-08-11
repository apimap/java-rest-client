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

import io.apimap.api.rest.ApiDataRestEntity;
import io.apimap.client.RestClientConfiguration;
import io.apimap.client.exception.ApiRequestFailedException;
import io.apimap.client.exception.IllegalApiContentException;
import org.apache.hc.client5.http.HttpHostConnectException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.message.StatusLine;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseRestClientTest {
    @Test
    void getResource_didFailConnectionError(){
        RestClientConfiguration configuration = new RestClientConfiguration();
        SurrogateBaseRestClient client = new SurrogateBaseRestClient(configuration);

        assertThrows(HttpHostConnectException.class,
                ()->{
                    ApiDataRestEntity object = new ApiDataRestEntity();
                    client.postResource(new HttpPost(new java.net.URI("http://localhost:1")), object, ApiDataRestEntity.class);
                });
    }

    @Test
    void getResource_didFail5xxError() throws IOException {
        RestClientConfiguration configuration = new RestClientConfiguration();

        HttpEntity entity = mock(HttpEntity.class);
        String initialString = "";
        when(entity.getContent()).thenReturn(new ByteArrayInputStream(initialString.getBytes()));

        CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
        when(httpResponse.getCode()).thenReturn(500);
        when(httpResponse.getEntity()).thenReturn(entity);

        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        when(httpClient.execute(any())).thenReturn(httpResponse);

        SurrogateBaseRestClient client = new SurrogateBaseRestClient(configuration, httpClient);

        assertThrows(ApiRequestFailedException.class,
                ()->{
                    ApiDataRestEntity object = new ApiDataRestEntity();
                    client.postResource(new HttpPost(new java.net.URI("http://localhost:8080")), object, ApiDataRestEntity.class);
                });
    }

    @Test
    void createResource_didFail4xxError() throws IOException {
        RestClientConfiguration configuration = new RestClientConfiguration();

        HttpEntity entity = mock(HttpEntity.class);
        String initialString = "";
        when(entity.getContent()).thenReturn(new ByteArrayInputStream(initialString.getBytes()));

        CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
        when(httpResponse.getCode()).thenReturn(401);
        when(httpResponse.getEntity()).thenReturn(entity);

        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        when(httpClient.execute(any())).thenReturn(httpResponse);

        SurrogateBaseRestClient client = new SurrogateBaseRestClient(configuration, httpClient);

        assertThrows(IllegalApiContentException.class,
                ()->{
                    ApiDataRestEntity object = new ApiDataRestEntity();
                    client.postResource(new HttpPost(new java.net.URI("http://localhost:8080")), object, ApiDataRestEntity.class);
                });
    }
}
