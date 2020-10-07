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

package org.opengroup.osdu.legal.azure.countries;

import javax.inject.Inject;
import javax.inject.Named;

import org.opengroup.osdu.azure.blobstorage.IBlobContainerClientFactory;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReaderFactory;
import org.springframework.stereotype.Component;

@Component
public class StorageReaderFactoryImpl implements IStorageReaderFactory {

    @Inject
    private IBlobContainerClientFactory blobContainerClientFactory;

    @Inject
    private DpsHeaders headers;

    @Inject
    @Named("STORAGE_CONTAINER_NAME")
    private String containerName;

    @Inject
    private JaxRsDpsLog logger;

    @Override
    public IStorageReader getReader(TenantInfo tenant, String projectRegion) {
        return new StorageReaderImpl(tenant, projectRegion, blobContainerClientFactory.getClient(headers.getPartitionId(), containerName), logger);
    }
}
