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

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class CollectionTraversingQuery extends ApiQuery {
    public CollectionTraversingQuery(String key) {
        super(TYPE.COLLECTION_TRAVERSING, key);
    }

    @Override
    public String urlFromContent(JsonApiRestResponseWrapper<?> content) {
        if(content.getLinks() == null) return null;

        ArrayList<LinkedHashMap> value = (ArrayList<LinkedHashMap>) content.getLinks().get("related");

        if(value == null) return null;

        for (LinkedHashMap ele : value) {
            if (ele != null
                    && ele.get("rel") != null
                    && ele.get("rel").equals(key)) {
                return (String) ele.get("href");
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
