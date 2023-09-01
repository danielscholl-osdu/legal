package org.opengroup.osdu.legal.aws.tags.dataaccess.mongodb.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LegalTagRepositoryFactoryMongoDBImplTest {

    @InjectMocks
    private LegalTagRepositoryFactoryMongoDBImpl factory;

    @Mock
    private LegalTagRepositoryMongoDBImpl repoImpl;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGet() {
        ILegalTagRepository result = factory.get("someTenantName");

        // Verifying that the get method returns the correct repoImpl
        assertEquals(repoImpl, result);
    }
}