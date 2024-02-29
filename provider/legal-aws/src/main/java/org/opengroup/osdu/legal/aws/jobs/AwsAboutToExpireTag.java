package org.opengroup.osdu.legal.aws.jobs;

import lombok.Data;

@Data
public class AwsAboutToExpireTag {

    private String tagName;
	private String dataPartitionId;

    AwsAboutToExpireTag(){
    }
    public AwsAboutToExpireTag(String tagName, String dataPartitionId) {
        this.tagName = tagName;
        this.dataPartitionId = dataPartitionId;
    }

}
