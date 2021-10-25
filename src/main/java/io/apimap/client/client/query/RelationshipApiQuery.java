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


import io.apimap.api.rest.DataRestEntity;
import io.apimap.api.rest.jsonapi.JsonApiRelationships;
import io.apimap.api.rest.jsonapi.JsonApiRootObject;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;

public class RelationshipApiQuery extends ApiQuery {

    protected String relationshipId;

    public RelationshipApiQuery(String key, String relationshipId) {
        super(TYPE.RELATIONSHIP, key);
        this.relationshipId = relationshipId;
    }

    public String getRelationshipId() {
        return relationshipId;
    }

    @Override
    public String urlFromContent(JsonApiRootObject content) {

        List<LinkedHashMap<String, Object>> elements = (List<LinkedHashMap<String, Object>>) content.getData();

        for (LinkedHashMap<String, Object> ele : elements) {
            if (ele.get("id").equals(relationshipId)) {

                LinkedHashMap value = (LinkedHashMap) ele.get("relationships");

                if (value.get(key) != null) {
                    return (String) ((LinkedHashMap) ((LinkedHashMap) value.get(key)).get("links")).get("self");
                }
            }
        }

        return null;
    }

    public String urlFromEntity(JsonApiRelationships content) {
        if (content.getRelationships().get(key) != null) {
            return (String) content.getRelationships().get(key).getLinks().get("self");
        }
        return null;
    }

    @Override
    public String urlFromAction(URI uri) {
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
