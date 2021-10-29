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
