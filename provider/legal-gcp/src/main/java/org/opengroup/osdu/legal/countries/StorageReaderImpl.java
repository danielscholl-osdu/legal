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
package org.opengroup.osdu.legal.countries;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.gcp.obm.driver.Driver;
import org.opengroup.osdu.core.gcp.obm.driver.ObmDriverRuntimeException;
import org.opengroup.osdu.core.gcp.obm.model.Blob;
import org.opengroup.osdu.core.gcp.obm.persistence.ObmDestination;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;
import org.springframework.http.MediaType;

@RequiredArgsConstructor
@Slf4j
public class StorageReaderImpl implements IStorageReader {

  private TenantInfo tenantInfo;
  private Driver storage;

  protected static final String BUCKET_NAME = "legal-service-configuration";
  private static final String FILE_NAME = "Legal_COO.json";
  private boolean isFullBucketName = false;

  public StorageReaderImpl(TenantInfo tenantInfo, String projectRegion, Driver storage) {
    new StorageReaderImpl(tenantInfo, projectRegion, storage, false);
  }

  public StorageReaderImpl(TenantInfo tenantInfo, String projectRegion, Driver storage,
      boolean isFullBucketName) {
    this.tenantInfo = tenantInfo;
    this.isFullBucketName = isFullBucketName;
    this.storage = storage;
  }

  @Override
  public byte[] readAllBytes() {
    byte[] content = null;
    try {
      if (storage.getBucket(getTenantBucketName(), getDestination()) == null) {
        storage.createBucket(getTenantBucketName(), getDestination());
        Blob emptyBlob = Blob.builder()
            .bucket(getTenantBucketName())
            .name(FILE_NAME)
            .contentType(MediaType.APPLICATION_JSON.toString())
            .build();
        storage.createBlob(emptyBlob, new byte[0], getDestination());
        content = storage.getBlobContent(getTenantBucketName(), FILE_NAME, getDestination());
        return content;
      } else {
        Blob blob = storage.getBlob(getTenantBucketName(), FILE_NAME, getDestination());
        if (Objects.nonNull(blob)) {
          content = storage.getBlobContent(getTenantBucketName(), FILE_NAME, getDestination());
        }
      }
    } catch (ObmDriverRuntimeException e) {
      log.error(e.getMessage(), e);
      throw e;
    }
    return content;
  }

  protected String getTenantBucketName() {
    if (Objects.nonNull(isFullBucketName) && isFullBucketName) {
      return this.tenantInfo.getProjectId() + "-" + this.tenantInfo.getName() + "-" + BUCKET_NAME;
    }
    return this.tenantInfo.getName() + "-" + BUCKET_NAME;
  }

  private ObmDestination getDestination() {
    return getDestination(tenantInfo.getDataPartitionId());
  }

  private ObmDestination getDestination(String dataPartitionId) {
    return ObmDestination.builder().partitionId(dataPartitionId).build();
  }
}
