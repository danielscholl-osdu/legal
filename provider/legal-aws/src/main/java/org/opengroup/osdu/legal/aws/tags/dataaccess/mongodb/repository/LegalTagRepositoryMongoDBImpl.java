// Copyright MongoDB, Inc or its affiliates. All Rights Reserved.
// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.legal.aws.tags.dataaccess.mongodb.repository;

import org.opengroup.osdu.core.aws.mongodb.MongoDBMultiClusterFactory;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The type Legal tag repository MongoDb.
 */
@ConditionalOnProperty(prefix = "repository", name = "implementation", havingValue = "mongodb")
@Component
public class LegalTagRepositoryMongoDBImpl implements ILegalTagRepository {

    //field names
    private static final String ID = "_id";
    public static final String IS_VALID = "isValid";

    private static final String ERR_MSG = "Legal tag conflict";

    //collection prefix
    public static final String COLLECTION_PREFIX = "Legal-";

    @Inject
    private DpsHeaders headers;

    @Inject
    private JaxRsDpsLog log;

    @Inject
    private MongoDBMultiClusterFactory mongoDBMultiClusterFactory;

    @Inject
    private IndexUpdater indexUpdater;

    private String getDataPartitionId() {
        this.log.warning("TenantInfo found to be null, defaulting to partition id from headers");
        return this.headers.getPartitionId();
    }

    /**
     * Create long.
     *
     * @param legalTag the legal tag
     * @return the long
     */
    @Override
    public Long create(LegalTag legalTag) {
        if (legalTag.getId().equals(-1L)) {
            throw new AppException(409, ERR_MSG, String.format("Cannot create a LegalTag id for the given name %s. Id is %s", legalTag.getName(), legalTag.getId()));
        }
        try {
            this.mongoDBMultiClusterFactory.getHelper(getDataPartitionId()).insert(legalTag, getLegalCollectionName(this.getDataPartitionId()));
        } catch (DuplicateKeyException e) {
            throw new AppException(409, ERR_MSG, String.format("A LegalTag already exists for the given name %s. Cannot duplicate LegalTag. Id is %s", legalTag.getName(), legalTag.getId()));
        }
        return legalTag.getId();
    }

    /**
     * Get collection.
     *
     * @param ids the ids
     * @return the collection
     */
    @Override
    public Collection<LegalTag> get(long[] ids) {
        List<Long> longList = Arrays.stream(ids).boxed().collect(Collectors.toList());
        return this.mongoDBMultiClusterFactory.getHelper(getDataPartitionId()).getList(ID, longList, LegalTag.class, getLegalCollectionName(this.getDataPartitionId())).stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Delete boolean.
     *
     * @param legalTag the legal tag
     * @return the boolean
     */
    @Override
    public Boolean delete(LegalTag legalTag) {
        try {
            return this.mongoDBMultiClusterFactory.getHelper(getDataPartitionId()).delete(ID, legalTag.getId(), getLegalCollectionName(this.getDataPartitionId()));
        } catch (Exception exception) {
            this.log.error("Error while deleting legal tag", exception);
            throw new AppException(500, String.format("Error deleting legal tag, id = %d", legalTag.getId()), exception.getMessage());
        }
    }

    /**
     * Update legal tag.
     *
     * @param newLegalTag the new legal tag
     * @return the legal tag
     */
    @Override
    public LegalTag update(LegalTag newLegalTag) {
        Query query = Query.query(Criteria.where(ID).is(newLegalTag.getId()));
        LegalTag replaced = this.mongoDBMultiClusterFactory.getHelper(getDataPartitionId()).findAndReplace(query, newLegalTag, getLegalCollectionName(this.getDataPartitionId()));
        if (replaced == null) {
            throw new AppException(409, ERR_MSG, String.format("A LegalTag does not exist for the given name %s. Cannot update LegalTag. Id is %s", newLegalTag.getName(), newLegalTag.getId()));
        }
        return newLegalTag;
    }


    /**
     * List collection.
     *
     * @param args the args
     * @return the collection
     */
    @Override
    public Collection<LegalTag> list(ListLegalTagArgs args) {
        Query query = Query.query(Criteria.where(IS_VALID).is(args.getIsValid()));
        return this.mongoDBMultiClusterFactory.getHelper(getDataPartitionId()).find(query, LegalTag.class, getLegalCollectionName(this.getDataPartitionId()));
    }

    private String getLegalCollectionName(String dataPartitionId) {
        indexUpdater.checkIndex(dataPartitionId);
        return COLLECTION_PREFIX + dataPartitionId;
    }
}   
