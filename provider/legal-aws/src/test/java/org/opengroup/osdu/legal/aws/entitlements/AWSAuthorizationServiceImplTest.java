/**
 *  Copyright © Amazon
 * Copyright 2017-2019, Schlumberger
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

package org.opengroup.osdu.legal.aws.entitlements;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsFactory;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsService;
import org.opengroup.osdu.core.common.logging.DefaultLogger;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.entitlements.*;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.test.util.ReflectionTestUtils;

import org.opengroup.osdu.core.common.http.HttpResponse;
import com.lambdaworks.redis.RedisException;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AWSAuthorizationServiceImplTest {

    private static final String MEMBER_EMAIL = "tester@gmail.com";
    private static final String HEADER_ACCOUNT_ID = "anyTenant";
    private static final String HEADER_AUTHORIZATION = "anyCrazyToken";

    @Mock
    private IEntitlementsFactory entitlementFactory;

    @Mock
    private ICache<String, Groups> cache;

    private DpsHeaders headers;

    @Mock
    private IEntitlementsService entitlementService;

    private final JaxRsDpsLog logger = new JaxRsDpsLog(new DefaultLogger(), new DpsHeaders());

    @Mock
    private DpsHeaders dpsHeaders;

    @InjectMocks
    private AWSAuthorizationServiceImpl entitlementsAndCacheService;

    private static final Map<String, String> headerMap = new HashMap<>();
    private final String[] rolesNames = {"role1", "role2"};
    private final String[] rolesEmails = {"role1@gmail.com", "role2@gmail.com"};
    private static final String ERROR_REASON = "Access denied";
	private static final String ERROR_MSG = "The user is not authorized to perform this action";

    @BeforeEach
    void setUp() {
        setDefaultHeaders();

        headers = DpsHeaders.createFromMap(headerMap);

        ReflectionTestUtils.setField(entitlementsAndCacheService, "jaxRsDpsLog", logger);
    }

    private void setDefaultHeaders() {
        headerMap.put(DpsHeaders.ACCOUNT_ID, HEADER_ACCOUNT_ID);
        headerMap.put(DpsHeaders.AUTHORIZATION, HEADER_AUTHORIZATION);
    }

    @Test
    void should_returnMemberEmail_when_authorizationIsSuccessful() throws Exception {
        when(entitlementFactory.create(any())).thenReturn(entitlementService);

        GroupInfo g1 = new GroupInfo();
        g1.setEmail(rolesEmails[0]);
        g1.setName(rolesNames[0]);

        GroupInfo g2 = new GroupInfo();
        g2.setEmail(rolesEmails[1]);
        g2.setName(rolesNames[1]);

        List<GroupInfo> groupsInfo = new ArrayList<>();
        groupsInfo.add(g1);
        groupsInfo.add(g2);

        Groups groups = new Groups();
        groups.setGroups(groupsInfo);
        groups.setMemberEmail(MEMBER_EMAIL);
        groups.setDesId(MEMBER_EMAIL);

        when(entitlementService.getGroups()).thenReturn(groups);

        AuthorizationResponse response = AuthorizationResponse.builder().groups(groups).user(MEMBER_EMAIL).build();


        assertEquals(response, entitlementsAndCacheService.authorizeAny(headers, rolesNames));
    }

    @Test
    void should_returnRedisException_when_getGroupsThrowsRedisException() throws Exception {
        when(entitlementFactory.create(any())).thenReturn(entitlementService);
        when(cache.get(any())).thenReturn(null);
        when(entitlementService.getGroups()).thenThrow(new RedisException("test exception"));
        assertDoesNotThrow(() -> entitlementsAndCacheService.getGroups(dpsHeaders));
    }

    @Test
    void should_returnEntitlementsException_when_getGroupsThrowsEntitlementsException() throws Exception {
        when(entitlementFactory.create(any())).thenReturn(entitlementService);

        HttpResponse response = new HttpResponse();
        int uniqueError = 599;
        response.setResponseCode(uniqueError);
        EntitlementsException exception = new EntitlementsException("Service error", response);

        when(cache.get(any())).thenReturn(null);
        when(entitlementService.getGroups()).thenThrow(exception);
        AppException thrownException = assertThrows(AppException.class, () -> entitlementsAndCacheService.getGroups(dpsHeaders));

        Assertions.assertEquals(uniqueError, thrownException.getError().getCode());
        Assertions.assertEquals(ERROR_REASON, thrownException.getError().getReason());
        Assertions.assertEquals(ERROR_MSG, thrownException.getError().getMessage());
    }

    @Test
    void should_returnGroups_when_cacheKeyNotNull() {
        Groups groups = new Groups();
        when(cache.get(any())).thenReturn(groups);
        entitlementsAndCacheService.getGroups(dpsHeaders);
        Assertions.assertNotNull(groups);
    }

    @Test
    void should_throwRedisException_when_cacheGetKeyThrowsRedisException() {
        when(entitlementFactory.create(any())).thenReturn(entitlementService);
        when(cache.get(any())).thenThrow(new RedisException("test exception"));
        assertDoesNotThrow(() -> entitlementsAndCacheService.getGroups(dpsHeaders));
    }

    @Test 
    void should_returnAuthorizationResponse_when_AuthorizeAnyGetsCalledWithTenantName() throws Exception{
        when(entitlementFactory.create(any())).thenReturn(entitlementService);

        String tenantName = rolesEmails[0].split("@")[1];
        GroupInfo g1 = new GroupInfo();
        g1.setEmail(rolesEmails[0]);
        g1.setName(rolesNames[0]);

        GroupInfo g2 = new GroupInfo();
        g2.setEmail(rolesEmails[1]);
        g2.setName(rolesNames[1]);

        List<GroupInfo> groupsInfo = new ArrayList<>();
        groupsInfo.add(g1);
        groupsInfo.add(g2);

        Groups groups = new Groups();
        groups.setGroups(groupsInfo);
        groups.setMemberEmail(MEMBER_EMAIL);
        groups.setDesId(MEMBER_EMAIL);

        when(entitlementService.getGroups()).thenReturn(groups);

        AuthorizationResponse response = AuthorizationResponse.builder().groups(groups).user(MEMBER_EMAIL).build();


        Assertions.assertEquals(response, entitlementsAndCacheService.authorizeAny(tenantName, headers, rolesNames));
    }
}
