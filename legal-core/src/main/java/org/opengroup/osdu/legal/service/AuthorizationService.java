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
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.entitlements.AuthorizationResponse;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.provider.interfaces.IAuthorizationService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary // overrides class in core common
public class AuthorizationService implements IAuthorizationService {

    private final IEntitlementsExtensionService entitlementsService;

    public AuthorizationService(IEntitlementsExtensionService entitlementsService) {
        this.entitlementsService = entitlementsService;
    }

    @Override
    public AuthorizationResponse authorizeAny(DpsHeaders dpsHeaders, String... permissions) {
        Groups groups = entitlementsService.getGroups(dpsHeaders);
        if(Boolean.FALSE.equals(groups.any(permissions))) {
            throw new AppException(HttpStatus.SC_UNAUTHORIZED, "Unauthorized", "User does nto have access to the API");
        }
        return AuthorizationResponse.builder()
                .user(groups.getMemberEmail())
                .groups(groups)
                .build();
    }

    @Override
    public AuthorizationResponse authorizeAny(String partition, DpsHeaders dpsHeaders, String... permissions) {
        throw new NotImplementedException("authorizeAny not implemented");
    }
}
