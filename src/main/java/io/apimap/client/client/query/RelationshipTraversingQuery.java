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


import io.apimap.api.rest.jsonapi.JsonApiRelationships;
import io.apimap.api.rest.jsonapi.JsonApiRestResponseWrapper;

import java.util.LinkedHashMap;
import java.util.List;

public class RelationshipTraversingQuery extends ApiQuery {

    protected String relationshipId;

    public RelationshipTraversingQuery(String key, String relationshipId) {
        super(TYPE.RELATIONSHIP_TRAVERSING, key);
        this.relationshipId = relationshipId;
    }

    public String getRelationshipId() {
        return relationshipId;
    }

    @Override
    public String urlFromContent(JsonApiRestResponseWrapper content) {
        if(content == null) return null;

        List<LinkedHashMap<String, Object>> elements = (List<LinkedHashMap<String, Object>>) content.getData();
        if(elements == null) return null;

        for (LinkedHashMap<String, Object> ele : elements) {
            if (ele != null
                    && ele.get("id") != null
                    && ele.get("id").equals(relationshipId)) {

                LinkedHashMap value = (LinkedHashMap) ele.get("relationships");
                if(value == null) return null;

                if (value.get(key) != null) {
                    return (String) ((LinkedHashMap) ((LinkedHashMap) value.get(key)).get("links")).get("self");
                }
            }
        }

        return null;
    }

    public String urlFromEntity(JsonApiRelationships content) {
        if(content == null) return null;

        if (content.getRelationships() != null
                && content.getRelationships().get(key) != null) {
            return (String) content.getRelationships().get(key).getLinks().get("self");
        }
        return null;
    }

    @Override
    public String toString() {
        return "RelationshipApiQuery{" +
                "relationshipId='" + relationshipId + '\'' +
                ", type=" + type +
                ", key='" + key + '\'' +
                '}';
    }
}
