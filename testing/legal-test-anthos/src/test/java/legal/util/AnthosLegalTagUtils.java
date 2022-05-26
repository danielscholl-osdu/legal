package legal.util;

import com.google.common.base.Strings;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import legal.util.conf.CloudObjectStorageFactory;
import org.opengroup.osdu.legal.util.LegalTagUtils;

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
        String projectName = System.getProperty("ANTHOS_PROJECT_ID", System.getenv("ANTHOS_PROJECT_ID")).toLowerCase();
        String enableFullBucketName = System.getProperty("ENABLE_FULL_BUCKET_NAME", System.getenv("ENABLE_FULL_BUCKET_NAME"));

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
