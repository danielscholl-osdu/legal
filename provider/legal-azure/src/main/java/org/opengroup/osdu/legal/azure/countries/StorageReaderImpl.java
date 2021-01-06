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

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class StorageReaderImpl implements IStorageReader {
    private final TenantInfo tenantInfo;
    private final String cloudRegion;
    private BlobContainerClient blobContainerClient;
    private JaxRsDpsLog logger;

    private static final String fileName = "Legal_COO.json";

    public StorageReaderImpl(TenantInfo tenantInfo, String cloudRegion, BlobContainerClient blobContainerClient, JaxRsDpsLog logger) {
        this.tenantInfo = tenantInfo;
        this.cloudRegion = cloudRegion;
        this.blobContainerClient = blobContainerClient;
        this.logger = logger;
    }

    @Override
    public byte[] readAllBytes() {
        return  readFromBlobStorage().getBytes(StandardCharsets.UTF_8); //should return a json format of an array of Country class
    }

    /**
     * This function should be fail close. It reads the pre-configuration file for the partition. As a security requirement, we should
     * fail the corresponding request instead of assuming the content is empty, but it should only fail close on the request level not
     * service level meaning the service could still start and running probably for the APIs which do not need the pre-configuration file
     */
    public String readFromBlobStorage() {

        BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient(fileName).getBlockBlobClient();
        if (blockBlobClient.exists())
        {
            try (ByteArrayOutputStream downloadStream = new ByteArrayOutputStream()) {
                blockBlobClient.download(downloadStream);
                String content = downloadStream.toString(StandardCharsets.UTF_8.name());
                return content;
            } catch (Exception e) {
                String message = String.format("read %s failed", fileName);
                throw new AppException(500, "Server error", message);
            }
        } else {
            String message = String.format("%s does not exist on partition %s", fileName, tenantInfo.getDataPartitionId());
            throw new AppException(500, "Server error", message);
        }
    }
}