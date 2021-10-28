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

public abstract class ApiQuery {
    public enum TYPE {
        CREATE_RESOURCE,
        COLLECTION_TRAVERSING,
        RELATIONSHIP_TRAVERSING,
        RESOURCE_TRAVERSING
    }

    protected TYPE type;
    protected String key;

    public ApiQuery() {
    }

    public ApiQuery(TYPE type, String key) {
        this.type = type;
        this.key = key;
    }

    public TYPE getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public abstract String urlFromContent(JsonApiRestResponseWrapper<?> content);

    @Override
    public String toString() {
        return "Query{" +
                "type=" + type +
                ", key='" + key + '\'' +
                '}';
    }

}
