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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opengroup.osdu.legal.countries.StorageReaderImpl.BUCKET_NAME;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.partition.PartitionPropertyResolver;
import org.opengroup.osdu.core.obm.core.Driver;
import org.opengroup.osdu.core.obm.core.model.Blob;
import org.opengroup.osdu.core.obm.core.model.Bucket;
import org.opengroup.osdu.core.obm.core.persistence.ObmDestination;
import org.opengroup.osdu.legal.config.PartitionPropertyNames;

@RunWith(MockitoJUnitRunner.class)
public class StorageReaderImplTests {

  private static final String TENANT_1 = "tenant1";
  private static final String FILE_NAME = "Legal_COO.json";
  private static final String BUCKET_FULL_NAME = "tenant1-legal-service-configuration";

  @Mock
  private TenantInfo tenantInfo;

  @Mock
  private Driver storage;

  @Mock
  private PartitionPropertyNames partitionPropertyNames;

  @Mock
  private PartitionPropertyResolver partitionPropertyResolver;

  @InjectMocks
  private StorageReaderImpl sut;

  private String bucketName;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    bucketName = "legal-service-configuration";
  }

  @Test
  public void should_createBucketAndObject_when_bucketDoesNotExist() {
    when(tenantInfo.getName()).thenReturn(TENANT_1);
    when(tenantInfo.getDataPartitionId()).thenReturn(TENANT_1);
    when(storage.getBlobContent(BUCKET_FULL_NAME, FILE_NAME, getDestination())).thenReturn(
        new byte[0]);

    byte[] bytes = sut.readAllBytes();
    assertNotNull(bytes);
  }

  @Test
  public void should_returnAllBytes_when_bucketExistsAndFileExist() {
    when(tenantInfo.getName()).thenReturn(TENANT_1);
    when(tenantInfo.getDataPartitionId()).thenReturn(TENANT_1);
    when(storage.getBucket(BUCKET_FULL_NAME, getDestination())).thenReturn(
        new Bucket(TENANT_1));
    when(storage.getBlob(BUCKET_FULL_NAME, FILE_NAME,
        getDestination())).thenReturn(
        Blob.builder().build());
    byte[] expectedBytes = "test".getBytes();
    when(storage.getBlobContent(BUCKET_FULL_NAME, FILE_NAME, getDestination()))
        .thenReturn(expectedBytes);

    byte[] bytes = sut.readAllBytes();
    assertEquals(expectedBytes, bytes);
  }

  @Test
  public void should_returnFullBucketName_when_IsFullBucketName_is_true() {
    when(tenantInfo.getName()).thenReturn(TENANT_1);
    when(tenantInfo.getProjectId()).thenReturn("projectId1");
    String bucketName = tenantInfo.getProjectId() + "-" + tenantInfo.getName() + "-" + BUCKET_NAME;
    StorageReaderImpl storageReader =
        new StorageReaderImpl(
            tenantInfo, null, storage, true, partitionPropertyResolver, partitionPropertyNames);
    String resultBucketName = storageReader.getTenantBucketName();
    assertEquals(bucketName, resultBucketName);
  }

  @Test
  public void should_returnBucketName_when_IsFullBucketName_is_false() {
    when(tenantInfo.getName()).thenReturn(TENANT_1);
    String bucketName = tenantInfo.getName() + "-" + BUCKET_NAME;
    StorageReaderImpl storageReader =
        new StorageReaderImpl(
            tenantInfo, null, storage, false, partitionPropertyResolver, partitionPropertyNames);
    String resultBucketName = storageReader.getTenantBucketName();
    assertEquals(bucketName, resultBucketName);
  }

  @Test
  @Ignore
  public void should_returnBucketName_when_IsFullBucketName_is_null() {
    when(tenantInfo.getName()).thenReturn(TENANT_1);
    when(tenantInfo.getProjectId()).thenReturn("projectId1");
    String bucketName = tenantInfo.getName() + "-" + BUCKET_NAME;
    TenantInfo tenantInfo1 = new TenantInfo();
    StorageReaderImpl storageReader =
        new StorageReaderImpl(
            tenantInfo1, null, null, false, partitionPropertyResolver, partitionPropertyNames);

    String resultBucketName = storageReader.getTenantBucketName();
    assertEquals(bucketName, resultBucketName);
  }

  @Test
  public void should_returnBucketName_fromPartition() {
    when(partitionPropertyNames.getLegalBucketName()).thenReturn("partition-bucket-name");
    when(partitionPropertyResolver.getOptionalPropertyValue(
            partitionPropertyNames.getLegalBucketName(), tenantInfo.getDataPartitionId()))
        .thenReturn(Optional.of("partition-bucket-name"));
    TenantInfo tenantInfo1 = new TenantInfo();
    StorageReaderImpl storageReader =
        new StorageReaderImpl(
            tenantInfo1, null, null, false, partitionPropertyResolver, partitionPropertyNames);

    String resultBucketName = storageReader.getTenantBucketName();
    assertEquals("partition-bucket-name", resultBucketName);
  }

  private ObmDestination getDestination() {
    return ObmDestination.builder().partitionId(TENANT_1).build();
  }
}