package org.opengroup.osdu.legal.aws.tags.dataaccess.mongodb.config;

import org.opengroup.osdu.core.aws.mongodb.MultiClusteredConfigReader;
import org.opengroup.osdu.core.aws.mongodb.config.MongoProperties;
import org.opengroup.osdu.core.aws.partition.PartitionInfoAws;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;


@Component
@Lazy
public class MultiClusteredConfigReaderLegal implements MultiClusteredConfigReader {

    private final MongoPropertiesReader propertiesReader;

    @Autowired
    public MultiClusteredConfigReaderLegal(MongoPropertiesReader propertiesReader) {
        this.propertiesReader = propertiesReader;
    }

    @Override
    public MongoProperties readProperties(PartitionInfoAws partitionInfoAws) {
        return propertiesReader.getProperties();
    }
}
