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

package org.opengroup.osdu.legal.aws.tags.dataaccess;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelperFactory;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelperV2;
import org.opengroup.osdu.core.aws.dynamodb.QueryPageResult;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.annotation.RequestScope;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import jakarta.inject.Inject;

@ConditionalOnProperty(prefix = "repository", name = "implementation",havingValue = "dynamodb",
        matchIfMissing = true)
@Repository // why use repository over component over service
@RequestScope
public class LegalTagRepositoryImpl implements ILegalTagRepository {

    @Inject
    private DpsHeaders headers;

    @Inject
    private JaxRsDpsLog log;

    private TenantInfo tenantInfo;

    @Inject
    private DynamoDBQueryHelperFactory dynamoDBQueryHelperFactory;

    @Value("${aws.dynamodb.legalTable.ssm.relativePath}")
    String legalRepositoryTableParameterRelativePath;

    public void setTenantInfo(TenantInfo tenantInfo) {
        this.tenantInfo = tenantInfo;
    }

    TenantInfo getTenantInfo(){
        return this.tenantInfo;
    }

    private String getDataPartitionId(){
        if(this.tenantInfo == null){
            log.warning("TenantInfo found to be null, defaulting to partition in headers");
            return headers.getPartitionId();
        }
        return tenantInfo.getDataPartitionId();
    }

    private DynamoDBQueryHelperV2 getLegalRepositoryQueryHelper() {
        String dataPartitionId = getDataPartitionId();
        return dynamoDBQueryHelperFactory.getQueryHelperForPartition(dataPartitionId, legalRepositoryTableParameterRelativePath);
    }


    @Override
    public Long create(LegalTag legalTag) {
        DynamoDBQueryHelperV2 queryHelper = getLegalRepositoryQueryHelper();

        LegalDoc legalDoc = createLegalDocFromTag(legalTag);
        if (queryHelper.keyExistsInTable(LegalDoc.class, legalDoc)){
            throw new AppException(409, "Legal tag conflict", String.format(
                    "A LegalTag already exists for the given name %s. Can't create again. Id is %s",
                    legalTag.getName(), legalTag.getId()));
        }
        return save(legalDoc);
    }

    @Override
    public Collection<LegalTag> get(long[] ids) {
        DynamoDBQueryHelperV2 queryHelper = getLegalRepositoryQueryHelper();

        List<LegalTag> tags = new ArrayList<>();

        for(long id: ids) {
            LegalDoc ld = queryHelper.loadByPrimaryKey(LegalDoc.class, String.valueOf(id), getDataPartitionId()); 
            if(ld != null) {
                tags.add(createLegalTagFromDoc(ld));
            }
        }

        return tags;
    }

    @Override
    public Boolean delete(LegalTag legalTag) {
        DynamoDBQueryHelperV2 queryHelper = getLegalRepositoryQueryHelper();

        Boolean result = true;
        try {
            queryHelper.deleteByPrimaryKey(LegalDoc.class, String.valueOf(legalTag.getId()), getDataPartitionId());
        } catch (Exception e){ // should be dynamodb specific exception
            result = false;
            // might need to throw app exception
        }
        return result;
    }

    @Override
    public LegalTag update(LegalTag newLegalTag) {
        // make sure integration test to try and update something that doesn't exist
        save(createLegalDocFromTag(newLegalTag));
        return newLegalTag;
    }

    @Override
    public Collection<LegalTag> list(ListLegalTagArgs args) {
        DynamoDBQueryHelperV2 queryHelper = getLegalRepositoryQueryHelper();

        String filterExpression = "dataPartitionId = :partitionId";

        AttributeValue dataPartitionAttributeValue = new AttributeValue(getDataPartitionId());

        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":partitionId", dataPartitionAttributeValue);

        int limit = args.getLimit();

        List<LegalDoc> docs = null;
        List<LegalTag> tags = new ArrayList<>();
        try {
            if(limit <= 0){
                docs = queryHelper.scanTable(LegalDoc.class, filterExpression, eav);
            } else {
                QueryPageResult<LegalDoc> scanPageResults = queryHelper.scanPage(LegalDoc.class, limit, args.getCursor(),filterExpression, eav);
                if (scanPageResults != null) docs = scanPageResults.results;
            }
        } catch (UnsupportedEncodingException e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Error parsing results",
                    e.getMessage());
        }

        if (docs != null) {
            docs.forEach(legalDoc -> {
                if (Objects.equals(legalDoc.getIsValid(), args.getIsValid())) {
                    tags.add(createLegalTagFromDoc(legalDoc));
                }
            });
        }
        
        
        

        return tags;
    }

    private Long save(LegalDoc legalDoc){
        DynamoDBQueryHelperV2 queryHelper = getLegalRepositoryQueryHelper();

        Long id = -1L;
        if(legalDoc != null){
            queryHelper.save(legalDoc);
            id = Long.parseLong(legalDoc.getId());
        }
        return id;
    }

    private LegalTag createLegalTagFromDoc(LegalDoc ld){
        LegalTag tag = new LegalTag();
        tag.setId(Long.parseLong(ld.getId()));
        tag.setName(ld.getName());
        tag.setIsValid(ld.getIsValid());
        tag.setDescription(ld.getDescription());
        tag.setProperties(ld.getProperties());
        return tag;
    }

    private LegalDoc createLegalDocFromTag(LegalTag legalTag){
        LegalDoc legalDoc = new LegalDoc();
        legalDoc.setId(String.valueOf(legalTag.getId()));
        legalDoc.setDataPartitionId(getDataPartitionId());
        legalDoc.setDescription(legalTag.getDescription());
        legalDoc.setName(legalTag.getName());
        legalDoc.setProperties(legalTag.getProperties());
        legalDoc.setIsValid(legalTag.getIsValid());
        return legalDoc;
    }
}
