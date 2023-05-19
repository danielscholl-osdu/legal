/*
 * Copyright 2021 Google LLC
 * Copyright 2021 EPAM Systems, Inc
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

package legal.util.conf;

import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CloudObjectStorageFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudObjectStorageFactory.class);
    private final MinIoConfig minIoConfig = MinIoConfig.Instance();

    private MinioClient minioClient;

    public CloudObjectStorageFactory() {
        init();
    }

    public void init() {
        minioClient = MinioClient.builder()
            .endpoint(minIoConfig.getMinIoEndpointUrl())
            .credentials(minIoConfig.getMinIoAccessKey(), minIoConfig.getMinIoSecretKey())
            .build();
        LOGGER.info("Minio client initialized");
    }

    public MinioClient getClient() {
        return this.minioClient;
    }

    public void setMinioClient(MinioClient minioClient) {
        this.minioClient = minioClient;
    }
}
