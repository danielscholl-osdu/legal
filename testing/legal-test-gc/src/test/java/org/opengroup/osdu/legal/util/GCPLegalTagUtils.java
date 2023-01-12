package org.opengroup.osdu.legal.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import com.google.api.client.util.Strings;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;
import lombok.extern.java.Log;

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
    String tenantName = System
        .getProperty("MY_TENANT_PROJECT", System.getenv("MY_TENANT_PROJECT")).toLowerCase();
    String projectName = System.getProperty("GCLOUD_PROJECT", System.getenv("GCLOUD_PROJECT"))
        .toLowerCase();
    String enableFullBucketName = System.getProperty("ENABLE_FULL_BUCKET_NAME",
        System.getenv("ENABLE_FULL_BUCKET_NAME"));

    enableFullBucketName = (Strings.isNullOrEmpty(enableFullBucketName) ? "false"
        : enableFullBucketName).toLowerCase();

    log.info("ENABLE_FULL_BUCKET_NAME = " + enableFullBucketName);

    String bucketName;
    if (Boolean.parseBoolean(enableFullBucketName)) {
      bucketName = projectName + "-" + tenantName + "-" + BUCKET_NAME;
    } else {
      bucketName = tenantName + "-" + BUCKET_NAME;
    }

    log.info("bucketName = " + bucketName);
    return bucketName;
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
