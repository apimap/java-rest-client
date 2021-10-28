package io.apimap.client;

import io.apimap.client.exception.IncorrectTokenException;

import java.io.IOException;
import java.util.function.Consumer;

public interface IRestClient {
    public RestClient withErrorHandler(Consumer<String> callback);

    // Traversing methods
    public RestClient followCollection(String relationshipId, String key);
    public RestClient followResource(String resourceKey);
    public RestClient onMissingCreate(String key, Object object, Consumer<Object> callback);
    public RestClient followCollection(String collectionKey);

    // State change
    public int deleteResource() throws IOException, IncorrectTokenException;
    public <T> T getResource(Class<T> resourceClassType) throws IOException, IncorrectTokenException;
    public <T> T createResource(T resource) throws IOException, IncorrectTokenException;
    public <T> T createOrUpdateResource(T resource) throws IOException, IncorrectTokenException;

    public static RestClient withConfiguration(RestClientConfiguration configuration) {
        return new RestClient(configuration);
    }
}
