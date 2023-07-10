package org.opengroup.osdu.legal.controller;

import org.junit.Assert;
import org.mockito.Mockito;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTag;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;

import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.legal.controller.LegalTagStatusJobController;
import org.opengroup.osdu.legal.jobs.LegalTagCompliance;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.opengroup.osdu.legal.jobs.LegalTagStatusJob;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.legal.logging.AuditLogger;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class LegalTagStatusJobControllerTest {

    public static final DpsHeaders dpsHeaders = new DpsHeaders();

    @Mock
    private RequestInfo requestInfo;

    @Mock
    private LegalTagStatusJob legalTagStatusJob;

    @Mock
    private AuditLogger auditLogger;

    @Mock
    private JaxRsDpsLog log;

    @Mock
    private ITenantFactory tenantStorageFactory;

    @InjectMocks
    private LegalTagStatusJobController sut;

    @Before
    public void setup() {

        dpsHeaders.put("data-partition-id", "common");
        Mockito.when(requestInfo.getHeaders()).thenReturn(dpsHeaders);
        TenantInfo tenantInfo = new TenantInfo();
        tenantInfo.setName("tenantName");
        tenantInfo.setProjectId("projectId");
        Mockito.when(tenantStorageFactory.listTenantInfo()).thenReturn(Collections.singletonList(tenantInfo));
    }

    @Test
    public void shouldReturn200WhenCheckUpdateStatusSucceeds() throws Exception {
        StatusChangedTag statusChangedTag = new StatusChangedTag("testTag", LegalTagCompliance.incompliant);
        StatusChangedTags statusChangedTags = new StatusChangedTags();
        statusChangedTags.getStatusChangedTags().add(statusChangedTag);
        Mockito.when(legalTagStatusJob.run("projectId", dpsHeaders, "tenantName")).thenReturn(statusChangedTags);

        ResponseEntity<HttpStatus> result = sut.checkLegalTagStatusChanges();

        Assert.assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        Mockito.verify(auditLogger).legalTagJobRanSuccess(Collections.singletonList(statusChangedTags.toString()));
    }

    @Test
    public void shouldReturn500WhenCheckUpdateStatusThrowsAnError() throws Exception {
        Exception exception = new Exception("error occurred");
        Mockito.when(legalTagStatusJob.run("projectId", dpsHeaders, "tenantName")).thenThrow(exception);

        ResponseEntity<HttpStatus> result = sut.checkLegalTagStatusChanges();

        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        Mockito.verify(log).error("Error running check LegalTag compliance job on tenant common", exception);
    }
}
