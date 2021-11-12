//  Copyright © Microsoft Corporation
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.opengroup.osdu.legal.azure.jobs;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.azure.eventgrid.models.EventGridEvent;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.MessageBody;
import com.microsoft.azure.servicebus.TopicClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.azure.eventgrid.EventGridTopicStore;
import org.opengroup.osdu.azure.servicebus.ITopicClientFactory;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.legal.azure.di.EventGridConfig;

import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class LegalTagPublisherImplTest {

    private static final String CORRELATION_ID = "correlation-id";
    private static final String USER_EMAIL = "user@email.com";
    private static final String PARTITION_ID = "partition-id";

    @Mock
    private JaxRsDpsLog logger;

    @Mock
    private ITopicClientFactory topicClientFactory;

    @Mock
    private EventGridTopicStore eventGridTopicStore;

    @Mock
    private EventGridConfig eventGridConfig;

    @Mock
    private TopicClient topicClient;

    @Mock
    private DpsHeaders headers;

    @InjectMocks
    private LegalTagPublisherImpl sut;

    @Before
    public void init() throws ServiceBusException, InterruptedException {
        Mockito.doReturn(CORRELATION_ID).when(headers).getCorrelationId();
        Mockito.doReturn(USER_EMAIL).when(headers).getUserEmail();
        Mockito.doReturn(PARTITION_ID).when(headers).getPartitionId();
        Mockito.doReturn(topicClient).when(topicClientFactory).getClient(Mockito.eq(PARTITION_ID), Mockito.any());
    }

    @Test
    public void shouldPublishToEventGridWhenFlagIsSet() throws Exception {
        StatusChangedTags tags = new StatusChangedTags();

        ArgumentCaptor<String> partitionNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> topicNameArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<EventGridEvent>> listEventGridEventArgumentCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.doNothing().when(this.eventGridTopicStore).publishToEventGridTopic(
                partitionNameCaptor.capture(), topicNameArgumentCaptor.capture(), listEventGridEventArgumentCaptor.capture()
        );
        Mockito.when(this.eventGridConfig.isPublishingToEventGridEnabled()).thenReturn(true);
        Mockito.when(this.eventGridConfig.getTopicName()).thenReturn("legaltagschangedtopic");

        sut.publish("project-id", headers, tags);

        Mockito.verify(this.eventGridTopicStore, Mockito.times(1))
                .publishToEventGridTopic(Mockito.any(), Mockito.any(), Mockito.anyList());

        Assert.assertEquals(1, listEventGridEventArgumentCaptor.getValue().size());
        Assert.assertEquals(topicNameArgumentCaptor.getValue(), "legaltagschangedtopic");
        Assert.assertEquals(partitionNameCaptor.getValue(), PARTITION_ID);
    }

    @Test
    public void shouldPublishLegalTag() throws Exception {
        StatusChangedTags tags = new StatusChangedTags();
        sut.publish("project-id", headers, tags);
        ArgumentCaptor<Message> msg = ArgumentCaptor.forClass(Message.class);
        ArgumentCaptor<String> log = ArgumentCaptor.forClass(String.class);

        Mockito.verify(logger).debug(log.capture());
        Assert.assertEquals("Storage publishes message " + CORRELATION_ID, log.getValue());

        Mockito.verify(topicClient).send(msg.capture());
        Map<String, Object> properties = msg.getValue().getProperties();

        Assert.assertEquals(PARTITION_ID, properties.get(DpsHeaders.DATA_PARTITION_ID));
        Assert.assertEquals(CORRELATION_ID, properties.get(DpsHeaders.CORRELATION_ID));
        Assert.assertEquals(USER_EMAIL, properties.get(DpsHeaders.USER_EMAIL));

        MessageBody messageBody = msg.getValue().getMessageBody();
        Gson gson = new Gson();
        String messageKey = "message";
        String dataKey = "data";
        JsonObject jsonObjectMessage = gson.fromJson(new String(messageBody.getBinaryData().get(0)), JsonObject.class);
        JsonObject jsonObject = (JsonObject) jsonObjectMessage.get(messageKey);
        Assert.assertEquals(PARTITION_ID, jsonObject.get(DpsHeaders.DATA_PARTITION_ID).getAsString());
        Assert.assertEquals(CORRELATION_ID, jsonObject.get(DpsHeaders.CORRELATION_ID).getAsString());
        Assert.assertEquals(USER_EMAIL, jsonObject.get(DpsHeaders.USER_EMAIL).getAsString());
        Assert.assertEquals(gson.toJsonTree(tags), jsonObject.get(dataKey));
    }
}
