/*
 * Copyright 2020 Google LLC
 * Copyright 2020 EPAM Systems, Inc
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

package org.opengroup.osdu.legal.tags;

import com.google.cloud.datastore.Datastore;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.gcp.multitenancy.IDatastoreFactory;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepositoryFactory;
import org.opengroup.osdu.legal.tags.dataaccess.DatastoreLegalTagRepository;
import org.opengroup.osdu.legal.tags.dataaccess.ResilientLegalTagRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class LegalTagRepositoryFactoryGcpImpl implements ILegalTagRepositoryFactory {

  private final Map<String, ILegalTagRepository> tenantRepositories = new HashMap<>();

  private TenantInfo tenantInfo;
  private IDatastoreFactory factory;
  private ITenantFactory tenantFactory;

  public LegalTagRepositoryFactoryGcpImpl(TenantInfo tenantInfo, IDatastoreFactory factory,
      ITenantFactory tenantFactory) {
    this.tenantInfo = tenantInfo;
    this.factory = factory;
    this.tenantFactory = tenantFactory;
  }

  @Override
  public ILegalTagRepository get(String tenantName) {
    if (StringUtils.isBlank(tenantName)) {
      throw invalidTenantGivenException(tenantName);
    }
    if (!tenantRepositories.containsKey(tenantName)) {
      addRepository(tenantName);
    }
    return tenantRepositories.get(tenantName);
  }

  private void addRepository(String tenantName) {
    TenantInfo info = tenantFactory.getTenantInfo(tenantName);
    Datastore ds = factory.getDatastore(info);
    if (Objects.isNull(ds)) {
      throw invalidTenantGivenException(tenantName);
    }
    ILegalTagRepository repo = new ResilientLegalTagRepository(new DatastoreLegalTagRepository(ds));
    tenantRepositories.put(tenantName, repo);
  }

  AppException invalidTenantGivenException(String tenantName) {
    return new AppException(403, "Forbidden",
        String.format("You do not have access to the %s value given %s",
            DpsHeaders.ACCOUNT_ID, tenantName));
  }
}
