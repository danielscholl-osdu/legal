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
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.MessageBody;
import com.microsoft.azure.servicebus.TopicClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.azure.servicebus.ITopicClientFactory;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LegalTagPublisherImplTest {

    private static final String DATA_PARTITION_WITH_FALLBACK_ACCOUNT_ID = "data-partition-account-id";
    private static final String CORRELATION_ID = "correlation-id";
    private static final String USER_EMAIL = "user@email.com";
    private static final String PARTITION_ID = "partition-id";

    @Mock
    private JaxRsDpsLog logger;

    @Mock
    private ITopicClientFactory topicClientFactory;

    @Mock
    private TopicClient topicClient;

    @Mock
    private DpsHeaders headers;

    @InjectMocks
    private LegalTagPublisherImpl sut;

    @Before
    public void init() throws ServiceBusException, InterruptedException {
        doReturn(DATA_PARTITION_WITH_FALLBACK_ACCOUNT_ID).when(headers).getPartitionIdWithFallbackToAccountId();
        doReturn(CORRELATION_ID).when(headers).getCorrelationId();
        doReturn(USER_EMAIL).when(headers).getUserEmail();
        doReturn(PARTITION_ID).when(headers).getPartitionId();
        doReturn(topicClient).when(topicClientFactory).getClient(eq(PARTITION_ID), any());
    }

    @Test
    public void testPublishLegalTag() throws Exception {
        StatusChangedTags tags = new StatusChangedTags();
        sut.publish("project-id", headers, tags);
        ArgumentCaptor<Message> msg = ArgumentCaptor.forClass(Message.class);
        ArgumentCaptor<String> log = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Exception> exception = ArgumentCaptor.forClass(Exception.class);

        verify(logger).info(log.capture());
        assertEquals("Storage publishes message " + CORRELATION_ID, log.getValue());

        verify(topicClient).send(msg.capture());
        Map<String, Object> properties = msg.getValue().getProperties();

        assertEquals(DATA_PARTITION_WITH_FALLBACK_ACCOUNT_ID, properties.get(DpsHeaders.ACCOUNT_ID));
        assertEquals(DATA_PARTITION_WITH_FALLBACK_ACCOUNT_ID, properties.get(DpsHeaders.DATA_PARTITION_ID));
        assertEquals(CORRELATION_ID, properties.get(DpsHeaders.CORRELATION_ID));
        assertEquals(USER_EMAIL, properties.get(DpsHeaders.USER_EMAIL));

        MessageBody messageBody = msg.getValue().getMessageBody();
        Gson gson = new Gson();
        String messageKey = "message";
        String dataKey = "data";
        JsonObject jsonObjectMessage = gson.fromJson(new String(messageBody.getBinaryData().get(0)), JsonObject.class);
        JsonObject jsonObject = (JsonObject) jsonObjectMessage.get(messageKey);
        assertEquals(DATA_PARTITION_WITH_FALLBACK_ACCOUNT_ID, jsonObject.get(DpsHeaders.ACCOUNT_ID).getAsString());
        assertEquals(DATA_PARTITION_WITH_FALLBACK_ACCOUNT_ID, jsonObject.get(DpsHeaders.DATA_PARTITION_ID).getAsString());
        assertEquals(CORRELATION_ID, jsonObject.get(DpsHeaders.CORRELATION_ID).getAsString());
        assertEquals(USER_EMAIL, jsonObject.get(DpsHeaders.USER_EMAIL).getAsString());
        assertEquals(gson.toJsonTree(tags), jsonObject.get(dataKey));
    }
}
