/**
* Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*      http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.opengroup.osdu.legal.util;

import com.amazonaws.services.s3.AmazonS3;
import org.opengroup.osdu.core.aws.cognito.AWSCognitoClient;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelperV2;
import org.opengroup.osdu.core.aws.s3.S3Config;
import org.opengroup.osdu.core.common.model.legal.LegalTag;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class AwsLegalTagUtils extends LegalTagUtils {
    private static final String FILE_NAME = "Legal_COO.json";
    private static final String BUCKET_NAME_AWS = System.getProperty("S3_LEGAL_CONFIG_BUCKET", System.getenv("S3_LEGAL_CONFIG_BUCKET"));

    private final static String COGNITO_CLIENT_ID_PROPERTY = "AWS_COGNITO_CLIENT_ID";
    private final static String COGNITO_AUTH_FLOW_PROPERTY = "AWS_COGNITO_AUTH_FLOW";
    private final static String COGNITO_AUTH_PARAMS_USER_PROPERTY = "AWS_COGNITO_AUTH_PARAMS_USER";
    private final static String COGNITO_AUTH_PARAMS_PASSWORD_PROPERTY = "AWS_COGNITO_AUTH_PARAMS_PASSWORD";

    private final static String TABLE_PREFIX = "TABLE_PREFIX";
    private final static String DYNAMO_DB_REGION = "DYNAMO_DB_REGION";
    private final static String DYNAMO_DB_ENDPOINT = "DYNAMO_DB_ENDPOINT";
    private static final String COLLECTION_PREFIX = "Legal-";


    private String BearerToken = "";

    @Override
    public synchronized void uploadTenantTestingConfigFile() {
        String amazonS3Endpoint = System.getProperty("AWS_S3_ENDPOINT", System.getenv("AWS_S3_ENDPOINT"));
        String amazonS3Region = System.getProperty("AWS_S3_REGION", System.getenv("AWS_S3_REGION"));
        String dataPartitionId = System.getProperty("MY_TENANT", System.getenv("MY_TENANT"));

        S3Config s3Config = new S3Config(amazonS3Endpoint, amazonS3Region);
        AmazonS3 s3Client = s3Config.amazonS3();

        try {
            s3Client.putObject(BUCKET_NAME_AWS, String.format("%s/%s", dataPartitionId, FILE_NAME), readTestFile("TenantConfigTestingPurpose.json"));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public synchronized String accessToken() throws Exception {
        if (BearerToken == "") {
            String clientId = System.getProperty(COGNITO_CLIENT_ID_PROPERTY, System.getenv(COGNITO_CLIENT_ID_PROPERTY));
            String authFlow = System.getProperty(COGNITO_AUTH_FLOW_PROPERTY, System.getenv(COGNITO_AUTH_FLOW_PROPERTY));
            String user = System.getProperty(COGNITO_AUTH_PARAMS_USER_PROPERTY, System.getenv(COGNITO_AUTH_PARAMS_USER_PROPERTY));
            String password = System.getProperty(COGNITO_AUTH_PARAMS_PASSWORD_PROPERTY, System.getenv(COGNITO_AUTH_PARAMS_PASSWORD_PROPERTY));

            AWSCognitoClient client = new AWSCognitoClient(clientId, authFlow, user, password);
            BearerToken = client.getToken();
        }
        return "Bearer " + BearerToken;
    }

    public void insertExpiredLegalTag() {
        // directly create expired legal tag document
        String integrationTagTestName = String.format("%s-dps-integration-test-1566474656479", getMyDataPartition()); // name has to match what's hardcoded in the test
        LegalDoc doc = new LegalDoc();
        doc.setDescription("Expired integration test tag");
        doc.setName(integrationTagTestName);
        doc.setId(Integer.toString(integrationTagTestName.hashCode()));
        doc.setProperties(getLegalTagProperties());
        doc.setDataPartitionId(getMyDataPartition());

        String dynamoDbRegion = System.getenv(DYNAMO_DB_REGION);
        String dynamoDbEndpoint = System.getenv(DYNAMO_DB_ENDPOINT);

        String table = String.format("%s-shared-LegalRepository", System.getenv(TABLE_PREFIX));
        DynamoDBQueryHelperV2 queryHelper = new DynamoDBQueryHelperV2(dynamoDbEndpoint, dynamoDbRegion, table);

        // delete legal tag if it exists
        if (queryHelper.keyExistsInTable(LegalDoc.class, doc)) {
            queryHelper.deleteByPrimaryKey(LegalDoc.class, doc.getId(), doc.getDataPartitionId());
        }

        queryHelper.save(doc);
    }

    private org.opengroup.osdu.core.common.model.legal.Properties getLegalTagProperties(){
        org.opengroup.osdu.core.common.model.legal.Properties properties = new org.opengroup.osdu.core.common.model.legal.Properties();
        List<String> countryOfOrigin = new ArrayList<>();
        Date date = new Date(1234567898765L);
        countryOfOrigin.add("US");
        properties.setCountryOfOrigin(countryOfOrigin);
        properties.setContractId("A1234");
        properties.setExpirationDate(date);
        properties.setOriginator("MyCompany");
        properties.setDataType("Transferred Data");
        properties.setSecurityClassification("Public");
        properties.setPersonalData("No Personal Data");
        properties.setExportClassification("EAR99");
        return properties;
    }
}
