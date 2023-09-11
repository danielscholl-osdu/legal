/*
 * Copyright 2020-2023 Google LLC
 * Copyright 2020-2023 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.legal.util;

import com.google.api.client.util.Strings;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;
import lombok.extern.java.Log;
import org.opengroup.osdu.legal.service.PartitionService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Objects;

@Log
public class GCPLegalTagUtils extends LegalTagUtils {
    private static final String BUCKET_NAME = "legal-service-configuration";
    private static final String FILE_NAME = "Legal_COO.json";

    @Override
    public synchronized void uploadTenantTestingConfigFile() {
        try {
            String serviceAccountFile = System.getProperty("INTEGRATION_TESTER", System.getenv("INTEGRATION_TESTER"));
            try (InputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(serviceAccountFile))) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream)
                        .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
                Storage storage = StorageOptions.newBuilder()
                        .setCredentials(credentials)
                        .setProjectId(System.getProperty("MY_TENANT_PROJECT", System.getenv("MY_TENANT_PROJECT")))
                        //.setProjectId(System.getenv("MY_TENANT_PROJECT"))
                        .build().getService();
                BlobId blobId = BlobId.of(getTenantBucketName(), FILE_NAME);
                BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("application/json").build();
                storage.create(blobInfo, getTenantConfigFileContent());
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

  private static String getTenantBucketName() {
    String tenantName = System.getProperty("MY_TENANT", System.getenv("MY_TENANT")).toLowerCase();
    String projectName =
        System.getProperty("GCLOUD_PROJECT", System.getenv("GCLOUD_PROJECT")).toLowerCase();
    String enableFullBucketName =
        System.getProperty("ENABLE_FULL_BUCKET_NAME", System.getenv("ENABLE_FULL_BUCKET_NAME"));
    String legalBucketName;
    try {
      legalBucketName =
          PartitionService.getPartitionProperty("partition.properties.legal.bucketName");
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }

    if (Objects.nonNull(legalBucketName)){
        log.info("Bucket name is using from Partition Service");
        return legalBucketName;
    }

    enableFullBucketName =
        (Strings.isNullOrEmpty(enableFullBucketName) ? "false" : enableFullBucketName)
            .toLowerCase();

    log.info("ENABLE_FULL_BUCKET_NAME = " + enableFullBucketName);

    if (Boolean.parseBoolean(enableFullBucketName)) {
        legalBucketName = projectName + "-" + tenantName + "-" + BUCKET_NAME;
    } else {
        legalBucketName = tenantName + "-" + BUCKET_NAME;
    }

    log.info("bucketName = " + legalBucketName);
    return legalBucketName;
  }

    @Override
    public synchronized String accessToken() throws Exception {
        if (Strings.isNullOrEmpty(token)) {
            String serviceAccountFile = System.getProperty("INTEGRATION_TESTER", System.getenv("INTEGRATION_TESTER"));
            token = new GoogleServiceAccount(serviceAccountFile).getAuthToken();
        }
        return "Bearer " + token;
    }
}
