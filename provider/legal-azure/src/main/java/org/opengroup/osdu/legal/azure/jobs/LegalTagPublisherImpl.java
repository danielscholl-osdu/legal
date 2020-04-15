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
import com.microsoft.azure.servicebus.TopicClient;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagPublisher;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;


@Component
public class LegalTagPublisherImpl implements ILegalTagPublisher {
    @Inject
    private TopicClient topicClient;

    @Inject
    private JaxRsDpsLog logger;

    @Override
    public void publish(String projectId, DpsHeaders headers, StatusChangedTags tags) throws Exception {
        Gson gson = new Gson();
        Message message = new Message();
        Map<String, Object> properties = new HashMap<>();

        // properties
        properties.put(DpsHeaders.ACCOUNT_ID, headers.getPartitionIdWithFallbackToAccountId());
        properties.put(DpsHeaders.DATA_PARTITION_ID, headers.getPartitionIdWithFallbackToAccountId());
        headers.addCorrelationIdIfMissing();
        properties.put(DpsHeaders.CORRELATION_ID, headers.getCorrelationId());
        properties.put(DpsHeaders.USER_EMAIL, headers.getUserEmail());

        message.setProperties(properties);

        // add all to body {"message": {"data":[], "id":...}}
        JsonObject jo = new JsonObject();
        jo.add("data", gson.toJsonTree(tags));
        jo.addProperty(DpsHeaders.ACCOUNT_ID, headers.getPartitionIdWithFallbackToAccountId());
        jo.addProperty(DpsHeaders.DATA_PARTITION_ID, headers.getPartitionIdWithFallbackToAccountId());
        jo.addProperty(DpsHeaders.CORRELATION_ID, headers.getCorrelationId());
        jo.addProperty(DpsHeaders.USER_EMAIL, headers.getUserEmail());
        JsonObject jomsg = new JsonObject();
        jomsg.add("message", jo);

        message.setBody(jomsg.toString().getBytes(StandardCharsets.UTF_8));
        message.setContentType("application/json");

        try {
            logger.info("Storage publishes message " + headers.getCorrelationId());
            topicClient.send(message);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
    }
}
