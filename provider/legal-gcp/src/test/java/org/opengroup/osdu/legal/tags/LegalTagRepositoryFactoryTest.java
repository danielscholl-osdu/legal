package org.opengroup.osdu.legal.tags;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import com.google.cloud.datastore.Datastore;

import org.junit.Test;
import org.opengroup.osdu.core.gcp.multitenancy.DatastoreFactory;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepositoryFactory;

public class LegalTagRepositoryFactoryTest {

    private static final String TENANT_1 = "tenant1";

    @Test(expected = AppException.class)
    public void should_throwAppException_when_givenBlankName(){
        DatastoreFactory factory = mock(DatastoreFactory.class);
        ILegalTagRepositoryFactory sut = new LegalTagRepositoryFactoryGcpImpl(factory);
        sut.get("");
    }

    @Test(expected = AppException.class)
    public void should_throwAppException_when_tenantDoesNotExist(){
        DatastoreFactory factory = mock(DatastoreFactory.class);
        when(factory.getDatastore(TENANT_1, TENANT_1)).thenReturn(null);

        ILegalTagRepositoryFactory sut = new LegalTagRepositoryFactoryGcpImpl(factory);
        sut.get(TENANT_1);
    }

    @Test
    public void should_returnExistingRepo_when_requestingTenantThatHasPreviouslyBeenRequested(){
        Datastore ds = mock(Datastore.class);
        DatastoreFactory factory = mock(DatastoreFactory.class);
        when(factory.getDatastore(TENANT_1, TENANT_1)).thenReturn(ds);

        ILegalTagRepositoryFactory sut = new LegalTagRepositoryFactoryGcpImpl(factory);
        ILegalTagRepository result = sut.get(TENANT_1);
        assertNotNull(result);
        verify(factory, times(1)).getDatastore(TENANT_1, TENANT_1);

        result = sut.get(TENANT_1);
        assertNotNull(result);
        verify(factory, times(1)).getDatastore(TENANT_1, TENANT_1);

    }

}
