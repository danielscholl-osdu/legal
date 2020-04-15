package org.opengroup.osdu.legal.countries;

import com.google.cloud.storage.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StorageReaderImplTests {
    @Mock
    private Storage storage;

    @Mock
    private TenantInfo tenantInfo;

    @Mock
    private Bucket bucket;

    @InjectMocks
    private StorageReaderImpl sut;

    private BlobId blobId;
    private String bucketName;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        bucketName = "legal-service-configuration";
        String file = "Legal_COO.json";
        blobId = BlobId.of(bucketName, file);
    }

    @Test
    public void should_createBucketAndObject_when_bucketDoesNotExist() {
        try {
            sut.readAllBytes();
        } catch (StorageException e) {
            verify(storage, times(1)).create(BucketInfo.newBuilder(bucketName)
                    .setStorageClass(StorageClass.MULTI_REGIONAL)
                    .setLocation("us")
                    .build());
            verify(storage, times(1)).create(BlobInfo.newBuilder(blobId).setContentType("application/json").build(), "".getBytes(UTF_8));
        }
    }

    @Test
    public void should_createObject_when_bucketExistsAndFileDoesNotExist() {
        try {
            sut.readAllBytes();
        } catch (StorageException e) {
            verify(storage, times(0)).create(BucketInfo.newBuilder("tenant1-coo-config-test")
                    .setStorageClass(StorageClass.MULTI_REGIONAL)
                    .build());
            verify(storage, times(1)).create(BlobInfo.newBuilder(blobId).setContentType("application/json").build(), "".getBytes(UTF_8));
        }
    }

    @Test
    public void should_returnAllBytes_when_bucketExistsAndFileExist() {
        when(tenantInfo.getName()).thenReturn("tenant1");
        byte[] expectedBytes = "test".getBytes();
        when(storage.readAllBytes(any())).thenReturn(expectedBytes);

        byte[] bytes = sut.readAllBytes();
        assertEquals(expectedBytes, bytes);
    }
}