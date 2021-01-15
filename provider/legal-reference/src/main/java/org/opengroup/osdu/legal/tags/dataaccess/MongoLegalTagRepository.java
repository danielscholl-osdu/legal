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

import static com.google.common.primitives.Longs.asList;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.BooleanUtils.negate;
import static org.opengroup.osdu.legal.entity.LegalTagMongoEntity.convertFrom;
import static org.opengroup.osdu.legal.entity.LegalTagMongoEntity.convertTo;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.legal.config.MongoDBConfigProperties;
import org.opengroup.osdu.legal.entity.LegalTagMongoEntity;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class MongoLegalTagRepository implements ILegalTagRepository {

  private final MongoDBConfigProperties mongoDBConfigProperties;
  private final MongoClientProvider clientProvider;

  @Autowired
  public MongoLegalTagRepository(MongoDBConfigProperties mongoDBConfigProperties, MongoClientProvider clientProvider) {
    this.mongoDBConfigProperties = mongoDBConfigProperties;
    this.clientProvider = clientProvider;
  }

  @Override
  public Long create(LegalTag legalTag) {
    Preconditions.checkNotNull(legalTag, "Legal tag is null!");
    Preconditions.checkNotNull(legalTag.getId(), "Legal tag's id is null!");

    MongoOperations ops = clientProvider.getOps(mongoDBConfigProperties.getMongoDbName());

    LegalTagMongoEntity savedEntity = ops.save(convertFrom(legalTag), LEGAL_TAGS_ENTITYNAME);

    return savedEntity.getId();
  }

  @Override
  public Collection<LegalTag> get(long[] ids) {
    Preconditions.checkNotNull(ids, "List of legal tag ids is null!");
    Preconditions.checkArgument(ArrayUtils.isNotEmpty(ids), "List of legal tag ids is empty!");

    MongoOperations ops = clientProvider.getOps(mongoDBConfigProperties.getMongoDbName());

    Query query = new Query(Criteria.where("_id").in(asList(ids)));
    List<LegalTagMongoEntity> entities =
        ops.find(query, LegalTagMongoEntity.class, LEGAL_TAGS_ENTITYNAME);

    return entities.stream()
        .map(LegalTagMongoEntity::convertTo)
        .collect(Collectors.toList());
  }

  @Override
  public Boolean delete(LegalTag legalTag) {
    Preconditions.checkNotNull(legalTag, "Legal tag is null!");

    MongoOperations ops = clientProvider.getOps(mongoDBConfigProperties.getMongoDbName());

    Query query = new Query(Criteria.where("_id").is(legalTag.getId()));

    Boolean exists = ops.exists(query, LegalTagMongoEntity.class, LEGAL_TAGS_ENTITYNAME);

    if (exists) {
      ops.remove(query, LegalTagMongoEntity.class, LEGAL_TAGS_ENTITYNAME);
      exists = ops.exists(query, LegalTagMongoEntity.class, LEGAL_TAGS_ENTITYNAME);
    }

    return negate(exists);
  }

  @Override
  public LegalTag update(LegalTag newLegalTag) {
    Preconditions.checkNotNull(newLegalTag, "Legal tag is null!");

    MongoOperations ops = clientProvider.getOps(mongoDBConfigProperties.getMongoDbName());

    Query query = new Query(Criteria.where("_id").is(newLegalTag.getId()));

    Boolean exists = ops.exists(query, LegalTagMongoEntity.class, LEGAL_TAGS_ENTITYNAME);

    if (negate(exists)) {
      throw AppException.legalTagDoesNotExistError(newLegalTag.getName());
    }

    LegalTagMongoEntity savedEntity = ops.save(convertFrom(newLegalTag), LEGAL_TAGS_ENTITYNAME);

    return convertTo(savedEntity);
  }

  @Override
  public Collection<LegalTag> list(ListLegalTagArgs args) {
    MongoOperations ops = clientProvider.getOps(mongoDBConfigProperties.getMongoDbName());

    Query query = new Query();

    if (negate(isNull(args.getIsValid()))) {
      query.addCriteria(Criteria.where("isValid").is(args.getIsValid()));
    }

    List<LegalTagMongoEntity> entities =
        ops.find(query, LegalTagMongoEntity.class, LEGAL_TAGS_ENTITYNAME);

    return entities.stream()
        .map(LegalTagMongoEntity::convertTo)
        .collect(Collectors.toList());
  }
}
