/*
 * Copyright 2020-2023 Google LLC
 * Copyright 2020-2023 EPAM Systems, Inc
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.partition.PartitionPropertyResolver;
import org.opengroup.osdu.core.gcp.obm.driver.Driver;
import org.opengroup.osdu.core.gcp.obm.model.Blob;
import org.opengroup.osdu.core.gcp.obm.model.Bucket;
import org.opengroup.osdu.core.gcp.obm.persistence.ObmDestination;
import org.opengroup.osdu.legal.config.PartitionPropertyNames;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StorageReaderImplTests {

  private static final String TENANT_1 = "tenant1";
  private static final String FILE_NAME = "Legal_COO.json";
  private static final String BUCKET_FULL_NAME = "tenant1-tenant1-legal-config";

  @Mock private TenantInfo tenantInfo;

  @Mock private Driver storage;

  @Mock private PartitionPropertyNames partitionPropertyNames;

  @Mock private PartitionPropertyResolver partitionPropertyResolver;

  @InjectMocks private StorageReaderImpl sut;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void should_returnAllBytes_when_bucketExistsAndFileExist() {
    when(tenantInfo.getName()).thenReturn(TENANT_1);
    when(tenantInfo.getDataPartitionId()).thenReturn(TENANT_1);
    when(tenantInfo.getProjectId()).thenReturn(TENANT_1);
    when(storage.getBucket(BUCKET_FULL_NAME, getDestination())).thenReturn(new Bucket(TENANT_1));
    when(storage.getBlob(BUCKET_FULL_NAME, FILE_NAME, getDestination()))
        .thenReturn(Blob.builder().build());
    byte[] expectedBytes = "test".getBytes();
    when(storage.getBlobContent(BUCKET_FULL_NAME, FILE_NAME, getDestination()))
        .thenReturn(expectedBytes);

    byte[] bytes = sut.readAllBytes();
    assertEquals(expectedBytes, bytes);
  }

  @Test
  public void should_returnEmptyArray_when_bucketNotExists() {
    when(tenantInfo.getName()).thenReturn(TENANT_1);
    when(tenantInfo.getDataPartitionId()).thenReturn(TENANT_1);
    when(tenantInfo.getProjectId()).thenReturn(TENANT_1);

    byte[] bytes = sut.readAllBytes();
    assertTrue(bytes.length == 0);
  }

  @Test
  public void should_returnEmptyArray_when_FileBucketNull() {
    when(tenantInfo.getName()).thenReturn(TENANT_1);
    when(tenantInfo.getDataPartitionId()).thenReturn(TENANT_1);
    when(tenantInfo.getProjectId()).thenReturn(TENANT_1);
    when(storage.getBucket(BUCKET_FULL_NAME, getDestination())).thenReturn(new Bucket(TENANT_1));
    when(storage.getBlob(BUCKET_FULL_NAME, FILE_NAME, getDestination()))
        .thenReturn(Blob.builder().build());
    when(storage.getBlobContent(BUCKET_FULL_NAME, FILE_NAME, getDestination())).thenReturn(null);

    byte[] bytes = sut.readAllBytes();
    assertTrue(bytes.length == 0);
  }

  @Test
  public void should_returnBucketName_fromPartition() {
    when(partitionPropertyNames.getLegalBucketName()).thenReturn("partition-bucket-name");
    when(partitionPropertyResolver.getOptionalPropertyValue(
            partitionPropertyNames.getLegalBucketName(), tenantInfo.getDataPartitionId()))
        .thenReturn(Optional.of("partition-bucket-name"));
    StorageReaderImpl storageReader =
        new StorageReaderImpl(
            tenantInfo, storage, partitionPropertyResolver, partitionPropertyNames);

    String resultBucketName = storageReader.getTenantBucketName();
    assertEquals("partition-bucket-name", resultBucketName);
  }

  private ObmDestination getDestination() {
    return ObmDestination.builder().partitionId(TENANT_1).build();
  }
}
