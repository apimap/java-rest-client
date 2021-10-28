import io.apimap.api.rest.ApiDataRestEntity;
import io.apimap.api.rest.ApiVersionDataRestEntity;
import io.apimap.api.rest.MetadataDataRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import io.apimap.client.IRestClient;
import io.apimap.client.RestClientConfiguration;
import io.apimap.client.exception.IncorrectTokenException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class QueryQueueTest {
    @Test
    public void createAndUploadMetadata_didSucceed() throws IncorrectTokenException, IOException {

        MetadataDataRestEntity metadataDataApiEntity = new MetadataDataRestEntity(
                "name",
                "description",
                "visibility",
                "apiVersion",
                "releaseStatus",
                "interfaceSpecification",
                "interfaceDescriptionLanguage",
                "architectureLayer",
                "businessUnit",
                "systemIdentifier",
                Arrays.asList(new String[]{"url1", "url2"})
        );

        ApiDataRestEntity apiDataApiEntity = new ApiDataRestEntity(
                metadataDataApiEntity.getName(),
                "git://"
        );

        ApiVersionDataRestEntity apiVersionDataApiEntity = new ApiVersionDataRestEntity(
                metadataDataApiEntity.getApiVersion()
        );


        Consumer<Object> apiCreatedCallback = content -> {
            System.out.println(content);
        };

        Consumer<Object> apiVersionCreatedCallback = content -> {
            System.out.println(content);
        };

        Consumer<String> errorHandlerCallback = content -> {
            System.out.println(content);
        };

        RestClientConfiguration configuration = new RestClientConfiguration("http://localhost:8080");
        configuration.setToken("c1ceb4f6-b4b3-4553-b1b7-9fe5611b019b");

        MetadataDataRestEntity object = IRestClient.withConfiguration(configuration)
                .followCollection(JsonApiRestResponseWrapper.API_COLLECTION)
                .followCollection(metadataDataApiEntity.getName(), JsonApiRestResponseWrapper.VERSION_COLLECTION)
                .onMissingCreate(metadataDataApiEntity.getName(), apiDataApiEntity, apiCreatedCallback)
                .followResource(metadataDataApiEntity.getApiVersion())
                .onMissingCreate(metadataDataApiEntity.getApiVersion(), apiVersionDataApiEntity, apiVersionCreatedCallback)
                .followCollection(JsonApiRestResponseWrapper.METADATA_COLLECTION)
                .createOrUpdateResource(metadataDataApiEntity);

        assertNotNull(object);
    }
}
