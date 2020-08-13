package org.opengroup.osdu.legal.tags;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import com.google.cloud.datastore.Datastore;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.gcp.multitenancy.DatastoreFactory;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepositoryFactory;

public class LegalTagRepositoryFactoryTest {

  private static final String TENANT_1 = "tenant1";
  private DatastoreFactory factory;
  private ITenantFactory tenantFactory;

  @Before
  public void init() {
    factory = mock(DatastoreFactory.class);
    tenantFactory = mock(ITenantFactory.class);
  }

  @Test(expected = AppException.class)
  public void should_throwAppException_when_givenBlankName() {
    TenantInfo tenantInfo = new TenantInfo();
    tenantInfo.setName(TENANT_1);
    when(factory.getDatastore(tenantInfo)).thenReturn(null);
    when(tenantFactory.getTenantInfo(TENANT_1)).thenReturn(null);
    ILegalTagRepositoryFactory sut = new LegalTagRepositoryFactoryGcpImpl(tenantInfo, factory,
        tenantFactory);
    sut.get("");
  }

  @Test(expected = AppException.class)
  public void should_throwAppException_when_tenantDoesNotExist() {
    TenantInfo tenantInfo = new TenantInfo();
    tenantInfo.setName(TENANT_1);
    when(factory.getDatastore(tenantInfo)).thenReturn(null);
    when(tenantFactory.getTenantInfo(TENANT_1)).thenReturn(null);

    ILegalTagRepositoryFactory sut = new LegalTagRepositoryFactoryGcpImpl(tenantInfo, factory,
        tenantFactory);
    sut.get(TENANT_1);
  }

  @Test
  public void should_returnExistingRepo_when_requestingTenantThatHasPreviouslyBeenRequested() {
    Datastore ds = mock(Datastore.class);
    DatastoreFactory factory = mock(DatastoreFactory.class);
    TenantInfo tenantInfo = new TenantInfo();
    tenantInfo.setName(TENANT_1);
    when(factory.getDatastore(tenantInfo)).thenReturn(ds);
    when(tenantFactory.getTenantInfo(TENANT_1)).thenReturn(tenantInfo);
    ILegalTagRepositoryFactory sut = new LegalTagRepositoryFactoryGcpImpl(tenantInfo, factory,
        tenantFactory);
    ILegalTagRepository result = sut.get(TENANT_1);
    assertNotNull(result);
    verify(factory, times(1)).getDatastore(tenantInfo);

    result = sut.get(TENANT_1);
    assertNotNull(result);
    verify(factory, times(1)).getDatastore(tenantInfo);

  }

}
