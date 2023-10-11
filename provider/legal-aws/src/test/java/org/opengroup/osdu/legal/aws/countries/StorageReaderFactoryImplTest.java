package org.opengroup.osdu.legal.aws.countries;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;

import static org.junit.jupiter.api.Assertions.assertSame;

class StorageReaderFactoryImplTest {

    @InjectMocks
    private StorageReaderFactoryImpl factory;

    @Mock
    private StorageReaderImpl storageReaderImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetReader() {
        TenantInfo mockTenantInfo = new TenantInfo();
        String mockProjectRegion = "mockRegion";
    
        
        IStorageReader result = factory.getReader(mockTenantInfo, mockProjectRegion);

        // Verify that the result from getReader is indeed the mocked storageReaderImpl
        assertSame(storageReaderImpl, result);
    }
}
