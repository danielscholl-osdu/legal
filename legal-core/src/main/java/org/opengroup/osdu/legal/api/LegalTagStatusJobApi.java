package org.opengroup.osdu.legal.api;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.legal.jobs.LegalTagStatusJob;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.legal.logging.AuditLogger;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.opengroup.osdu.core.common.model.legal.ServiceConfig;

import java.util.Collection;
import javax.inject.Inject;

import static java.util.Collections.singletonList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs")
public class LegalTagStatusJobApi {

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

    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_CRON + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @GetMapping("/updateLegalTagStatus")
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
