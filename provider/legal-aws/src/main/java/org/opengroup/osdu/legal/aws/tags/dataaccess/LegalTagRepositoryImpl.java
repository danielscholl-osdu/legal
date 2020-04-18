// Copyright © Amazon Web Services
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

package org.opengroup.osdu.legal.aws.tags.dataaccess;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelper;
import org.opengroup.osdu.core.aws.dynamodb.QueryPageResult;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.core.common.model.legal.LegalTag;

import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Repository // why use repository over component over service
public class LegalTagRepositoryImpl implements ILegalTagRepository {

    @Value("${aws.dynamodb.table.prefix}")
    String tablePrefix;

    @Value("${aws.dynamodb.region}")
    String dynamoDbRegion;

    @Value("${aws.dynamodb.endpoint}")
    String dynamoDbEndpoint;

    private DynamoDBQueryHelper queryHelper;

    @PostConstruct
    public void init(){
        queryHelper = new DynamoDBQueryHelper(dynamoDbEndpoint, dynamoDbRegion, tablePrefix);
    }

    @Override
    public Long create(LegalTag legalTag) {
        LegalDoc legalDoc = CreateLegalDocFromTag(legalTag);
        if (queryHelper.keyExistsInTable(LegalDoc.class, legalDoc)){
            throw new AppException(409, "Legal tag conflict", String.format(
                    "A LegalTag already exists for the given name %s. Can't create again. Id is %s",
                    legalTag.getName(), legalTag.getId()));
        }
        return save(legalDoc);
    }

    @Override
    public Collection<LegalTag> get(long[] ids) {
        List<LegalTag> tags = new ArrayList<>();

        for(long id: ids) {
            LegalDoc ld = queryHelper.loadByPrimaryKey(LegalDoc.class, String.valueOf(id)); //dynamoDBLegal.findById(String.valueOf(id));
            if(ld != null) {
                tags.add(CreateLegalTagFromDoc(ld));
            }
        }

        return tags;
    }

    @Override
    public Boolean delete(LegalTag legalTag) {
        Boolean result = true;
        try {
            queryHelper.deleteByPrimaryKey(LegalDoc.class, String.valueOf(legalTag.getId()));
        } catch (Exception e){ // should be dynamodb specific exception
            result = false;
            // might need to throw app exception
        }
        return result;
    }

    @Override
    public LegalTag update(LegalTag newLegalTag) {
        // make sure integration test to try and update something that doesn't exist
        save(CreateLegalDocFromTag(newLegalTag));
        return newLegalTag;
    }

    @Override
    public Collection<LegalTag> list(ListLegalTagArgs args) {
        int limit = args.getLimit();

        List<LegalDoc> docs = null;
        List<LegalTag> tags = new ArrayList<>();
        try {
            if(limit <= 0){
                docs = queryHelper.scanTable(LegalDoc.class);
            } else {
                QueryPageResult<LegalDoc> scanPageResults = queryHelper.scanPage(LegalDoc.class, limit, args.getCursor());
                if (scanPageResults != null) docs = scanPageResults.results;
            }
        } catch (UnsupportedEncodingException e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Error parsing results",
                    e.getMessage());
        }

        if(docs != null) {
            docs.forEach(legalDoc -> {
                if (legalDoc.getIsValid() == args.getIsValid()) tags.add(CreateLegalTagFromDoc(legalDoc));
            });
        }

        return tags;
    }

    private Long save(LegalDoc legalDoc){
        Long id = -1L;
        if(legalDoc != null){
            queryHelper.save(legalDoc);
            id = Long.parseLong(legalDoc.getId());
        }
        return id;
    }

    private LegalTag CreateLegalTagFromDoc(LegalDoc ld){
        LegalTag tag = new LegalTag();
        tag.setId(Long.parseLong(ld.getId()));
        tag.setName(ld.getName());
        tag.setIsValid(ld.getIsValid());
        tag.setDescription(ld.getDescription());
        tag.setProperties(ld.getProperties());
        return tag;
    }

    private LegalDoc CreateLegalDocFromTag(LegalTag legalTag){
        LegalDoc legalDoc = new LegalDoc();
        legalDoc.setId(String.valueOf(legalTag.getId()));
        legalDoc.setDescription(legalTag.getDescription());
        legalDoc.setName(legalTag.getName());
        legalDoc.setProperties(legalTag.getProperties());
        legalDoc.setIsValid(legalTag.getIsValid());
        return legalDoc;
    }
}
