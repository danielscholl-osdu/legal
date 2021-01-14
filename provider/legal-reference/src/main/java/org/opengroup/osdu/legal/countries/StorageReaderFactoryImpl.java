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

import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.config.MongoDBConfigProperties;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReaderFactory;
import org.opengroup.osdu.legal.tags.dataaccess.MongoClientProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StorageReaderFactoryImpl implements IStorageReaderFactory {

  private static final String LEGAL_COLLECTION_NAME = "countries";

  private final MongoDBConfigProperties mongoDBConfigProperties;
  private final MongoClientProvider clientProvider;

  @Autowired
  public StorageReaderFactoryImpl(MongoDBConfigProperties mongoDBConfigProperties, MongoClientProvider clientProvider) {
    this.mongoDBConfigProperties = mongoDBConfigProperties;
    this.clientProvider = clientProvider;
  }

  @Override
  public IStorageReader getReader(TenantInfo tenant, String projectRegion) {
    return new StorageReaderImpl(tenant, projectRegion, mongoDBConfigProperties.getMongoDbName(),
        LEGAL_COLLECTION_NAME, clientProvider);
  }
}
