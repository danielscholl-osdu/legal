package org.opengroup.osdu.legal.controller;

import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.legal.api.LegalTagStatusJobApi;
import org.opengroup.osdu.legal.jobs.LegalTagStatusJob;
import org.opengroup.osdu.legal.logging.AuditLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.Collection;

import static java.util.Collections.singletonList;

@RestController
public class LegalTagStatusJobController implements LegalTagStatusJobApi {

    @Inject
    private RequestInfo requestInfo;

    @Inject
    private LegalTagStatusJob legalTagStatusJob;
    
    @Inject
    private ITenantFactory tenantStorageFactory;

    @Inject
    private AuditLogger auditLogger;

    @Inject
    private JaxRsDpsLog log;

    @Override
    public ResponseEntity<HttpStatus> checkLegalTagStatusChanges() {
        tenantStorageFactory.flushCache();
        DpsHeaders convertedHeaders = requestInfo.getHeaders();
        Collection<TenantInfo> tenantsInfo = tenantStorageFactory.listTenantInfo();

        boolean allPassed = true;
        for (TenantInfo tenantInfo : tenantsInfo) {
            boolean result = runJob(convertedHeaders, tenantInfo, legalTagStatusJob);
            if (allPassed) {
                allPassed = result;
            }
        }

        HttpStatus status = allPassed ? HttpStatus.NO_CONTENT : HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(status);
    }

    private boolean runJob(DpsHeaders convertedHeaders, TenantInfo tenantInfo, LegalTagStatusJob legalTagStatusJob) {
        boolean success = true;
        try {
            StatusChangedTags result = legalTagStatusJob.run(tenantInfo.getProjectId(), convertedHeaders, tenantInfo.getName());
            auditLogger.legalTagJobRanSuccess(singletonList(result.toString()));
        } catch (Exception e) {
            success = false;
            log.error( "Error running check LegalTag compliance job on tenant " + convertedHeaders.getPartitionIdWithFallbackToAccountId(), e);
        }
        return success;
    }
}
