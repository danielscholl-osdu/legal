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

public class MinIoConfig {

    private String minIoEndpointUrl;
    private String minIoAccessKey;
    private String minIoSecretKey;

    private static final MinIoConfig minIoConfig = new MinIoConfig();

    public static MinIoConfig Instance() {
        minIoConfig.minIoEndpointUrl = System.getProperty("TEST_MINIO_URL", System.getenv("TEST_MINIO_URL"));
        minIoConfig.minIoAccessKey = System.getProperty("TEST_MINIO_ACCESS_KEY", System.getenv("TEST_MINIO_ACCESS_KEY"));
        minIoConfig.minIoSecretKey = System.getProperty("TEST_MINIO_SECRET_KEY", System.getenv("TEST_MINIO_SECRET_KEY"));
        return minIoConfig;
    }

    public String getMinIoEndpointUrl() {
        return minIoEndpointUrl;
    }

    public String getMinIoAccessKey() {
        return minIoAccessKey;
    }

    public String getMinIoSecretKey() {
        return minIoSecretKey;
    }
}
