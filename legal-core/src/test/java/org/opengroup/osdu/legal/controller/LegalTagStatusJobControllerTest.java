package org.opengroup.osdu.legal.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTag;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.common.provider.interfaces.ITenantFactory;
import org.opengroup.osdu.legal.jobs.LegalTagCompliance;
import org.opengroup.osdu.legal.jobs.LegalTagStatusJob;
import org.opengroup.osdu.legal.logging.AuditLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.opengroup.osdu.legal.jobs.models.LegalTagJobResult;
import org.opengroup.osdu.legal.jobs.models.AboutToExpireLegalTags;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        when(requestInfo.getHeaders()).thenReturn(dpsHeaders);
        TenantInfo tenantInfo = new TenantInfo();
        tenantInfo.setName("tenantName");
        tenantInfo.setProjectId("projectId");
        tenantInfo.setDataPartitionId("common");
        when(tenantStorageFactory.listTenantInfo()).thenReturn(Collections.singletonList(tenantInfo));
    }

    @Test
    public void shouldReturn204_whenCheckUpdateStatus_succeeds() throws Exception {
        StatusChangedTag statusChangedTag = new StatusChangedTag("testTag", LegalTagCompliance.incompliant);
        StatusChangedTags statusChangedTags = new StatusChangedTags();
        statusChangedTags.getStatusChangedTags().add(statusChangedTag);
        LegalTagJobResult legalTagJobResult = new LegalTagJobResult(statusChangedTags, new AboutToExpireLegalTags());
        when(legalTagStatusJob.run("projectId", dpsHeaders, "tenantName")).thenReturn(legalTagJobResult);

        ResponseEntity<HttpStatus> result = sut.checkLegalTagStatusChanges();

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(auditLogger).legalTagJobRanSuccess(Collections.singletonList(statusChangedTags.toString()));
    }

    @Test
    public void shouldReturn500_whenCheckUpdateStatus_throwsAnError() throws Exception {
        Exception exception = new Exception("error occurred");
        when(legalTagStatusJob.run("projectId", dpsHeaders, "tenantName")).thenThrow(exception);

        ResponseEntity<HttpStatus> result = sut.checkLegalTagStatusChanges();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        verify(log).error("Error running check LegalTag compliance job on tenant common", exception);
    }

    @Test
    public void shouldReturn500_whenCheckUpdateStatus_ThrowsAnErrorForFirstJobAndSucceedsForNextJob() throws Exception {
        TenantInfo tenantInfo1 = new TenantInfo();
        tenantInfo1.setName("tenantName1");
        tenantInfo1.setProjectId("projectId1");
        tenantInfo1.setDataPartitionId("common");
        TenantInfo tenantInfo2 = new TenantInfo();
        tenantInfo2.setName("tenantName2");
        tenantInfo2.setProjectId("projectId2");
        tenantInfo2.setDataPartitionId("common");
        StatusChangedTag statusChangedTag = new StatusChangedTag("testTag", LegalTagCompliance.incompliant);
        StatusChangedTags statusChangedTags = new StatusChangedTags();
        statusChangedTags.getStatusChangedTags().add(statusChangedTag);
        LegalTagJobResult legalTagJobResult = new LegalTagJobResult(statusChangedTags, new AboutToExpireLegalTags());

        when(tenantStorageFactory.listTenantInfo()).thenReturn(List.of(tenantInfo1, tenantInfo2));
        Exception exception = new Exception("error occurred");
        when(legalTagStatusJob.run("projectId1", dpsHeaders, "tenantName1")).thenThrow(exception);
        when(legalTagStatusJob.run("projectId2", dpsHeaders, "tenantName2")).thenReturn(legalTagJobResult);


        ResponseEntity<HttpStatus> result = sut.checkLegalTagStatusChanges();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        verify(log).error("Error running check LegalTag compliance job on tenant common", exception);
    }
}
