package org.opengroup.osdu.legal.tags;

import com.google.cloud.datastore.Datastore;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.gcp.multitenancy.IDatastoreFactory;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepositoryFactory;
import org.opengroup.osdu.legal.tags.dataaccess.DatastoreLegalTagRepository;
import org.opengroup.osdu.legal.tags.dataaccess.ResilientLegalTagRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class LegalTagRepositoryFactoryGcpImpl implements ILegalTagRepositoryFactory {

  private final Map<String, ILegalTagRepository> tenantRepositories = new HashMap<>();

  private TenantInfo tenantInfo;
  private IDatastoreFactory factory;
  private ITenantFactory tenantFactory;

  public LegalTagRepositoryFactoryGcpImpl(TenantInfo tenantInfo, IDatastoreFactory factory,
      ITenantFactory tenantFactory) {
    this.tenantInfo = tenantInfo;
    this.factory = factory;
    this.tenantFactory = tenantFactory;
  }

  @Override
  public ILegalTagRepository get(String tenantName) {
    if (StringUtils.isBlank(tenantName)) {
      throw invalidTenantGivenException(tenantName);
    }
    if (!tenantRepositories.containsKey(tenantName)) {
      addRepository(tenantName);
    }
    return tenantRepositories.get(tenantName);
  }

  private void addRepository(String tenantName) {
    TenantInfo tenantInfo = tenantFactory.getTenantInfo(tenantName);
    Datastore ds = factory.getDatastore(tenantInfo);
    if (Objects.isNull(ds)) {
      throw invalidTenantGivenException(tenantName);
    }
    ILegalTagRepository repo = new ResilientLegalTagRepository(new DatastoreLegalTagRepository(ds));
    tenantRepositories.put(tenantName, repo);
  }

  AppException invalidTenantGivenException(String tenantName) {
    return new AppException(403, "Forbidden",
        String.format("You do not have access to the %s value given %s",
            DpsHeaders.ACCOUNT_ID, tenantName));
  }
}
