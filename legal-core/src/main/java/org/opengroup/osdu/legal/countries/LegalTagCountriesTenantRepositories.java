package org.opengroup.osdu.legal.countries;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReader;
import org.opengroup.osdu.legal.provider.interfaces.IStorageReaderFactory;

import org.springframework.stereotype.Repository;

import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

@Repository
@Log
public class LegalTagCountriesTenantRepositories {

    @Inject
    private IStorageReaderFactory storageReaderFactory;

    private final Map<String, LegalTagCountriesRepository> countriesTenantRepositories = new HashMap<>();

    LegalTagCountriesRepository get(TenantInfo tenant, String projectRegion){
        String tenantName = tenant.getName();
        if(StringUtils.isBlank(tenantName))
            throw invalidTenantGivenException(tenantName);
        if(!countriesTenantRepositories.containsKey(tenantName)){
            addRepository(tenant, projectRegion);
        }
        return countriesTenantRepositories.get(tenantName);
    }

    private void addRepository(TenantInfo tenant, String projectRegion) {
        IStorageReader storageReader = storageReaderFactory.getReader(tenant, projectRegion);
        LegalTagCountriesRepository repo = new LegalTagCountriesRepositoryImpl(storageReader);
        countriesTenantRepositories.put(tenant.getName(), repo);
    }

    private AppException invalidTenantGivenException(String tenantName){
        log.warning(String.format("Requested tenantname does not exist in list of tenants %s", tenantName));
        return new AppException(403, "Forbidden", String.format("You do not have access to the %s, value given %s",
                DpsHeaders.DATA_PARTITION_ID, tenantName));
    }
}
