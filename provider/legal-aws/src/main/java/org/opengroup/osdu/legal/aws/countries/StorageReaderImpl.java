// Copyright © Amazon Web Services
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.legal.aws.countries;

import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.aws.tags.dataaccess.S3RecordClient;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;

public class StorageReaderImpl implements IStorageReader {
    private final TenantInfo tenantInfo;
    private S3RecordClient client;

    public StorageReaderImpl(TenantInfo tenantInfo, String legalConfigBucketName, String legalConfigFileName,
                             String awsS3Endpoint, String awsS3Region) {
        this.tenantInfo = tenantInfo;
        client = new S3RecordClient(legalConfigBucketName, legalConfigFileName, awsS3Endpoint, awsS3Region);
    }

    @Override
    public byte[] readAllBytes() {
        return (client.getConfigFile().getBytes()); //should return a json format of an array of Country class
    }
}
