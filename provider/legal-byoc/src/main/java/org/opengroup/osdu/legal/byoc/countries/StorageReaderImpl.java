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

package org.opengroup.osdu.legal.byoc.countries;

import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;

public class StorageReaderImpl implements IStorageReader {
    private final TenantInfo tenantInfo;
    private final String cloudRegion;

    public StorageReaderImpl(TenantInfo tenantInfo, String cloudRegion) {
        this.tenantInfo = tenantInfo;
        this.cloudRegion = cloudRegion;
    }

    @Override
    public byte[] readAllBytes() {
        return ("").getBytes(); //should return a json format of an array of Country class
    }
}
