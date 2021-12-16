package org.opengroup.osdu.legal.jobs;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.PubsubMessage;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.core.gcp.PubSub.PubSubExtensions;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.gcp.multitenancy.PublisherFactory;
import org.opengroup.osdu.legal.logging.AuditLogger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LegalTagPublisherImplTests {
    @Mock
    private PublisherFactory publisherFactory;
    @Mock
    private PubSubExtensions pubSubExtensions;
    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private LegalTagPublisherImpl sut;

    private DpsHeaders headers = new DpsHeaders();

    @Before
    public void setup() throws Exception {
        sut = createSut();

        headers.put(DpsHeaders.USER_EMAIL, "ash");
        headers.put(DpsHeaders.CORRELATION_ID, "123");
        headers.put(DpsHeaders.DATA_PARTITION_ID, "tenant1");
    }

    @Test
    public void should_publishExpectedMessageToPubSub_when_methodCalledWithValidHeadersAndStatusChangedTags() throws Exception {
        sut.publish("project1", headers, new StatusChangedTags());

        ArgumentCaptor<PubsubMessage> captor = ArgumentCaptor.forClass(PubsubMessage.class);
        verify(pubSubExtensions).publishAndCreateTopicIfNotExist(any(), captor.capture());
        PubsubMessage capturedMessage = captor.getValue();
        String data = capturedMessage.toString();

        assertEquals("data: \"{\\\"statusChangedTags\\\":[]}\"\n" +
                "attributes {\n" +
                "  key: \"data-partition-id\"\n" +
                "  value: \"tenant1\"\n" +
                "}\n" +
                "attributes {\n" +
                "  key: \"correlation-id\"\n" +
                "  value: \"123\"\n" +
                "}\n" +
                "attributes {\n" +
                "  key: \"user\"\n" +
                "  value: \"ash\"\n" +
                "}\n", data);
    }

    @Test
    public void should_auditLogPublishMessage_when_messageIsPublishedToPubSub() throws Exception {
        sut.publish("project1", headers, new StatusChangedTags());
        verify(auditLogger).publishedStatusChangeSuccess(any());
    }

    private LegalTagPublisherImpl createSut() throws Exception {
        Publisher publisher = mock(Publisher.class);
        when(publisherFactory.createPublisher(any(), any())).thenReturn(publisher);
        when(pubSubExtensions.publishAndCreateTopicIfNotExist(any(), any())).thenReturn("success");
        return sut;
    }
}
