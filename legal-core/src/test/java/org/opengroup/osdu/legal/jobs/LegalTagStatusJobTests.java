package org.opengroup.osdu.legal.jobs;

import org.junit.Ignore;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.legal.logging.AuditLogger;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagPublisher;
import org.opengroup.osdu.legal.provider.interfaces.IAboutToExpireLegalTagPublisher;
import org.opengroup.osdu.legal.tags.LegalTagConstraintValidator;
import org.opengroup.osdu.legal.tags.LegalTagService;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.legal.jobs.models.LegalTagJobResult;
import org.opengroup.osdu.legal.jobs.models.AboutToExpireLegalTags;
import org.opengroup.osdu.core.common.feature.IFeatureFlag;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;
import static junit.framework.TestCase.fail;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(classes = {IFeatureFlag.class})
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
    private IAboutToExpireLegalTagPublisher aboutToExpireLegalTagPublisherMock;
    @Mock
    private JaxRsDpsLog log;
    @Mock
    private IFeatureFlag aboutToExpireLegalTagFeatureFlag;

    @InjectMocks
    LegalTagStatusJob sut;

    private DpsHeaders headers = new DpsHeaders();

    @Before
    public void setup() {
        when(validator.getErrors(any())).thenReturn(null);
        headers.put(DpsHeaders.DATA_PARTITION_ID, "SIS-INTERNAL-HQ");
        headers.put(DpsHeaders.CORRELATION_ID, "12345-12345");
        headers.put(DpsHeaders.USER_EMAIL, "nonexistent@nonexisent.domain");
        // aboutToExpireFeatureFlag
        when(aboutToExpireLegalTagFeatureFlag.isFeatureEnabled(any())).thenReturn(true);
        ReflectionTestUtils.setField(sut, "legalTagExpiration", "1d");
    }

    @Test
    public void should_returnNoResults_when_legalTagsComplianceStatusHasNotChanged() throws Exception {
        Collection<LegalTag> validLegalTags = new ArrayList<>();
        sut = createSut(validLegalTags);
        //when(validator.getErrors(any())).thenReturn("");

        LegalTagJobResult result = sut.run("project1", headers, "tenant");
        assertEquals(0, result.statusChangedTags.getStatusChangedTags().size());
    }

    @Test
    public void should_returnValidLegalTagName_when_isNotValidTagBecomesCompliant() throws Exception {
        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTag validLegalTagWithTrueStatus = createValidLegalTagWithIsValidStatus("legaltag", false, 0);
        validLegalTags.add(validLegalTagWithTrueStatus);

        sut = createSut(validLegalTags);

        LegalTagJobResult result = sut.run("project1", headers, "tenant");
        assertEquals(1, result.statusChangedTags.getStatusChangedTags().size());
        assertEquals(LegalTagCompliance.compliant, result.statusChangedTags.getStatusChangedTags().get(0).getChangedTagStatus());
        verify(validator, times(1)).setHeaders(any());
    }

    @Test
    public void should_returnInvalidLegalTagName_when_isValidTagBecomesIncompliant() throws Exception {
        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTag validLegalTagWithTrueStatus = createValidLegalTagWithIsValidStatus("legaltag", true, 0);
        validLegalTags.add(validLegalTagWithTrueStatus);

        sut = createSut(validLegalTags);
        when(validator.getErrors(any())).thenReturn("Not Valid");
        LegalTagJobResult result = sut.run("project1", headers, "tenant");

        assertEquals(1, result.statusChangedTags.getStatusChangedTags().size());
        assertEquals(LegalTagCompliance.incompliant, result.statusChangedTags.getStatusChangedTags().get(0).getChangedTagStatus());
    }

    @Test
    public void should_sendLegalTagNameAndNewStatusWithCorrelationIdToPubSub_when_legalTagBecomesIncompliant() throws
            Exception {
        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTag validLegalTagWithTrueStatus = createValidLegalTagWithIsValidStatus("legaltag", true, 0);
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

    @Test
    public void should_returnAboutToExpireLegalTagName_when_legalTagAboutToExpireIn3Days() throws Exception {
        ReflectionTestUtils.setField(sut, "legalTagExpiration", "3d");

        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTag aboutToExpireLegalTag1 = createValidLegalTagWithIsValidStatus("aboutToExpireLegalTag1", true, 1);
        validLegalTags.add(aboutToExpireLegalTag1);
        LegalTag aboutToExpireLegalTag2 = createValidLegalTagWithIsValidStatus("aboutToExpireLegalTag2", true, 2);
        validLegalTags.add(aboutToExpireLegalTag2);
        LegalTag longTermLegalTag = createValidLegalTagWithIsValidStatus("longTermLegalTag", true, 10);
        validLegalTags.add(longTermLegalTag);

        sut = createSut(validLegalTags);
        LegalTagJobResult result = sut.run("project1", headers, "tenant");

        assertEquals(2, result.aboutToExpireLegalTags.getAboutToExpireLegalTags().size());
        assertEquals("aboutToExpireLegalTag1", result.aboutToExpireLegalTags.getAboutToExpireLegalTags().get(0));
        assertEquals("aboutToExpireLegalTag2", result.aboutToExpireLegalTags.getAboutToExpireLegalTags().get(1));
    }

    @Test
    public void should_returnAboutToExpireLegalTagName_when_legalTagAboutToExpireIn2Weeks() throws Exception {
        ReflectionTestUtils.setField(sut, "legalTagExpiration", "2w");

        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTag aboutToExpireLegalTag1 = createValidLegalTagWithIsValidStatus("aboutToExpireLegalTag1", true, 12);
        validLegalTags.add(aboutToExpireLegalTag1);
        LegalTag aboutToExpireLegalTag2 = createValidLegalTagWithIsValidStatus("aboutToExpireLegalTag2", true, 13);
        validLegalTags.add(aboutToExpireLegalTag2);
        LegalTag longTermLegalTag = createValidLegalTagWithIsValidStatus("longTermLegalTag", true, 20);
        validLegalTags.add(longTermLegalTag);

        sut = createSut(validLegalTags);
        LegalTagJobResult result = sut.run("project1", headers, "tenant");

        assertEquals(2, result.aboutToExpireLegalTags.getAboutToExpireLegalTags().size());
        assertEquals("aboutToExpireLegalTag1", result.aboutToExpireLegalTags.getAboutToExpireLegalTags().get(0));
        assertEquals("aboutToExpireLegalTag2", result.aboutToExpireLegalTags.getAboutToExpireLegalTags().get(1));
    }

    @Test
    public void should_returnAboutToExpireLegalTagName_when_legalTagAboutToExpireIn3Months() throws Exception {
        ReflectionTestUtils.setField(sut, "legalTagExpiration", "3m");

        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTag aboutToExpireLegalTag = createValidLegalTagWithIsValidStatus("aboutToExpireLegalTag", true, 80);
        validLegalTags.add(aboutToExpireLegalTag);
        LegalTag longTermLegalTag = createValidLegalTagWithIsValidStatus("longTermLegalTag", true, 100);
        validLegalTags.add(longTermLegalTag);

        sut = createSut(validLegalTags);
        LegalTagJobResult result = sut.run("project1", headers, "tenant");

        assertEquals(1, result.aboutToExpireLegalTags.getAboutToExpireLegalTags().size());
        assertEquals("aboutToExpireLegalTag", result.aboutToExpireLegalTags.getAboutToExpireLegalTags().get(0));
    }

    @Test
    public void should_returnAboutToExpireLegalTagName_when_legalTagAboutToExpireIn1Year() throws Exception {
        ReflectionTestUtils.setField(sut, "legalTagExpiration", "1y");

        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTag aboutToExpireLegalTag = createValidLegalTagWithIsValidStatus("aboutToExpireLegalTag", true, 354);
        validLegalTags.add(aboutToExpireLegalTag);
        LegalTag longTermLegalTag = createValidLegalTagWithIsValidStatus("longTermLegalTag", true, 367);
        validLegalTags.add(longTermLegalTag);

        sut = createSut(validLegalTags);
        LegalTagJobResult result = sut.run("project1", headers, "tenant");

        assertEquals(1, result.aboutToExpireLegalTags.getAboutToExpireLegalTags().size());
        assertEquals("aboutToExpireLegalTag", result.aboutToExpireLegalTags.getAboutToExpireLegalTags().get(0));
    }

    @Test
    public void should_returnAboutToExpireLegalTagName_when_invalidTimeToExpire() throws Exception {
        ReflectionTestUtils.setField(sut, "legalTagExpiration", "asd");

        Collection<LegalTag> validLegalTags = new ArrayList<>();
        sut = createSut(validLegalTags);

        try {
            sut.run("project1", headers, "tenant");
        } catch (Exception e) {
            fail(String.format("Should not throw this exception: '%s'", e.getMessage()));
        }
    }

    @Test
    public void should_sendLegalTagNameToPubSub_when_legalTagAboutToExpire() throws Exception {
        ReflectionTestUtils.setField(sut, "legalTagExpiration", "2w");

        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTag aboutToExpireLegalTag = createValidLegalTagWithIsValidStatus("aboutToExpireLegalTag1", true, 13);
        validLegalTags.add(aboutToExpireLegalTag);

        sut = createSut(validLegalTags);

        sut.run("project1", headers, "tenant");

        verify(aboutToExpireLegalTagPublisherMock, times(1)).publish(any(), any(), any());
    }

    @Test
    public void should_notSendLegalTagNameToPubSub_when_legalTagNotAboutToExpire() throws Exception {
        ReflectionTestUtils.setField(sut, "legalTagExpiration", "2w");

        Collection<LegalTag> validLegalTags = new ArrayList<>();
        LegalTag longTermLegalTag = createValidLegalTagWithIsValidStatus("aboutToExpireLegalTag1", true, 20);
        validLegalTags.add(longTermLegalTag);

        sut = createSut(validLegalTags);

        sut.run("project1", headers, "tenant");

        verify(aboutToExpireLegalTagPublisherMock, times(0)).publish(any(), any(), any());
    }

    private static LegalTag createValidLegalTagWithIsValidStatus(String name, Boolean isValid, int daysToExpire) {
        LegalTag legalTag = new LegalTag();
        legalTag.setName(name);
        legalTag.setIsValid(isValid);
        if (daysToExpire > 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new java.util.Date(System.currentTimeMillis()));
            cal.add(Calendar.DAY_OF_YEAR, + daysToExpire);
            Properties properties = new Properties();
            properties.setExpirationDate(new java.sql.Date(cal.getTimeInMillis()));
            legalTag.setProperties(properties);
        }
        return legalTag;
    }

    LegalTagStatusJob createSut(Collection<LegalTag> tags) throws Exception {
        when(legalTagServiceMock.listLegalTag(anyBoolean(), any())).thenReturn(tags);
        return sut;
    }
}
