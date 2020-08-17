//  Copyright © Microsoft Corporation
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.opengroup.osdu.legal.azure.tags.dataaccess;

import com.azure.cosmos.FeedOptions;
import com.azure.cosmos.SqlParameter;
import com.azure.cosmos.SqlParameterList;
import com.azure.cosmos.SqlQuerySpec;
import org.opengroup.osdu.azure.CosmosStore;
import org.opengroup.osdu.common.Validators;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Repository
public class LegalTagRepositoryImpl implements ILegalTagRepository {

    private ReentrantLock mutex = new ReentrantLock();

    @Autowired
    private CosmosStore cosmosStore;

    @Autowired
    private String legalTagsContainer;

    @Autowired
    private String cosmosDBName;

    @Autowired
    private DpsHeaders headers;

    @Override
    public Long create(LegalTag legalTag) {
        Validators.checkNotNull(legalTag, "LegalTag");
        Long id = legalTag.getId();
        String strId = String.valueOf(id);

        LegalTagDoc legalTagDoc = new LegalTagDoc(strId, legalTag);
        try {
            mutex.lock();
            Optional<LegalTagDoc> existingDoc = cosmosStore.findItem(headers.getPartitionId(), cosmosDBName, legalTagsContainer, strId, strId, LegalTagDoc.class);
            if (existingDoc.isPresent()) {
                throw AppException.legalTagAlreadyExistsError(legalTag.getName());
            }
            cosmosStore.upsertItem(headers.getPartitionId(), cosmosDBName, legalTagsContainer, legalTagDoc);
        } finally {
            mutex.unlock();
        }
        return id;
    }

    @Override
    public Collection<LegalTag> get(long[] ids) {
        List<LegalTag> output = new ArrayList<>();

        if(ids != null) {
            for(long id : ids)
            {
                String strId = String.valueOf(id);
                cosmosStore.findItem(headers.getPartitionId(), cosmosDBName, legalTagsContainer, strId, strId, LegalTagDoc.class)
                        .ifPresent(tagDoc -> output.add(tagDoc.getLegalTag()));
            }
        }
        return output;
    }

    @Override
    public Boolean delete(LegalTag legalTag) {
        Long id = legalTag.getId();
        String strId = String.valueOf(id);
        boolean exists = cosmosStore.findItem(headers.getPartitionId(), cosmosDBName, legalTagsContainer, strId, strId, LegalTagDoc.class).isPresent();
        if (!exists)
            return false;

        cosmosStore.deleteItem(headers.getPartitionId(), cosmosDBName, legalTagsContainer, strId, strId);
        return true;
    }

    @Override
    public LegalTag update(LegalTag newLegalTag) {
        if(newLegalTag == null)
            return null;

        Long id = newLegalTag.getId();
        String strId = String.valueOf(id);
        boolean exists = cosmosStore.findItem(headers.getPartitionId(), cosmosDBName, legalTagsContainer, strId, strId, LegalTagDoc.class).isPresent();
        if (!exists)
            throw AppException.legalTagDoesNotExistError(newLegalTag.getName());

        cosmosStore.upsertItem(headers.getPartitionId(), cosmosDBName, legalTagsContainer, new LegalTagDoc(strId, newLegalTag));

        return newLegalTag;
    }

    @Override
    public Collection<LegalTag> list(ListLegalTagArgs args) {
        SqlQuerySpec query = new SqlQuerySpec()
                .setQueryText("SELECT * FROM c WHERE c.legalTag.isValid = @isValid")
                .setParameters(new SqlParameterList(new SqlParameter("@isValid", args.getIsValid())));

        FeedOptions options = new FeedOptions().setEnableCrossPartitionQuery(true);
        return cosmosStore.queryItems(headers.getPartitionId(), cosmosDBName, legalTagsContainer, query, options, LegalTagDoc.class)
                .stream()
                .map(LegalTagDoc::getLegalTag)
                .collect(Collectors.toList());
    }
}
