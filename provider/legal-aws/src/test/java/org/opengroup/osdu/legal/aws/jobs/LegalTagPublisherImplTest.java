package org.opengroup.osdu.legal.aws.jobs;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opengroup.osdu.core.aws.sns.AmazonSNSConfig;
import org.opengroup.osdu.core.aws.sns.PublishRequestBuilder;
import org.opengroup.osdu.core.aws.ssm.K8sLocalParameterProvider;
import org.opengroup.osdu.core.aws.ssm.K8sParameterNotFoundException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

import org.springframework.test.util.ReflectionTestUtils;

import com.amazonaws.services.sns.AmazonSNS;

class LegalTagPublisherImplTest {
    
    @InjectMocks
    private LegalTagPublisherImpl legalTagPublisherImpl;

    @Mock
    private AmazonSNSConfig snsConfig;

    @Mock
    private K8sLocalParameterProvider k8sLocalParameterProvider;

    @Mock
    private AmazonSNS snsClient;

    @Mock
    private DpsHeaders headers;

    @Mock
    private PublishRequestBuilder<AwsStatusChangedTag> publishRequestBuilder;


    private final String testTopic = "testTopic";


    private final String testRegion = "testRegion";
    private static final String DATA_PARTITION_ID = "testDataPartitionId";
    private static final String CORRELATION_ID = "testCorrelationId";
    private static final String AUTHORIZATION = "testAuthorization";
    
    @BeforeEach
    void setup() throws K8sParameterNotFoundException {
        MockitoAnnotations.openMocks(this);
        when(snsConfig.AmazonSNS()).thenReturn(snsClient);
        when(k8sLocalParameterProvider.getParameterAsString("legal-sns-topic-arn"))
                .thenReturn(testTopic);
        ReflectionTestUtils.setField(legalTagPublisherImpl, "amazonSNSRegion", testRegion);
        when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(DATA_PARTITION_ID);
        doNothing().when(headers).addCorrelationIdIfMissing();
        when(headers.getCorrelationId()).thenReturn(CORRELATION_ID);
        when(headers.getAuthorization()).thenReturn(AUTHORIZATION);
        
        legalTagPublisherImpl.init();

    }

    @Test
    void testInit() throws K8sParameterNotFoundException{
        
        // Assert
        assertNotNull(legalTagPublisherImpl);
    }       

}

