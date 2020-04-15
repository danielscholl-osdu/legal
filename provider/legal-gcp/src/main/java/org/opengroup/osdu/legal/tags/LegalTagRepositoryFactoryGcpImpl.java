package org.opengroup.osdu.legal.tags;

import java.util.HashMap;
import java.util.Map;

import com.google.cloud.datastore.Datastore;

import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.gcp.multitenancy.DatastoreFactory;
import org.opengroup.osdu.core.gcp.multitenancy.IDatastoreFactory;
import org.opengroup.osdu.core.gcp.multitenancy.TenantFactory;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepositoryFactory;
import org.opengroup.osdu.legal.tags.dataaccess.DatastoreLegalTagRepository;
import org.opengroup.osdu.legal.tags.dataaccess.ResilientLegalTagRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class LegalTagRepositoryFactoryGcpImpl implements ILegalTagRepositoryFactory {

    private final IDatastoreFactory factory;
    private final Map<String, ILegalTagRepository> tenantRepositories = new HashMap<>();

    public LegalTagRepositoryFactoryGcpImpl(){
        this(new DatastoreFactory(new TenantFactory()));
    }

    LegalTagRepositoryFactoryGcpImpl(IDatastoreFactory factory){
        this.factory = factory;
    }

    @Override
    public ILegalTagRepository get(String tenantName){
        if(StringUtils.isBlank(tenantName))
            throw invalidTenantGivenException(tenantName);
        if(!tenantRepositories.containsKey(tenantName)){
            addRepository(tenantName);
        }
        return tenantRepositories.get(tenantName);
    }

    private void addRepository(String tenantName) {
        Datastore ds = factory.getDatastore(tenantName, tenantName);
        if(ds == null)
            throw invalidTenantGivenException(tenantName);
        ILegalTagRepository repo = new ResilientLegalTagRepository(new DatastoreLegalTagRepository(ds));
        tenantRepositories.put(tenantName, repo);
    }

    AppException invalidTenantGivenException(String tenantName){
        return new AppException(403, "Forbidden", String.format("You do not have access to the %s value given %s",
                DpsHeaders.ACCOUNT_ID, tenantName));
    }
}
