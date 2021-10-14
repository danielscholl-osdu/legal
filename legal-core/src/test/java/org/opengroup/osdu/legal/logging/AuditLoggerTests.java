package org.opengroup.osdu.legal.logging;

import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditLoggerTests {
    @Mock
    private JaxRsDpsLog log;
    @Mock
    private RequestInfo requestInfo;

    @InjectMocks
    private AuditLogger sut;

    @Before
    public void Setup(){
        when(requestInfo.getUser()).thenReturn("ash");
    }

    @Test
    public void should_writeLegaltagCreatedEventSuccess(){
        List<String> resource = Collections.singletonList("1");
        sut.createdLegalTagSuccess(resource);
        verify(log).audit(any());
    }

    @Test
    public void should_writeLegaltagDeleteSuccessEvent(){
        List<String> resource = Collections.singletonList("1");
        sut.deletedLegalTagSuccess(resource);
        verify(log).audit(any());
    }

    @Test
    public void should_writeLegaltagJobRunSuccessEvent(){
        List<String> resource = Collections.singletonList("1");
        sut.legalTagJobRanSuccess(resource);
        verify(log).audit(any());
    }

    @Test
    public void should_writeLegalTagJobRunFailEvent(){
        List<String> resource = Collections.singletonList("1");
         sut.legalTagJobRanFail(resource);
         verify(log).audit(any());
    }

    @Test
    public void should_writeLegaltagPublishedStatusSuccessEvent(){
        List<String> resource = Collections.singletonList("1");
        sut.publishedStatusChangeSuccess(resource);
        verify(log).audit(any());
    }

    @Test
    public void should_writeLegalTagReadPropertiesSuccessEvent(){
        List<String> resource = Collections.singletonList("1");
        sut.readLegalPropertiesSuccess(resource);
        verify(log).audit(any());
    }

    @Test
    public void should_writeLegalTagReadPropertiesFailEvent(){
        List<String> resource = Collections.singletonList("1");
        sut.readLegalPropertiesFail(resource);
        verify(log).audit(any());
    }

    @Test
    public void should_writeLegalTagUpdateSuccessEvent(){
        List<String> resource = Collections.singletonList("1");
        sut.updatedLegalTagSuccess(resource);
        verify(log).audit(any());
    }

    @Test
    public void should_writeLegalTagUpdateFailEvent(){
        List<String> resource = Collections.singletonList("1");
        sut.updatedLegalTagFail(resource);
        verify(log).audit(any());
    }

    @Test
    public void should_writeLegalTagReadSuccessEvent(){
        List<String> resource = Collections.singletonList("1");
        sut.readLegalTagSuccess(resource);
        verify(log).audit(any());
    }

    @Test
    public void should_writeLegalTagReadFailEvent(){
        List<String> resource = Collections.singletonList("1");
        sut.readLegalTagFail(resource);
        verify(log).audit(any());
    }

    @Test
    public void should_writeLegalTagValidateSuccessEvent(){
        sut.validateLegalTagSuccess();
        verify(log).audit(any());
    }

    @Test
    public void should_writeLegalTagValidateFailEvent(){
        sut.validateLegalTagFail();
        verify(log).audit(any());
    }
}
