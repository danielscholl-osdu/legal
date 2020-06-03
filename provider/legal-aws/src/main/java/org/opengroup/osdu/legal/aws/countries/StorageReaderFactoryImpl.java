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

import org.opengroup.osdu.core.aws.ssm.ParameterStorePropertySource;
import org.opengroup.osdu.core.aws.ssm.SSMConfig;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReaderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StorageReaderFactoryImpl implements IStorageReaderFactory {

    private String legalConfigBucketName;

    @Value("${aws.s3.legal.config.file-name}")
    private String legalConfigFileName;

    @Value("${aws.s3.endpoint}")
    private String awsS3Endpoint;

    @Value("${aws.s3.region}")
    private String awsS3Region;

    @Value("${aws.legal.s3.bucket.name}")
    String parameter;

    private ParameterStorePropertySource ssm;

    @Override
    public IStorageReader getReader(TenantInfo tenant, String projectRegion) {
        SSMConfig ssmConfig = new SSMConfig();
        ssm = ssmConfig.amazonSSM();
        legalConfigBucketName = ssm.getProperty(parameter).toString();
        return new StorageReaderImpl(tenant, legalConfigBucketName, legalConfigFileName, awsS3Endpoint, awsS3Region);
    }
}
