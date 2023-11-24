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

package org.opengroup.osdu.legal.aws.tags.dataaccess.mongodb.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.core.aws.mongodb.MongoDBMultiClusterFactory;
import org.opengroup.osdu.core.aws.mongodb.helper.BasicMongoDBHelper;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.legal.aws.api.mongo.config.LegalTestConfig;
import org.opengroup.osdu.legal.aws.api.mongo.util.ParentUtil;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.mockito.MockitoAnnotations;
import static org.opengroup.osdu.legal.aws.api.mongo.util.LegalTagGenerator.generateLegalTags;


@DataMongoTest
@SpringJUnitConfig(classes = {LegalTestConfig.class})
class LegalTagRepositoryMongoDBImplTest extends ParentUtil {

    @InjectMocks
    private LegalTagRepositoryMongoDBImpl legalTagRepository;

    @Mock
    private MongoDBMultiClusterFactory mongoDBMultiClusterFactory;

    @Mock
    private BasicMongoDBHelper mongoTemplateHelper;

    @Mock 
    private IndexUpdater indexUpdater;

    @BeforeEach
    public void setUp() {
       MockitoAnnotations.openMocks(this);
       when(mongoDBMultiClusterFactory.getHelper(any())).thenReturn(mongoTemplateHelper);
    }


    @Test
    void testCreateWithValidTag() {
        LegalTag legalTag = new LegalTag();
        legalTag.setId(1L); // Set whatever ID you want that is not -1
        legalTag.setName("validName");

        doNothing().when(indexUpdater).checkIndex(anyString()); 
        Long result = legalTagRepository.create(legalTag);

        assertEquals(legalTag.getId(), result);
    }

    @Test
    void testCreateWithIdNegativeOne() {
        LegalTag legalTag = new LegalTag();
        legalTag.setId(-1L);
        legalTag.setName("invalidName");

        assertThrows(AppException.class, () -> legalTagRepository.create(legalTag));

    }
    
    @Test
    void testCreateWithDuplicateKey() {
        LegalTag legalTag = new LegalTag();
        legalTag.setId(1L); // Set whatever ID you want that is not -1
        legalTag.setName("duplicateName");

        doThrow(new DuplicateKeyException("")).when(mongoTemplateHelper).insert(eq(legalTag), anyString());

        assertThrows(AppException.class, () -> legalTagRepository.create(legalTag));

    }

    @Test
    void testGetWithValidIds() {
        long[] ids = new long[]{1L, 2L, 3L};
        List<Long> longList = Arrays.stream(ids).boxed().collect(Collectors.toList());

        List<String> legalTagNames = Arrays.asList("legalTag1", "LegalTag2", "legalTag3");
        List<LegalTag> legalTags = generateLegalTags(legalTagNames);

        
        when(mongoTemplateHelper.getList(anyString(), eq(longList), eq(LegalTag.class), anyString())).thenReturn(legalTags);

        doNothing().when(indexUpdater).checkIndex(anyString()); // Mocking the indexUpdater behavior

        Collection<LegalTag> result = legalTagRepository.get(ids);

        assertEquals(legalTags, result);
    }

    @Test
    void testDeleteWithValidTag() {
        LegalTag legalTag = new LegalTag();
        legalTag.setId(1L); 
        legalTag.setName("validName");

        doNothing().when(indexUpdater).checkIndex(anyString()); // Mocking the indexUpdater behavior
        when(mongoTemplateHelper.delete(anyString(), eq(legalTag.getId()), anyString())).thenReturn(true);

        Boolean result = legalTagRepository.delete(legalTag);

        assertTrue(result);
    }

    @Test
    void testDeleteWithoutValidTag() throws Exception {
        LegalTag legalTag = new LegalTag();
        legalTag.setId(-1L); 
        legalTag.setName("invalid");

        when(mongoTemplateHelper.delete(anyString(), eq(legalTag.getId()), anyString())).thenThrow(new RuntimeException());
        assertThrows(AppException.class, () -> legalTagRepository.delete(legalTag));
    }

    @Test 
    void testUpdateWithValidTag() {
        LegalTag newLegalTag = new LegalTag();
        newLegalTag.setId(2L); 
        newLegalTag.setName("newTag");  
        LegalTag oldLegalTag = new LegalTag();
        oldLegalTag.setId(1L); 
        oldLegalTag.setName("oldTag");  

        Query query = Query.query(Criteria.where("_id").is(newLegalTag.getId()));
        when(mongoTemplateHelper.findAndReplace(eq(query), eq(newLegalTag), anyString())).thenReturn(oldLegalTag);

        LegalTag updatedTag = legalTagRepository.update(newLegalTag);

        assertEquals(newLegalTag, updatedTag);
    }

    @Test
    void testUpdateWithInvalidTag() {
        LegalTag newLegalTag = new LegalTag();
        when(mongoTemplateHelper.findAndReplace(any(), any(), anyString())).thenReturn(null);
        assertThrows(AppException.class, () -> legalTagRepository.update(newLegalTag));
    }

    @Test
    void testList() {
        ListLegalTagArgs args = new ListLegalTagArgs();
        args.setIsValid(true);
        Query query = Query.query(Criteria.where("isValid").is(args.getIsValid()));
        when(mongoTemplateHelper.find(eq(query), eq(LegalTag.class), anyString())).thenReturn(generateLegalTags(Arrays.asList("legalTag1", "LegalTag2", "legalTag3")));
        // assert 
        assertEquals(3, legalTagRepository.list(args).size());
    }

}