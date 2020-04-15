package org.opengroup.osdu.legal.jobs;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.legal.logging.AuditLogger;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagPublisher;
import org.opengroup.osdu.legal.tags.LegalTagConstraintValidator;
import org.opengroup.osdu.legal.tags.LegalTagService;
import org.opengroup.osdu.core.common.model.legal.LegalTag;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LegalTagStatusJobTests {
    @Mock
    private LegalTagConstraintValidator validator;
    @Mock
    private LegalTagService legalTagServiceMock;
    @Mock
    private AuditLogger auditLogger;
    @Mock
    private ILegalTagPublisher messagePublisherMock;
    @Mock
    private JaxRsDpsLog log;

    @InjectMocks
    LegalTagStatusJob sut;

    private DpsHeaders headers = new DpsHeaders();

    @Before
    public void setup() {
        when(validator.getErrors(any())).thenReturn(null);
        headers.put(DpsHeaders.DATA_PARTITION_ID, "SIS-INTERNAL-HQ");
        headers.put(DpsHeaders.CORRELATION_ID, "12345-12345");
        headers.put(DpsHeaders.USER_EMAIL, "nonexistent@nonexisent.domain");
    }

    @Test
    public void should_returnNoResults_when_legalTagsComplianceStatusHasNotChanged() throws Exception {
        Collection<LegalTag> validLegalTags = new ArrayList<>();
        sut = createSut(validLegalTags);
        //when(validator.getErrors(any())).thenReturn("");

        StatusChangedTags result = sut.run("project1", headers, "tenant");
        assertEquals(0, result.getStatusChangedTags().size());
    }

    @Test
    public void should_returnValidLegalTagName_when_isNotValidTagBecomesCompliant() throws Exception {
        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTag validLegalTagWithTrueStatus = createValidLegalTagWithIsValidStatus("legaltag", false);
        validLegalTags.add(validLegalTagWithTrueStatus);

        sut = createSut(validLegalTags);

        StatusChangedTags result = sut.run("project1", headers, "tenant");
        assertEquals(1, result.getStatusChangedTags().size());
        assertEquals(LegalTagCompliance.compliant, result.getStatusChangedTags().get(0).getChangedTagStatus());
        verify(validator, times(1)).setHeaders(any());
    }

    @Test
    public void should_returnInvalidLegalTagName_when_isValidTagBecomesIncompliant() throws Exception {
        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTag validLegalTagWithTrueStatus = createValidLegalTagWithIsValidStatus("legaltag", true);
        validLegalTags.add(validLegalTagWithTrueStatus);

        sut = createSut(validLegalTags);
        when(validator.getErrors(any())).thenReturn("Not Valid");
        StatusChangedTags result = sut.run("project1", headers, "tenant");

        assertEquals(1, result.getStatusChangedTags().size());
        assertEquals(LegalTagCompliance.incompliant, result.getStatusChangedTags().get(0).getChangedTagStatus());
    }

    @Test
    public void should_sendLegalTagNameAndNewStatusWithCorrelationIdToPubSub_when_legalTagBecomesIncompliant() throws
            Exception {
        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTag validLegalTagWithTrueStatus = createValidLegalTagWithIsValidStatus("legaltag", true);
        validLegalTags.add(validLegalTagWithTrueStatus);

        sut = createSut(validLegalTags);
        when(validator.getErrors(any())).thenReturn("Not Valid");

        sut.run("project1", headers, "tenant");

        verify(messagePublisherMock, times(1)).publish(any(), any(), any());
    }

    @Test
    public void should_notSendMessageToPubSub_when_noLegalTagStatusHasChanged() throws Exception {
        Collection<LegalTag> validLegalTags = new ArrayList<>();

        sut = createSut(validLegalTags);
        //when(validator.getErrors(any())).thenReturn("");

        sut.run("project1", headers, "tenant");
        verify(messagePublisherMock, never()).publish(any(), any(), any());
    }

    @Test
    public void should_notAuditLogPublishMessage_when_tagsStatusHasNotChanged() throws Exception {
        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTagStatusJob legalTagStatusJob = createSut(validLegalTags);

        legalTagStatusJob.run("project1", headers, "tenant");

        verify(auditLogger, never()).publishedStatusChangeSuccess(any());
    }

    private static LegalTag createValidLegalTagWithIsValidStatus(String name, Boolean isValid) {
        LegalTag legalTag = new LegalTag();
        legalTag.setName(name);
        legalTag.setIsValid(isValid);
        return legalTag;
    }

    LegalTagStatusJob createSut(Collection<LegalTag> tags) throws Exception {
        when(legalTagServiceMock.listLegalTag(anyBoolean(), any())).thenReturn(tags);
        return sut;
    }
}
