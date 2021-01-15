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

package org.opengroup.osdu.legal.tags.dataaccess;

import static java.util.Objects.isNull;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.legal.config.MongoDBConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class MongoClientProvider {

  private static final Logger LOG = LoggerFactory.getLogger(MongoClientProvider.class);
  private static final String MONGO_PREFIX = "mongodb://";
  private static final String MONGO_OPTIONS = "retryWrites=true&w=majority&maxIdleTimeMS=10000";
  private final MongoDBConfigProperties mongoDBConfigProperties;

  private MongoClient mongoClient = null;

  @Autowired
  public MongoClientProvider(MongoDBConfigProperties mongoDBConfigProperties) {
    this.mongoDBConfigProperties = mongoDBConfigProperties;
  }

  private MongoClient initMongoClient() throws RuntimeException {
    final String connectionString = String.format("%s%s:%s@%s/?%s",
        MONGO_PREFIX,
        mongoDBConfigProperties.getMongoDbUser(),
        mongoDBConfigProperties.getMongoDbPassword(),
        mongoDBConfigProperties.getMongoDbUrl(),
        MONGO_OPTIONS);
    ConnectionString connString = new ConnectionString(connectionString);
    MongoClientSettings settings = MongoClientSettings.builder()
        .applyConnectionString(connString)
        .retryWrites(true)
        .build();
    try {
      mongoClient = MongoClients.create(settings);
    } catch (Exception ex) {
      LOG.error("Error connecting MongoDB", ex);
      throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Error connecting MongoDB",
          ex.getMessage(), ex);
    }
    return mongoClient;
  }

  public MongoClient getMongoClient() {
    return isNull(mongoClient) ? initMongoClient() : mongoClient;
  }

  public MongoOperations getOps(String dbName) {
    return new MongoTemplate(getMongoClient(), dbName);
  }

}
