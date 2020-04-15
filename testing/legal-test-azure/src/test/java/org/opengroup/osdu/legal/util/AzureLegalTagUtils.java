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

package org.opengroup.osdu.legal.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlockBlobClient;
import com.azure.storage.blob.ContainerClient;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.google.common.base.Strings;

public class AzureLegalTagUtils extends LegalTagUtils {
    private static final String FILE_NAME = "Legal_COO.json";
    private static final String CONTAINER_NAME_AZURE = "legal-service-azure-configuration";

    @Override
    public synchronized void uploadTenantTestingConfigFile() {
        try {
            String storageAccount = System.getProperty("AZURE_LEGAL_STORAGE_ACCOUNT", System.getenv("AZURE_LEGAL_STORAGE_ACCOUNT")).toLowerCase();
            String storageAccountKey = System.getProperty("AZURE_LEGAL_STORAGE_KEY", System.getenv("AZURE_LEGAL_STORAGE_KEY"));
            SharedKeyCredential credential = new SharedKeyCredential(storageAccount, storageAccountKey);
            BlobServiceClient storageClient = new BlobServiceClientBuilder()
                    .endpoint(String.format("https://%s.blob.core.windows.net", storageAccount))
                    .credential(credential)
                    .buildClient();
            ContainerClient containerClient = storageClient
                    .getContainerClient(CONTAINER_NAME_AZURE);
            BlockBlobClient blobClient = containerClient.getBlockBlobClient(FILE_NAME);
            String content = readTestFile("TenantConfigTestingPurpose.json");
            InputStream dataStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            blobClient.upload(dataStream, content.length());
            dataStream.close();
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
    
    @Override
    public synchronized String accessToken() throws Exception {
        if (Strings.isNullOrEmpty(token)) {
            String sp_id = System.getProperty("INTEGRATION_TESTER", System.getenv("INTEGRATION_TESTER"));
            String sp_secret = System.getProperty("AZURE_TESTER_SERVICEPRINCIPAL_SECRET", System.getenv("AZURE_TESTER_SERVICEPRINCIPAL_SECRET"));
            String tenant_id = System.getProperty("AZURE_AD_TENANT_ID", System.getenv("AZURE_AD_TENANT_ID"));
            String app_resource_id = System.getProperty("AZURE_AD_APP_RESOURCE_ID", System.getenv("AZURE_AD_APP_RESOURCE_ID"));
            token = AzureServicePrincipal.getIdToken(sp_id, sp_secret, tenant_id, app_resource_id);
        }
        return "Bearer " + token;
    }

    public static String getAzureServiceBusConnectionString() {
		return System.getProperty("AZURE_LEGAL_SERVICEBUS", System.getenv("AZURE_LEGAL_SERVICEBUS"));
    }
    
	public static String getAzureServiceBusTopicName() {
	    return System.getProperty("AZURE_LEGAL_TOPICNAME", System.getenv("AZURE_LEGAL_TOPICNAME"));
    }
}
