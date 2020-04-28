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

package org.opengroup.osdu.legal.util;

import java.io.IOException;
import com.amazonaws.services.s3.AmazonS3;
import org.opengroup.osdu.core.aws.cognito.AWSCognitoClient;
import org.opengroup.osdu.core.aws.s3.S3Config;

public class AwsLegalTagUtils extends LegalTagUtils {
    private static final String FILE_NAME = "Legal_COO.json";
    private static final String BUCKET_NAME_AWS = System.getProperty("S3_LEGAL_CONFIG_BUCKET", System.getenv("S3_LEGAL_CONFIG_BUCKET"));

    private final static String COGNITO_CLIENT_ID_PROPERTY = "AWS_COGNITO_CLIENT_ID";
    private final static String COGNITO_AUTH_FLOW_PROPERTY = "AWS_COGNITO_AUTH_FLOW";
    private final static String COGNITO_AUTH_PARAMS_USER_PROPERTY = "AWS_COGNITO_AUTH_PARAMS_USER";
    private final static String COGNITO_AUTH_PARAMS_PASSWORD_PROPERTY = "AWS_COGNITO_AUTH_PARAMS_PASSWORD";

    @Override
    public synchronized void uploadTenantTestingConfigFile() {
        String amazonS3Endpoint = System.getProperty("AWS_S3_ENDPOINT", System.getenv("AWS_S3_ENDPOINT"));
        String amazonS3Region = System.getProperty("AWS_S3_REGION", System.getenv("AWS_S3_REGION"));

        S3Config s3Config = new S3Config(amazonS3Endpoint, amazonS3Region);
        AmazonS3 s3Client = s3Config.amazonS3();
        try {
            s3Client.putObject(BUCKET_NAME_AWS, FILE_NAME, readTestFile("TenantConfigTestingPurpose.json"));
        } catch(IOException e){
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    @Override
    public synchronized String accessToken() throws Exception {
        String clientId = System.getProperty(COGNITO_CLIENT_ID_PROPERTY, System.getenv(COGNITO_CLIENT_ID_PROPERTY));
        String authFlow = System.getProperty(COGNITO_AUTH_FLOW_PROPERTY, System.getenv(COGNITO_AUTH_FLOW_PROPERTY));
        String user = System.getProperty(COGNITO_AUTH_PARAMS_USER_PROPERTY, System.getenv(COGNITO_AUTH_PARAMS_USER_PROPERTY));
        String password = System.getProperty(COGNITO_AUTH_PARAMS_PASSWORD_PROPERTY, System.getenv(COGNITO_AUTH_PARAMS_PASSWORD_PROPERTY));

        AWSCognitoClient client = new AWSCognitoClient(clientId, authFlow, user, password);

        return "Bearer " + client.getToken();
    }
}
