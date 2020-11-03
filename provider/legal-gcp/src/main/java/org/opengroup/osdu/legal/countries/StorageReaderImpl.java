package org.opengroup.osdu.legal.countries;

import com.google.cloud.storage.*;

import java.util.Objects;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;
import org.springframework.http.MediaType;

import static java.nio.charset.StandardCharsets.UTF_8;

public class StorageReaderImpl implements IStorageReader {

    private TenantInfo    tenantInfo;
    private String        projectRegion;
    private Storage       storage;

    protected static final String BUCKET_NAME = "legal-service-configuration";
    private static final String FILE_NAME = "Legal_COO.json";
    private Boolean isFullBucketName = false;

    public StorageReaderImpl(TenantInfo tenantInfo, String projectRegion) {
      new StorageReaderImpl(tenantInfo, projectRegion, false);
    }

  public StorageReaderImpl(TenantInfo tenantInfo, String projectRegion, Boolean isFullBucketName) {
        this.tenantInfo = tenantInfo;
        this.projectRegion = projectRegion;
        this.storage = getStorage();
        this.isFullBucketName = isFullBucketName;
  }

  @Override
    public byte[] readAllBytes() {
        BlobId blobId = getBlobId();
        byte[] content = null;

        try {
            content = storage.readAllBytes(blobId);
        } 
        catch (StorageException e) {
            if (storage.get(getTenantBucketName(), Storage.BucketGetOption.fields()) == null) {
                createBucket();
            }
            if (storage.get(blobId) == null) {
                createEmptyObject();
            }
        }
        return content;
    }

    private Storage getStorage() {
        return StorageOptions.newBuilder()
                             .setCredentials(new CloudStorageCredential(this.tenantInfo))
                             .setProjectId(this.tenantInfo.getProjectId())
                             .build()
                             .getService();
    }
    
    private BlobId getBlobId() {
        return BlobId.of(getTenantBucketName(), FILE_NAME);
    }

    private void createBucket() {
        this.storage.create(BucketInfo.newBuilder(getTenantBucketName())
                    .setStorageClass(StorageClass.MULTI_REGIONAL)
                    .setLocation(this.projectRegion)
                    .build());
    }

    private void createEmptyObject() {
        BlobId blobId = getBlobId();
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(MediaType.APPLICATION_JSON.toString()).build();
        this.storage.create(blobInfo, "".getBytes(UTF_8));
    }

  protected String getTenantBucketName() {
      if (Objects.nonNull(isFullBucketName) && isFullBucketName) {
        return this.tenantInfo.getProjectId() + "-" + this.tenantInfo.getName() + "-" + BUCKET_NAME;
      }
      return this.tenantInfo.getName() + "-" + BUCKET_NAME;
    }

}
