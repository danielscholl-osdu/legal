package org.opengroup.osdu.legal.aws.jobs;

import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.AmazonSNS;
import org.opengroup.osdu.core.aws.ssm.K8sLocalParameterProvider;
import org.opengroup.osdu.core.aws.ssm.K8sParameterNotFoundException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.aws.sns.AmazonSNSConfig;
import org.opengroup.osdu.core.aws.sns.PublishRequestBuilder;
import org.opengroup.osdu.legal.provider.interfaces.IAboutToExpireLegalTagPublisher;
import org.opengroup.osdu.legal.jobs.models.AboutToExpireLegalTag;
import org.opengroup.osdu.legal.jobs.models.AboutToExpireLegalTags;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AboutToExpireLegalTagPublisherImpl implements IAboutToExpireLegalTagPublisher {
    private String amazonSNSTopic;

    @Value("${aws.sns.region}")
    private String amazonSNSRegion;

    private AmazonSNS snsClient;

    private K8sLocalParameterProvider k8sLocalParameterProvider;

    @Value("${OSDU_ABOUT_TO_EXPIRE_LEGALTAG_TOPIC}")
    private String osduAboutToExpireLegalTagTopic;

    @Inject
    private JaxRsDpsLog log;

    public void setK8sLocalParameterProvider(K8sLocalParameterProvider k8sLocalParameterProvider) {
        this.k8sLocalParameterProvider = k8sLocalParameterProvider;
    }

    @PostConstruct
    public void init() throws K8sParameterNotFoundException {
        if (this.k8sLocalParameterProvider == null) {
            this.k8sLocalParameterProvider = new K8sLocalParameterProvider(); 
        }

        AmazonSNSConfig snsConfig = new AmazonSNSConfig(amazonSNSRegion);
        snsClient = snsConfig.AmazonSNS();
        amazonSNSTopic = k8sLocalParameterProvider.getParameterAsString("legal-sns-topic-arn");
    }

    @Override
    public void publish(String projectId, DpsHeaders headers, AboutToExpireLegalTags aboutToExpireLegalTags) {
        final int BATCH_SIZE = 50;
        // attributes
        PublishRequestBuilder<AwsAboutToExpireLegalTags> publishRequestBuilder = new PublishRequestBuilder<>();
        publishRequestBuilder.setGeneralParametersFromHeaders(headers);
        log.debug("Publishing to topic " + osduAboutToExpireLegalTagTopic);
        for (int i = 0; i < aboutToExpireLegalTags.getAboutToExpireLegalTags().size(); i += BATCH_SIZE){
            List<AboutToExpireLegalTag> batch = aboutToExpireLegalTags.getAboutToExpireLegalTags().subList(i, Math.min(aboutToExpireLegalTags.getAboutToExpireLegalTags().size(), i + BATCH_SIZE));
            List<AwsAboutToExpireLegalTag> awsBatch = batch.stream()
                    .map(aboutToExpireLegalTag -> new AwsAboutToExpireLegalTag(aboutToExpireLegalTag.getTagName(), headers.getPartitionId(), aboutToExpireLegalTag.getExpirationDate()))
                    .collect(Collectors.toList());
            AwsAboutToExpireLegalTags awsBatchTags = new AwsAboutToExpireLegalTags(awsBatch);
            PublishRequest publishRequest = publishRequestBuilder.generatePublishRequest(osduAboutToExpireLegalTagTopic, amazonSNSTopic, awsBatchTags);
            snsClient.publish(publishRequest);
        }
    }
}

record AwsAboutToExpireLegalTags(List<AwsAboutToExpireLegalTag> aboutToExpireLegalTags) { }
