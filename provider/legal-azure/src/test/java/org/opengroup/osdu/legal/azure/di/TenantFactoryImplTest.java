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

package org.opengroup.osdu.legal.azure.di;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.azure.CosmosStore;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.azure.tags.dataaccess.LegalTagRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TenantFactoryImplTest {

    private static final String dataPartitionId = "data-partition-id";

    private final String[] ids = {"id1", "id2"};
    private final String[] complianceRuleSets = {"compliance-rule-set-1", "compliance-rule-set-2"};

    @Mock
    private CosmosStore cosmosStore;

    @Mock
    private DpsHeaders headers;

    @InjectMocks
    private TenantFactoryImpl sut;

    @Before
    public void init() {
        lenient().doReturn(dataPartitionId).when(headers).getPartitionId();
        List<TenantInfoDoc> tenantInfoDocs = new ArrayList<>();
        assertEquals(ids.length, complianceRuleSets.length);
        for (int i = 0; i < ids.length; i++) {
            TenantInfoDoc tenantInfoDoc = new TenantInfoDoc(ids[i], complianceRuleSets[i]);
            tenantInfoDocs.add(tenantInfoDoc);
        }
        doReturn(tenantInfoDocs).when(cosmosStore).findAllItems(eq(dataPartitionId), any(), any(), any());
    }

    @Test
    public void testExists_whenExistingTenantNameGiven() {
        for (String tenantName: ids) {
            assertTrue(sut.exists(tenantName));
        }
    }

    @Test
    public void testExists_whenNonExistingTenantNameGiven() {
        assertFalse(sut.exists("id-that-does-not-exist"));
        assertFalse(sut.exists(""));
    }

    @Test
    public void testGetTenantInfo_whenExistingTenantNameGiven() {
        for (int i = 0; i < ids.length; i++) {
            TenantInfo tenantInfo = sut.getTenantInfo(ids[i]);
            assertEquals(tenantInfo.getName(), ids[i]);
            assertEquals(tenantInfo.getComplianceRuleSet(), complianceRuleSets[i]);
        }
    }

    @Test
    public void testListTenantInfo() {
        List<TenantInfo> tenantInfoList = new ArrayList<TenantInfo> (sut.listTenantInfo());
        for (TenantInfo tenantInfo: tenantInfoList) {
            assertTrue(ArrayUtils.contains(ids, tenantInfo.getName()));
            assertTrue(ArrayUtils.contains(complianceRuleSets, tenantInfo.getComplianceRuleSet()));
        }
    }
}

