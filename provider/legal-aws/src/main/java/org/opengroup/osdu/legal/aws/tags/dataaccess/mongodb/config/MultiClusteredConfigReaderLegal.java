package org.opengroup.osdu.legal.aws.tags.dataaccess.mongodb.config;

import org.opengroup.osdu.core.aws.mongodb.AbstractMultiClusteredConfigReader;
import org.opengroup.osdu.core.aws.ssm.SSMManagerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class MultiClusteredConfigReaderLegal extends AbstractMultiClusteredConfigReader {
    String serviceName = "legal";

    @Autowired
    public MultiClusteredConfigReaderLegal(SSMManagerUtil ssmManagerUtil) {
        super(ssmManagerUtil);
    }

    @Override
    protected String applyServiceName(String originalName) {
        return originalName.replace(serviceNamePlaceHolder, serviceName);
    }
}
