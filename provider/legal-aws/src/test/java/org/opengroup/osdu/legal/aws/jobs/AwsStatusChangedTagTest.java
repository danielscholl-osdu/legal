package org.opengroup.osdu.legal.aws.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AwsStatusChangedTagTest {

    @Mock
    private Enum mockEnum;

    private AwsStatusChangedTag awsStatusChangedTag;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        awsStatusChangedTag = new AwsStatusChangedTag("testName", mockEnum, "partition123");
    }

    @Test
    void testConstructor() {
        assertEquals("testName", awsStatusChangedTag.getChangedTagName());
        assertEquals(mockEnum, awsStatusChangedTag.getChangedTagStatus());
        assertEquals("partition123", awsStatusChangedTag.getDataPartitionId());
    }

}
