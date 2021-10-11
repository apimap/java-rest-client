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

package io.apimap.client.client.query;

import io.apimap.api.rest.jsonapi.JsonApiRootObject;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

public class CreateApiQuery extends ApiQuery {
    protected Object object;
    protected Consumer<Object> callback;
    Class resourceClassType;

    public CreateApiQuery(String key, Object object, Consumer<Object> callback, Class resourceClassType) {
        super(TYPE.CREATE, key);
        this.object = object;
        this.callback = callback;
        this.resourceClassType = resourceClassType;
    }

    public Object getObject() {
        return object;
    }

    public Class getResourceClassType() {
        return resourceClassType;
    }

    public Consumer<Object> getCallback() {
        return callback;
    }

    @Override
    public String urlFromContent(JsonApiRootObject content) {
        List<LinkedHashMap<String, Object>> elements = (List<LinkedHashMap<String, Object>>) content.getData();

        for (LinkedHashMap<String, Object> ele : elements) {
            if (ele.get("id").equals(key)) {
                return ((LinkedHashMap<String, String>) ele.get("links")).get("self");
            }
        }

        return null;
    }

    @Override
    public String urlFromAction(URI uri) {
        return null;
    }

    @Override
    public String toString() {
        return "CreateApiQuery{" +
                "object=" + object +
                ", callback=" + callback +
                ", resourceClassType=" + resourceClassType +
                ", type=" + type +
                ", key='" + key + '\'' +
                '}';
    }
}
