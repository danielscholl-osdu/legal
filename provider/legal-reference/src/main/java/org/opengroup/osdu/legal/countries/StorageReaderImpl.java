/*
 * Copyright 2021 Google LLC
 * Copyright 2021 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.legal.countries;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;
import org.opengroup.osdu.legal.tags.dataaccess.MongoClientProvider;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class StorageReaderImpl implements IStorageReader {

  private final TenantInfo tenantInfo;
  private final String cloudRegion;
  private final String dbName;
  private final String collectionName;
  private final MongoClientProvider client;

  public StorageReaderImpl(TenantInfo tenantInfo, String projectRegion, String dbName,
      String collectionName, MongoClientProvider client) {
    this.tenantInfo = tenantInfo;
    this.cloudRegion = projectRegion;
    this.collectionName = collectionName;
    this.dbName = dbName;
    this.client = client;
  }

  @Override
  public byte[] readAllBytes() {
    MongoOperations ops = new MongoTemplate(client.getMongoClient(), dbName);

    Query query = new Query(Criteria
        .where("tenant").is(tenantInfo.getName())
        .and("region").is(cloudRegion));

    query.fields().include("name");
    query.fields().include("alpha2");
    query.fields().include("numeric");
    query.fields().include("residencyRisk");
    query.fields().include("typesNotApplyDataResidency");

    List<JsonObject> objects = ops.find(query, JsonObject.class, collectionName);

    JsonArray array = new JsonArray();
    for (JsonObject s : objects) {
      array.add(s);
    }

    return new Gson().toJson(array).getBytes();
  }
}
