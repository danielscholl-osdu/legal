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

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelperFactory;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelperV2;
import org.opengroup.osdu.core.aws.dynamodb.QueryPageResult;
import org.opengroup.osdu.core.aws.exceptions.InvalidCursorException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.model.legal.LegalTag;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import java.io.UnsupportedEncodingException;
import java.util.*;




class LegalTagRepositoryImplTest {

    @InjectMocks
    private LegalTagRepositoryImpl repo;

    @Mock
    private DynamoDBQueryHelperV2 queryHelper;

    @Mock
    private DpsHeaders headers;

    @Mock
    private JaxRsDpsLog log;

    @Mock
    private DynamoDBQueryHelperFactory dynamoDBQueryHelperFactory;

    @Mock
    private ListLegalTagArgs args;

    private final String testPartition = "test-partition";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(dynamoDBQueryHelperFactory.getQueryHelperForPartition(eq(testPartition), any()))
                .thenReturn(queryHelper);
        when(headers.getPartitionId())
                .thenReturn(testPartition);
        args = new ListLegalTagArgs();
        args.setIsValid(true);
        args.setCursor("{{\"title\"={\"S\":\"Monster on the Campus\",}},{\"title\":{\"S\":\"+1\",}},}}}");
    }

    @Test
    void testSetTenantInfo() {
        // arrange
        TenantInfo tenantInfo = new TenantInfo();
        // act
        repo.setTenantInfo(tenantInfo);

        // assert
        Assert.assertEquals(tenantInfo, repo.getTenantInfo());
    }

    @Test
    void testGetTenantInfo() {
        // arrange
        TenantInfo tenantInfo = new TenantInfo();
        repo.setTenantInfo(tenantInfo);

        // act
        TenantInfo result = repo.getTenantInfo();

        // assert
        Assert.assertEquals(tenantInfo, result);
    }

    @Test
    void testCreate_Success(){
        LegalTag tag = new LegalTag();
        tag.setId(-100L);

        when(queryHelper.keyExistsInTable(eq(LegalDoc.class), any())).thenReturn(false);
        doNothing().when(queryHelper).save(any());

        repo.create(tag);

        verify(queryHelper, times(1)).save(any());
    }

    @Test
    void testCreate_Failure_TagAlreadyExists() {
        LegalTag tag = new LegalTag();
        tag.setId(-100L);
        when(queryHelper.keyExistsInTable(eq(LegalDoc.class), any())).thenReturn(true);

        assertThrows(AppException.class, () -> {
            repo.create(tag);
        });
    }

    @Test
    void testGet(){
        // arrange
        long[] ids = new long[]{
            1L, 2L, 3L
        };

        // act
        repo.get(ids);

        // assert
        verify(queryHelper, times(1)).loadByPrimaryKey(any(), eq("1"), any());
        verify(queryHelper, times(1)).loadByPrimaryKey(any(), eq("2"), any());
        verify(queryHelper, times(1)).loadByPrimaryKey(any(), eq("3"), any());
    }

    @Test
    void testGetldNull() {
        long[] ids = new long[]{
            1L, 2L, 3L
        };
        when(queryHelper.loadByPrimaryKey(eq(LegalDoc.class), anyString(), any())).thenReturn(null);
        Collection<LegalTag> tags = repo.get(ids);
        // assert
        Assert.assertEquals(0, tags.size());
    }

    @Test
    void testDelete(){
        // arrange
        LegalTag tag = new LegalTag();
        tag.setId(-100L);

        repo.delete(tag);

        verify(queryHelper, times(1)).deleteByPrimaryKey(any(), eq("-100"), any());
    }

    @Test
    void testDeleteThrowsException() {
        // arrange
        LegalTag tag = new LegalTag();
        tag.setId(-100L);

        doThrow(new RuntimeException("Simulated exception")).when(queryHelper).deleteByPrimaryKey(any(), anyString(), any());

        assertSame(false, repo.delete(tag));
    }

    @Test
    void testUpdate(){
        LegalTag tag = new LegalTag();
        tag.setId(-100L);
        doNothing().when(queryHelper).save(any(LegalDoc.class));

        repo.update(tag);

        verify(queryHelper, times(1)).save(any(LegalDoc.class));
    }

    @Test
    void testList() throws InvalidCursorException, UnsupportedEncodingException {
        args.setLimit(100);
        LegalDoc ld = new LegalDoc();
        ld.setId(String.valueOf(-100L));
        ld.setName("test-name");
        ld.setDescription("test-desc");
        ld.setIsValid(true);
        List<LegalDoc> listLd = new ArrayList<>();
        listLd.add(ld);
        QueryPageResult<LegalDoc> page = new QueryPageResult<LegalDoc>(null, listLd);


        when(queryHelper.scanPage(eq(LegalDoc.class), eq(100),any(), any(), any()))
                    .thenReturn(page);


        // act
        ArrayList<LegalTag> tags = (ArrayList)repo.list(args);
        Long tagId = tags.get(0).getId();

        // assert
        Assert.assertEquals((long)tagId, -100L);
    }

    @Test
    void testListThrowsException() throws UnsupportedEncodingException {
        args.setLimit(0);

        when(queryHelper.scanTable(eq(LegalDoc.class), anyString(), any())).thenAnswer(invocation -> {
            throw new UnsupportedEncodingException("Simulated exception");
        });

        assertThrows(AppException.class, () -> {
            repo.list(args);
        });
    }


    @Test
    void testListReturnsNull(){
        // arrange
        
        args.setLimit(100);

        // act
        ArrayList<LegalTag> tags = (ArrayList)repo.list(args);

        // assert
        Assert.assertEquals(0, tags.size());
    }
}
