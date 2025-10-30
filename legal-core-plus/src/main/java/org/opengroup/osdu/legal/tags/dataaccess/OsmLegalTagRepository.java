/*
 *  Copyright 2020-2023 Google LLC
 *  Copyright 2020-2023 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.opengroup.osdu.legal.tags.dataaccess;


import static org.opengroup.osdu.core.osm.core.model.where.predicate.Eq.eq;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.osm.core.model.Destination;
import org.opengroup.osdu.core.osm.core.model.Kind;
import org.opengroup.osdu.core.osm.core.model.Namespace;
import org.opengroup.osdu.core.osm.core.model.query.GetQuery;
import org.opengroup.osdu.core.osm.core.service.Context;
import org.opengroup.osdu.core.osm.core.service.Transaction;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

@Repository
@Scope(SCOPE_SINGLETON)
@Log
@RequiredArgsConstructor
public class OsmLegalTagRepository implements ILegalTagRepository {

  private final Context context;
  private final TenantInfo tenantInfo;

  public static final Kind LEGAL_TAGS_ENTITY_NAME = new Kind("LegalTagOsm");
  private ReentrantLock lock = new ReentrantLock();

  private Destination getDestination() {
    return Destination.builder().partitionId(tenantInfo.getDataPartitionId())
        .namespace(new Namespace(tenantInfo.getName())).kind(LEGAL_TAGS_ENTITY_NAME).build();
  }

  @Override
  public Long create(LegalTag legalTag) {
    Preconditions.checkNotNull(legalTag, "Legal tag is null!");
    Preconditions.checkNotNull(legalTag.getId(), "Legal tag's id is null!");

    Long id = -1L;
    if (Objects.nonNull(legalTag)) {

      Transaction txn = null;
      GetQuery<LegalTag> query = new GetQuery<>(LegalTag.class, getDestination(),
          eq("name", legalTag.getName()));
      try {
        txn = context.beginTransaction(getDestination());
        lock.lock();
        if (context.findOne(query).isPresent()) {
          txn.rollbackIfActive();
          throw new AppException(409,
              "A LegalTag already exists for the given name",
              "A LegalTag already exists for the given name");
        } else {
          LegalTag result = context.createAndGet(getDestination(), legalTag);
          txn.commitIfActive();
          return result.getId();
        }
      } finally {
        lock.unlock();
        if (Objects.nonNull(txn)) {
          txn.rollbackIfActive();
        }

      }
    }
    return id;
  }

  @Override
  public Collection<LegalTag> get(long[] ids) {
    List<LegalTag> output = new ArrayList<>();

    if (Objects.nonNull(ids) && ids.length > 0) {
      for (long id : ids) {
        GetQuery<LegalTag> osmQuery = new GetQuery<>(LegalTag.class, getDestination(),
            eq("id", id));
        LegalTag legalTag = context.getOne(osmQuery);
        if (Objects.nonNull(legalTag)) {
          output.add(legalTag);
        }
      }
    }
    return output;
  }

  @Override
  public Boolean delete(LegalTag legalTag) {
    context.deleteById(LegalTag.class, getDestination(), legalTag.getId());
    return true;
  }

  @Override
  public LegalTag update(LegalTag newLegalTag) {
    LegalTag legalTag = null;
    if (Objects.nonNull(newLegalTag)) {
      Transaction txn = context.beginTransaction(getDestination());
      try {
        legalTag = context.upsertAndGet(getDestination(), newLegalTag);
        txn.commitIfActive();
      } finally {
        txn.rollbackIfActive();
      }
    }

    return legalTag;
  }

  @Override
  public Collection<LegalTag> list(ListLegalTagArgs args) {
    GetQuery<LegalTag> osmQuery = new GetQuery<>(LegalTag.class, getDestination(),
        eq(IS_VALID, args.getIsValid()));
    List<LegalTag> results = context.getResultsAsList(osmQuery);
    if (Objects.nonNull(results) && !results.isEmpty()) {
      return results;
    }
    return Collections.emptyList();
  }
}
