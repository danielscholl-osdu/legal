//  Copyright © SLB
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

package org.opengroup.osdu.legal.service;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.entitlements.AuthorizationResponse;
import org.opengroup.osdu.core.common.model.entitlements.GroupInfo;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizationServiceTest {

    private static final String MEMBER_EMAIL = "memberEmail";

    @Mock
    private IEntitlementsExtensionService entitlementsService;

    @InjectMocks
    private AuthorizationService sut;

    @Test(expected = AppException.class)
    public void should_throwAppException_when_givenGroupDoesNotExistForUser() {
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setName("c");
        Groups group = new Groups();
        group.setGroups(List.of(groupInfo));
        when(entitlementsService.getGroups(any())).thenReturn(group);

        sut.authorizeAny(new DpsHeaders(), "a", "b");
    }

    @Test
    public void should_returnGroupsWithUserEmail_when_givenGroupExistsForUser() {
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setName("a");
        Groups group = new Groups();
        group.setGroups(List.of(groupInfo));
        group.setMemberEmail(MEMBER_EMAIL);
        when(entitlementsService.getGroups(any())).thenReturn(group);

        AuthorizationResponse response = sut.authorizeAny(new DpsHeaders(), "a", "b");

        assertEquals(MEMBER_EMAIL, response.getUser());
        assertEquals(group, response.getGroups());
    }

    @Test(expected = NotImplementedException.class)
    public void should_throwNotImplementedException_when_authorizeAnyWithPartitionParameter() {
        String partition = "partition";
        sut.authorizeAny(partition, new DpsHeaders(), "a", "b");
    }
}
