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

import io.apimap.client.exception.IncorrectTokenException;
import org.apache.hc.core5.http.ContentType;

import java.io.IOException;
import java.util.function.Consumer;

public interface IRestClient {
    public RestClient withErrorHandler(Consumer<String> callback);
    public RestClient withApiToken(String token);

    // Traversing methods
    public RestClient followCollection(String relationshipId, String key);
    public RestClient followResource(String resourceKey);
    public RestClient onMissingCreate(String key, Object object, Consumer<Object> callback);
    public RestClient followCollection(String collectionKey);

    // State change
    public int deleteResource() throws IOException, IncorrectTokenException;
    public <T> T getResource(Class<T> resourceClassType, ContentType contentType) throws IOException, IncorrectTokenException;
    public <T> T createResource(T resource, ContentType contentType) throws IOException, IncorrectTokenException;
    public <T> T createOrUpdateResource(T resource, ContentType contentType) throws IOException, IncorrectTokenException;

    public static
    RestClient withConfiguration(RestClientConfiguration configuration) {
        return new RestClient(configuration);
    }
}
