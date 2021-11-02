// Copyright © Amazon Web Services
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.legal.aws.jobs;


import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.AmazonSNS;
import org.opengroup.osdu.core.aws.ssm.K8sLocalParameterProvider;
import org.opengroup.osdu.core.aws.ssm.K8sParameterNotFoundException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.aws.sns.AmazonSNSConfig;
import org.opengroup.osdu.core.aws.sns.PublishRequestBuilder;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTag;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LegalTagPublisherImpl implements ILegalTagPublisher {
    private String amazonSNSTopic;

    @Value("${aws.sns.region}")
    private String amazonSNSRegion;

    private AmazonSNS snsClient;

    @Inject
    private DpsHeaders headers;

    @PostConstruct
    public void init() throws K8sParameterNotFoundException {
        AmazonSNSConfig snsConfig = new AmazonSNSConfig(amazonSNSRegion);
        snsClient = snsConfig.AmazonSNS();
        K8sLocalParameterProvider provider = new K8sLocalParameterProvider();
        amazonSNSTopic = provider.getParameterAsString("legal-sns-topic-arn");
    }

    @Override
    public void publish(String projectId, DpsHeaders headers, StatusChangedTags tags) {
        final int BATCH_SIZE = 50;
        // attributes
        Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put(DpsHeaders.ACCOUNT_ID, new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(headers.getPartitionIdWithFallbackToAccountId()));
        messageAttributes.put(DpsHeaders.DATA_PARTITION_ID, new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(headers.getPartitionIdWithFallbackToAccountId()));
        headers.addCorrelationIdIfMissing();
        messageAttributes.put(DpsHeaders.CORRELATION_ID, new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(headers.getCorrelationId()));
        messageAttributes.put(DpsHeaders.AUTHORIZATION, new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(headers.getAuthorization()));

        for (int i = 0; i < tags.getStatusChangedTags().size(); i += BATCH_SIZE){
            List<StatusChangedTag> batch = tags.getStatusChangedTags().subList(i, Math.min(tags.getStatusChangedTags().size(), i + BATCH_SIZE));

            List<AwsStatusChangedTag> awsBatch = batch.stream()
                    .map(t -> new AwsStatusChangedTag(t.getChangedTagName(), t.getChangedTagStatus(), headers.getPartitionId()))
                    .collect(Collectors.toList());

            PublishRequestBuilder<AwsStatusChangedTag> publishRequestBuilder = new PublishRequestBuilder<>();
            PublishRequest publishRequest = publishRequestBuilder.generatePublishRequest("statusChangedTags",
                    awsBatch, messageAttributes, amazonSNSTopic);
            snsClient.publish(publishRequest);
        }
    }
}
