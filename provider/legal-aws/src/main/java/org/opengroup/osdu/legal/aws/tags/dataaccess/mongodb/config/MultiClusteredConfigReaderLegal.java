package org.opengroup.osdu.legal.aws.tags.dataaccess.mongodb.config;

import org.opengroup.osdu.core.aws.mongodb.AbstractMultiClusteredConfigReader;
import org.opengroup.osdu.core.aws.ssm.SSMManagerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class MultiClusteredConfigReaderLegal extends AbstractMultiClusteredConfigReader {

    @Autowired
    public MultiClusteredConfigReaderLegal(SSMManagerUtil ssmManagerUtil) {
        super(ssmManagerUtil);
    }

    @Override
    protected String getDatabaseName(String environment) {
        return environment + "_osdu_legal";
    }
}
