/**
* Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*      http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.opengroup.osdu.legal.aws.api.mongo.util;

import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Collection;
import java.util.List;

import static org.opengroup.osdu.legal.aws.tags.dataaccess.mongodb.repository.LegalTagRepositoryMongoDBImpl.COLLECTION_PREFIX;


public final class MongoTemplateHelper {
    private final MongoTemplate mongoTemplate;

    public MongoTemplateHelper(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    public LegalTag insert(LegalTag legalTag) {
        return this.insert(legalTag, ParentUtil.DATA_PARTITION);
    }

    public void insert(Collection<? extends LegalTag> legalTags) {
        this.insert(legalTags, ParentUtil.DATA_PARTITION);
    }

    public void insert(Collection<? extends LegalTag> legalTags, String collection) {
        legalTags.forEach(t -> this.insert(t, collection));
    }

    public LegalTag insert(LegalTag legalTags, String dataPartition) {
        return this.mongoTemplate.insert(legalTags, getLegalCollectionName(dataPartition));

    }

    public LegalTag findById(Long id, String dataPartition) {
        return this.mongoTemplate.findById(id, LegalTag.class, getLegalCollectionName(dataPartition));
    }

    public List<LegalTag> findAll(String dataPartition) {
        return this.mongoTemplate.findAll(LegalTag.class, getLegalCollectionName(dataPartition));
    }


    public void dropCollections() {
        this.mongoTemplate.getCollectionNames().forEach(this.mongoTemplate::dropCollection);
    }

    private String getLegalCollectionName(String dataPartitionId) {
        return COLLECTION_PREFIX + dataPartitionId;
    }

}
