/*
 * Copyright 2020 Google LLC
 * Copyright 2020 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.legal.jobs;

import com.google.api.gax.rpc.DeadlineExceededException;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import org.opengroup.osdu.legal.logging.AuditLogger;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.core.gcp.PubSub.PubSubExtensions;
import org.opengroup.osdu.core.gcp.multitenancy.IPublisherFactory;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagPublisher;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

import static java.util.Arrays.asList;

@Service
public class LegalTagPublisherImpl implements ILegalTagPublisher {

    @Inject
    private IPublisherFactory publisherFactory;

    @Inject
    private PubSubExtensions pubSubExtensions;

    @Inject
    private AuditLogger auditLogger;

    private String topicId = "legaltags_changed";

    public void publish(String projectId, DpsHeaders headers, StatusChangedTags statusChangedTags) throws Exception {

        Publisher publisher = publisherFactory.createPublisher(projectId, topicId);

        String messageId;
        PubsubMessage pubsubMessage = null;
        try {
            pubsubMessage = generatePubsubMessage(headers, statusChangedTags);
            messageId = pubSubExtensions.publishAndCreateTopicIfNotExist(publisher, pubsubMessage);
        } catch (DeadlineExceededException e) {//try again
            messageId = pubSubExtensions.publishAndCreateTopicIfNotExist(publisher, pubsubMessage);
        } finally {
            publisher.shutdown();
        }
        auditLogger.publishedStatusChangeSuccess(asList(messageId, statusChangedTags.toString()));

    }

    private PubsubMessage generatePubsubMessage(DpsHeaders headers, StatusChangedTags statusChangedTags) {
        Gson gson = new GsonBuilder().create();
        JsonElement statusChangedTagsJson = gson.toJsonTree(statusChangedTags, StatusChangedTags.class);
        ByteString statusChangedTagsData = ByteString.copyFromUtf8(statusChangedTagsJson.toString());

        PubsubMessage.Builder builder = PubsubMessage.newBuilder();
        builder.putAttributes(DpsHeaders.DATA_PARTITION_ID, headers.getPartitionIdWithFallbackToAccountId());
        builder.putAttributes(DpsHeaders.CORRELATION_ID, headers.getCorrelationId());
        builder.putAttributes(DpsHeaders.USER_EMAIL, headers.getUserEmail());
        builder.setData(statusChangedTagsData);

        return builder.build();
    }
}
