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

package legal.util;

import com.google.common.base.Strings;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import legal.service.PartitionService;
import legal.util.conf.CloudObjectStorageFactory;
import lombok.extern.java.Log;
import org.opengroup.osdu.legal.util.LegalTagUtils;

@Log
public class AnthosLegalTagUtils extends LegalTagUtils {

    private static final String BUCKET_NAME = "legal-service-configuration";
    private static final String FILE_NAME = "Legal_COO.json";
    private static final CloudObjectStorageFactory storageFactory = new CloudObjectStorageFactory();
    private static final OpenIDTokenProvider tokenProvider = new OpenIDTokenProvider();

    public AnthosLegalTagUtils() {
    }

    @Override
    public synchronized void uploadTenantTestingConfigFile() {
        try {
            MinioClient client = storageFactory.getClient();
            byte[] tenantConfigFileContent = getTenantConfigFileContent();
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("X-Amz-Storage-Class", "REDUCED_REDUNDANCY");
            ObjectWriteResponse objectWriteResponse = client.putObject(
                PutObjectArgs.builder()
                    .bucket(getTenantBucketName())
                    .object(FILE_NAME)
                    .stream(new ByteArrayInputStream(tenantConfigFileContent), tenantConfigFileContent.length, -1)
                    .headers(headers)
                    .build());
            System.out.println(objectWriteResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getTenantBucketName() {
        String tenantName = System.getProperty("MY_TENANT", System.getenv("MY_TENANT")).toLowerCase();
        String projectName = System.getProperty("BAREMETAL_PROJECT_ID", System.getenv("BAREMETAL_PROJECT_ID")).toLowerCase();
        String enableFullBucketName = System.getProperty("ENABLE_FULL_BUCKET_NAME", System.getenv("ENABLE_FULL_BUCKET_NAME"));
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

        enableFullBucketName = (Strings.isNullOrEmpty(enableFullBucketName) ? "false"
            : enableFullBucketName).toLowerCase();

        String bucketName;
        if (Boolean.parseBoolean(enableFullBucketName)) {
            bucketName = projectName + "-" + tenantName + "-" + BUCKET_NAME;
        } else {
            bucketName = tenantName + "-" + BUCKET_NAME;
        }
        return bucketName;
    }

    @Override
    public synchronized String accessToken() throws Exception {
        if (Strings.isNullOrEmpty(token)) {
            token = tokenProvider.getToken();
        }
        return "Bearer " + token;
    }
}
