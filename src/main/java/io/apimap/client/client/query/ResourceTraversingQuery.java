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

package io.apimap.client.client.query;

import io.apimap.rest.jsonapi.JsonApiRestResponseWrapper;

import java.util.LinkedHashMap;
import java.util.List;

public class ResourceTraversingQuery extends ApiQuery {

    public ResourceTraversingQuery(String key) {
        super(TYPE.RESOURCE_TRAVERSING, key);
    }

    @Override
    public String urlFromContent(JsonApiRestResponseWrapper content) {
        if(content == null) return null;

        List<LinkedHashMap<String, Object>> elements = (List<LinkedHashMap<String, Object>>) content.getData();
        if(elements == null) return null;

        for (LinkedHashMap<String, Object> ele : elements) {
            if (ele != null
                    && ele.get("id") != null
                    && ele.get("id").equals(key)) {
                return ((LinkedHashMap<String, String>) ele.get("links")).get("self");
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "ResourceApiQuery{" +
                ", type=" + type +
                ", key='" + key + '\'' +
                '}';
    }
}
