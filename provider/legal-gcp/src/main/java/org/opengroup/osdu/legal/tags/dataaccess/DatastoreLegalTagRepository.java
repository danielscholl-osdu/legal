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

package org.opengroup.osdu.legal.tags.dataaccess;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import org.opengroup.osdu.core.common.model.legal.*;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class DatastoreLegalTagRepository implements ILegalTagRepository {
    private final Datastore googleDatastore;

    public DatastoreLegalTagRepository(Datastore ds) {
        googleDatastore = ds;
    }

    @Override
    public Long create(LegalTag legalTag) {
        Long id = -1L;

        try {
            if (legalTag != null) {
                FullEntity<Key> entity = convertLegalTagToEntity(legalTag);
                Key keys = saveNewEntry(entity, legalTag.getName());
                id = keys.getId();
            }
        } catch (DatastoreException ex) {
            throw new PersistenceException(ex.getCode(), ex.getMessage(), ex.getReason());
        }
        return id;
    }

    @Override
    public Collection<LegalTag> get(long[] ids) {
        List<LegalTag> output = new ArrayList<>();

        try {
            if (ids != null && ids.length > 0) {
                List<Key> keys = new ArrayList<>();
                for (long id : ids) {
                    keys.add(createkey(id));
                }
                Iterator<Entity> entities = this.googleDatastore.get(keys);
                while (entities != null && entities.hasNext()) {
                    output.add(convertEntityToLegalTag(entities.next()));
                }
            }
        } catch (DatastoreException ex) {
            throw new PersistenceException(ex.getCode(), ex.getMessage(), ex.getReason());
        }
        return output;
    }

    @Override
    public Boolean delete(LegalTag legalTag) {
        boolean output = false;
        Transaction txn = null;
        try {
            Key key = createkey(legalTag.getId());
            txn = this.googleDatastore.newTransaction();

            //backup legaltag before deleting
            Entity existingEntity = txn.get(key);
            if (existingEntity != null) {
                LegalTag currentLegalTag = convertEntityToLegalTag(existingEntity);
                FullEntity<IncompleteKey> currentEntity = convertLegalTagToHistoricEntity(currentLegalTag);
                txn.putWithDeferredIdAllocation(currentEntity);
                txn.delete(key);
                txn.commit();
            }
            output = true;
        } catch (DatastoreException ex) {
            throw new PersistenceException(ex.getCode(), ex.getMessage(), ex.getReason());
        } finally {
            if (txn != null && txn.isActive()) {
                txn.rollback();
            }
        }
        return output;
    }

    @Override
    public LegalTag update(LegalTag newLegalTag) {
        if (newLegalTag == null)
            return null;

        Transaction txn = null;
        FullEntity<Key> newEntity = convertLegalTagToEntity(newLegalTag);
        try {
            txn = this.googleDatastore.newTransaction();

            Entity existingEntity = txn.get(newEntity.getKey());
            if (existingEntity == null)
                throw AppException.legalTagDoesNotExistError(newLegalTag.getName());

            LegalTag currentLegalTag = convertEntityToLegalTag(existingEntity);
            FullEntity<IncompleteKey> currentEntity = convertLegalTagToHistoricEntity(currentLegalTag);
            txn.putWithDeferredIdAllocation(currentEntity);//this will make a backup of the current entity so we always have a record of it using a datastore assigned id
            txn.put(newEntity);//this will overwrite the current entity
            txn.commit();
        } catch (DatastoreException ex) {
            throw new PersistenceException(ex.getCode(), ex.getMessage(), ex.getReason());
        } finally {
            if (txn != null && txn.isActive()) {
                txn.rollback();
            }
        }
        return newLegalTag;
    }

    @Override
    public Collection<LegalTag> list(ListLegalTagArgs args) {
        List<LegalTag> output = new ArrayList<>();
        try {
            Query<Entity> query = Query.newEntityQueryBuilder()
                    .setKind(LEGAL_TAGS_ENTITYNAME)
                    .setFilter(StructuredQuery.PropertyFilter.eq(IS_VALID, args.getIsValid()))
                    .build();
            QueryResults<Entity> results = this.googleDatastore.run(query);
            while (results != null && results.hasNext()) {
                output.add(convertEntityToLegalTag(results.next()));
            }
        } catch (DatastoreException ex) {
            throw new PersistenceException(ex.getCode(), ex.getMessage(), ex.getReason());
        }
        return output;
    }

    private Key saveNewEntry(FullEntity<Key> entity, String kind) {
        Transaction txn = this.googleDatastore.newTransaction();
        try {
            Entity existingEntity = txn.get(entity.getKey());
            if (existingEntity != null)
                throw AppException.legalTagAlreadyExistsError(kind);

            txn.put(entity);
            txn.commit();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
        return entity.getKey();
    }

    private LegalTag convertEntityToLegalTag(Entity entity) {
        Properties properties = new Properties();
        properties.setOriginator(entity.getString(ORIGINATOR));
        properties.setExpirationDate(new Date(entity.getTimestamp(EXPIRATION_DATE).toSqlTimestamp().getTime()));
        properties.setContractId(entity.getString(CONTRACT_ID));
        properties.setDataType(entity.getString(DATA_TYPE));
        properties.setSecurityClassification(entity.getString(SECURITY_CLASSIFICATION));
        properties.setExportClassification(entity.getString(EXPORT_CLASSIFICATION));
        properties.setPersonalData(entity.getString(PERSONAL_DATA));

        List<Value<String>> legaltagsValues = entity.getList(COUNTRY_OF_ORIGIN);
        List<String> countries = new ArrayList<>();
        legaltagsValues.forEach(tag -> countries.add(tag.get()));
        properties.setCountryOfOrigin(countries);

        LegalTag legalTag = new LegalTag();
        legalTag.setProperties(properties);
        legalTag.setName(entity.getString(NAME));
        legalTag.setIsValid(entity.getBoolean(IS_VALID));
        legalTag.setId(entity.getKey().getId());

        if (entity.contains(DESCRIPTION))
            legalTag.setDescription(entity.getString(DESCRIPTION));

        return legalTag;
    }

    @SuppressWarnings("unchecked")
    private FullEntity<Key> convertLegalTagToEntity(LegalTag legalTag) {
        Key key = createkey(legalTag.getId());
        return getFullEntity(legalTag, key);
    }

    @SuppressWarnings("unchecked")
    private FullEntity<IncompleteKey> convertLegalTagToHistoricEntity(LegalTag legalTag) {
        IncompleteKey key = this.googleDatastore.newKeyFactory()
                .setKind(LEGAL_TAGS_HISTORIC_ENTITYNAME).newKey();
        return getFullEntity(legalTag, key);
    }

    private FullEntity getFullEntity(LegalTag legalTag, IncompleteKey key) {
        Properties properties = legalTag.getProperties();

        List<StringValue> countries = new ArrayList<>();
        properties.getCountryOfOrigin().forEach(tag -> countries.add(new StringValue(tag)));

        FullEntity entity = FullEntity.newBuilder(key)
                .set(NAME, legalTag.getName())
                .set(DESCRIPTION, legalTag.getDescription())
                .set(IS_VALID, legalTag.getIsValid())
                .set(COUNTRY_OF_ORIGIN, countries)
                .set(CONTRACT_ID, properties.getContractId())
                .set(EXPIRATION_DATE, Timestamp.of(properties.getExpirationDate()))
                .set(CREATION_DT, Timestamp.of(new java.sql.Timestamp(System.currentTimeMillis())))
                .set(ORIGINATOR, properties.getOriginator())
                .set(DATA_TYPE, properties.getDataType())
                .set(SECURITY_CLASSIFICATION, properties.getSecurityClassification())
                .set(EXPORT_CLASSIFICATION, properties.getExportClassification())
                .set(PERSONAL_DATA, properties.getPersonalData()).build();
        return entity;
    }

    private Key createkey(long id) {
        return this.googleDatastore.newKeyFactory()
                .setKind(LEGAL_TAGS_ENTITYNAME).newKey(id);
    }
}
