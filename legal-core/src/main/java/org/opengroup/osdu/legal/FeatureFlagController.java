package org.opengroup.osdu.legal;

import org.opengroup.osdu.core.common.feature.IFeatureFlag;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class FeatureFlagController {

    @Autowired
    private IFeatureFlag aboutToExpireLegalTagFeatureFlag;    

    public Boolean isAboutToExpireFeatureFlagEnabled() {
        return aboutToExpireLegalTagFeatureFlag.isFeatureEnabled(Constants.ABOUT_TO_EXPIRE_FEATURE_NAME);
    }
}
