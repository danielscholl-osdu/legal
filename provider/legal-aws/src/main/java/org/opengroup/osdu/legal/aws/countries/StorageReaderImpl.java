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

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import org.opengroup.osdu.core.aws.s3.IS3ClientFactory;
import org.opengroup.osdu.core.aws.s3.S3ClientFactory;
import org.opengroup.osdu.core.aws.s3.S3ClientWithBucket;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class StorageReaderImpl implements IStorageReader {

    @Inject
    private DpsHeaders dpsHeaders;

    @Inject
    private IS3ClientFactory s3ClientFactory;

    @Value("${aws.s3.configbucket.ssm.relativePath}")
    private String s3ConfigBucketParameterRelativePath;

    @Value("${aws.s3.legal.config.file-name}")
    private String legalConfigFileName;

    @Override
    public byte[] readAllBytes() {
        return getConfigFile().getBytes(); //should return a json format of an array of Country class
    }

    public String getConfigFile(){
        S3ClientWithBucket s3ClientWithBucket = s3ClientFactory.getS3ClientForPartition(
                dpsHeaders.getPartitionId(), s3ConfigBucketParameterRelativePath);
        AmazonS3 s3 = s3ClientWithBucket.getS3Client();
        String legalConfigBucketName = s3ClientWithBucket.getBucketName();

        String contents = "";
        try {
            contents = s3.getObjectAsString(legalConfigBucketName, legalConfigFileName);
        } catch (AmazonServiceException e) {
            throw new AppException(500, "Amazon service exception getting config file", e.getMessage());
        } catch (SdkClientException e) {
            throw new AppException(500, "Amazon Sdk client exception getting config file", e.getMessage());
        }
        return contents;
    }
}
