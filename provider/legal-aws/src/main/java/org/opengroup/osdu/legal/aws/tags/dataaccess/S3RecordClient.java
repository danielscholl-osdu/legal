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

package org.opengroup.osdu.legal.aws.tags.dataaccess;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import org.opengroup.osdu.core.aws.s3.S3Config;
import org.opengroup.osdu.core.common.model.http.AppException;

public class S3RecordClient {

    // Storing all records in one bucket does not impact performance, no need to spread keys anymore
    private String legalConfigBucketName;
    private String legalConfigFileName;

    private AmazonS3 s3;

    public S3RecordClient(String legalConfigBucketName, String legalConfigFileName, String awsS3Endpoint, String awsS3Region){
        this.legalConfigBucketName = legalConfigBucketName;
        this.legalConfigFileName = legalConfigFileName;
        S3Config s3Config = new S3Config(awsS3Endpoint, awsS3Region);
        s3 = s3Config.amazonS3();
    }

    public String getConfigFile(){
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
