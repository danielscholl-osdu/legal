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

package org.opengroup.osdu.legal.aws.countries;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opengroup.osdu.core.aws.s3.IS3ClientFactory;
import org.opengroup.osdu.core.aws.s3.S3ClientWithBucket;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.opengroup.osdu.core.common.model.http.AppException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class StorageReaderImplTest {

    @InjectMocks
    private StorageReaderImpl storageReader;

    @Mock
    private DpsHeaders dpsHeaders;

    @Mock
    private IS3ClientFactory s3ClientFactory;

    @Mock
    private S3ClientWithBucket s3ClientWithBucket;

    @Mock
    private AmazonS3 s3Client;

    private final String bucketName = "testBucket";
    private final String partitionId = "testPartition";
    private final String legalConfigFileName = "testLegalConfigFileName";
    private final String content = "[...]";  // Dummy JSON for Country array

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(s3ClientWithBucket.getS3Client()).thenReturn(s3Client);
        when(s3ClientWithBucket.getBucketName()).thenReturn(bucketName);
        when(dpsHeaders.getPartitionIdWithFallbackToAccountId()).thenReturn(partitionId);
        when(dpsHeaders.getPartitionId()).thenReturn(partitionId);
        when(s3ClientFactory.getS3ClientForPartition(anyString(), any())).thenReturn(s3ClientWithBucket);
        ReflectionTestUtils.setField(storageReader, "legalConfigFileName", legalConfigFileName);
    }

    @Test
    void testGetConfigFile_HappyPath() {
        when(s3Client.getObjectAsString(bucketName, partitionId + "/" + legalConfigFileName)).thenReturn(content);
        String result = storageReader.getConfigFile();
        assertEquals(content, result);
    }

    @Test
    void testReadAllBytes(){
        when(s3Client.getObjectAsString(bucketName, partitionId + "/" + legalConfigFileName)).thenReturn(content);
        byte[] result = storageReader.readAllBytes();
        assertArrayEquals(content.getBytes(), result);
    }

    @Test
    void testGetConfigFile_WhenAmazonS3ExceptionWithStatus404() {
        AmazonS3Exception exception = mock(AmazonS3Exception.class);
        when(exception.getStatusCode()).thenReturn(404);
        when(s3Client.getObjectAsString(bucketName, partitionId + "/" + legalConfigFileName)).thenThrow(exception);
        String result = storageReader.getConfigFile();
        assertEquals("[]", result);
    }

    @Test
    void testGetConfigFile_WhenAmazonS3ExceptionWithStatusOtherThan404() {
        AmazonS3Exception exception = mock(AmazonS3Exception.class);
        when(exception.getStatusCode()).thenReturn(500);
        when(s3Client.getObjectAsString(bucketName, partitionId + "/" + legalConfigFileName)).thenThrow(exception);
        assertThrows(AppException.class, () -> storageReader.getConfigFile());
    }

    @Test
    void testGetConfigFile_WhenAmazonServiceExceptionThrown() {
        when(s3Client.getObjectAsString(bucketName, partitionId + "/" + legalConfigFileName)).thenThrow(AmazonServiceException.class);
        assertThrows(AppException.class, () -> storageReader.getConfigFile());
    }

    @Test
    void testGetConfigFile_WhenSdkClientExceptionThrown() {
        when(s3Client.getObjectAsString(bucketName, partitionId + "/" + legalConfigFileName)).thenThrow(SdkClientException.class);
        assertThrows(AppException.class, () -> storageReader.getConfigFile());
    }
}
