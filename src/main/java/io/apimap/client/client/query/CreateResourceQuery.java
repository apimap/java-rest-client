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

import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;
import org.apache.hc.core5.http.ContentType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

public class CreateResourceQuery extends ApiQuery {
    protected Object object;
    protected Consumer<Object> callback;
    Class resourceClassType;
    ContentType contentType;

    public CreateResourceQuery(String key, Object object, Consumer<Object> callback, ContentType contentType) {
        super(TYPE.CREATE_RESOURCE, key);
        this.object = object;
        this.callback = callback;
        this.resourceClassType = object.getClass();
        this.contentType = contentType;
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

    public ContentType getContentType() {
        return contentType;
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
        return "CreateApiQuery{" +
                "object=" + object +
                ", callback=" + callback +
                ", resourceClassType=" + resourceClassType +
                ", contentType=" + contentType +
                ", type=" + type +
                ", key='" + key + '\'' +
                '}';
    }
}
