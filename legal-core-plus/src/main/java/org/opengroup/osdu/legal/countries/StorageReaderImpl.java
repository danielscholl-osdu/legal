/*
 *  Copyright 2020-2023 Google LLC
 *  Copyright 2020-2023 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.legal.countries;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.partition.PartitionPropertyResolver;
import org.opengroup.osdu.core.obm.core.Driver;
import org.opengroup.osdu.core.obm.core.ObmDriverRuntimeException;
import org.opengroup.osdu.core.obm.core.model.Blob;
import org.opengroup.osdu.core.obm.core.persistence.ObmDestination;
import org.opengroup.osdu.legal.config.PartitionPropertyNames;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;

import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
public class StorageReaderImpl implements IStorageReader {

  private PartitionPropertyResolver partitionPropertyResolver;
  private PartitionPropertyNames partitionPropertyNames;

  private TenantInfo tenantInfo;
  private Driver storage;

  protected static final String BUCKET_NAME = "legal-config";
  private static final String FILE_NAME = "Legal_COO.json";
  private boolean isFullBucketName = false;

  public StorageReaderImpl(
      TenantInfo tenantInfo,
      Driver storage,
      PartitionPropertyResolver partitionPropertyResolver,
      PartitionPropertyNames partitionPropertyNames) {
    this.tenantInfo = tenantInfo;
    this.storage = storage;
    this.partitionPropertyResolver = partitionPropertyResolver;
    this.partitionPropertyNames = partitionPropertyNames;
  }

  @Override
  public byte[] readAllBytes() {
    byte[] content = null;
    try {
      String tenantBucketName = getTenantBucketName();
      ObmDestination destination = getDestination();

      if (Objects.isNull(storage.getBucket(getTenantBucketName(), getDestination()))) {
        log.warn("Bucket %s is not existing.".formatted(tenantBucketName));
        return new byte[0];
      }

      Blob blob = storage.getBlob(tenantBucketName, FILE_NAME, destination);
      if (Objects.isNull(blob)) {
        log.warn("File %s in bucket %s is not existing.".formatted(FILE_NAME, tenantBucketName));
      } else {
        content = storage.getBlobContent(getTenantBucketName(), FILE_NAME, getDestination());
      }
    } catch (ObmDriverRuntimeException e) {
      log.error(e.getMessage(), e);
    }
    return content == null ? new byte[0] : content;
  }

  protected String getTenantBucketName() {
    return partitionPropertyResolver
        .getOptionalPropertyValue(
            partitionPropertyNames.getLegalBucketName(), tenantInfo.getDataPartitionId())
        .orElseGet(
            () ->
                this.tenantInfo.getProjectId()
                    + "-"
                    + this.tenantInfo.getName()
                    + "-"
                    + BUCKET_NAME);
  }

  private ObmDestination getDestination() {
    return getDestination(tenantInfo.getDataPartitionId());
  }

  private ObmDestination getDestination(String dataPartitionId) {
    return ObmDestination.builder().partitionId(dataPartitionId).build();
  }
}
