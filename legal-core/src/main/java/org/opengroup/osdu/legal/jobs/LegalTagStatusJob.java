package org.opengroup.osdu.legal.jobs;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTag;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagPublisher;
import org.opengroup.osdu.legal.provider.interfaces.IAboutToExpireLegalTagPublisher;
import org.opengroup.osdu.legal.tags.LegalTagConstraintValidator;
import org.opengroup.osdu.legal.tags.LegalTagService;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.legal.jobs.models.LegalTagJobResult;
import org.opengroup.osdu.legal.jobs.models.AboutToExpireLegalTags;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.feature.IFeatureFlag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Calendar;
import java.util.Date;
import javax.inject.Inject;

@Component
public class LegalTagStatusJob {
    @Inject
    private LegalTagConstraintValidator validator;
    @Inject
    private LegalTagService legalTagService;
    @Inject
    private ILegalTagPublisher legalTagPublisher;
    @Inject
    private IAboutToExpireLegalTagPublisher aboutToExpireLegalTagPublisher;
    @Inject
    private JaxRsDpsLog log;

    @Autowired
    private IFeatureFlag aboutToExpireLegalTagFeatureFlag;

    @Value("${LEGALTAG_EXPIRATION}")
    private String legalTagExpiration;

    private String aboutToExpireFeatureFlagName = "featureFlag.aboutToExpireLegalTag.enabled";

    public LegalTagJobResult run(String projectId, DpsHeaders headers, String tenantName) throws Exception {
        LegalTagJobResult legalTagJobResult = new LegalTagJobResult(new StatusChangedTags(), new AboutToExpireLegalTags());
        validator.setHeaders(headers);
        legalTagJobResult = checkAndUpdateLegalTagStatus(true, tenantName, legalTagJobResult);
        legalTagJobResult = checkAndUpdateLegalTagStatus(false, tenantName, legalTagJobResult);

        publishLegalTagStatusUpdateEvents(!legalTagJobResult.statusChangedTags.getStatusChangedTags().isEmpty(), projectId, headers, legalTagJobResult.statusChangedTags);
        if (isAboutToExpireFeatureFlagEnabled()) {
            publisAboutToExpireLegalTagEvents(!legalTagJobResult.aboutToExpireLegalTags.getAboutToExpireLegalTags().isEmpty(), projectId, headers, legalTagJobResult.aboutToExpireLegalTags);
        }

        return legalTagJobResult;
    }

    private LegalTagJobResult checkAndUpdateLegalTagStatus(Boolean isCurrentlyValid, String tenantName, LegalTagJobResult legalTagJobResult) {
        Collection<LegalTag> validLegalTags = legalTagService.listLegalTag(isCurrentlyValid, tenantName);
        for (LegalTag tag : validLegalTags) {
            String errors = validator.getErrors(tag);
            Boolean hasErrors = errors != null;

            if (isAboutToExpireFeatureFlagEnabled() && isCurrentlyValid) {
                checkAboutToExpireLegalTag(tag, tenantName, legalTagJobResult.aboutToExpireLegalTags, hasErrors);
            }

            if (isCurrentlyValid.equals(hasErrors)) {
                log.info(String.format("Changing state of %s from %s to %s. Errors found in legaltag %s", tag.getName(),
                        isCurrentlyValid ? "Valid" : "Invalid",
                        hasErrors ? "Invalid" : "Valid", errors));
                legalTagJobResult.statusChangedTags.getStatusChangedTags().add(new StatusChangedTag(tag.getName(), hasErrors ? LegalTagCompliance.incompliant : LegalTagCompliance.compliant));
                legalTagService.updateStatus(tag.getName(), !isCurrentlyValid, tenantName);
            }
        }
        return legalTagJobResult;
    }

    private void checkAboutToExpireLegalTag(LegalTag tag, String tenantName, AboutToExpireLegalTags aboutToExpireLegalTags, Boolean hasErrors) {
        Properties properties = tag.getProperties();
        Date expirationDate = properties.getExpirationDate();
        Date today = new Date();
        Date aboutToExpireDate = getAboutToExpireDate(expirationDate);
        Boolean isAboutToExpire = aboutToExpireDate.before(today);

        if (isAboutToExpire) {
            log.info(String.format("Found legal tag about to expire: %s", tag.getName()));
            aboutToExpireLegalTags.getAboutToExpireLegalTags().add(tag.getName());
        }
    }

    private Date getAboutToExpireDate(Date expirationDate) throws AppException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(expirationDate);

        try {
            if (legalTagExpiration.contains("d")) {
                int numberOfDays = Integer.parseInt(legalTagExpiration.replace("d", ""));
                cal.add(Calendar.DAY_OF_YEAR, - numberOfDays);
            } else if (legalTagExpiration.contains("w")) {
                int numberOfWeeks = Integer.parseInt(legalTagExpiration.replace("w", ""));
                cal.add(Calendar.DAY_OF_YEAR, - 7 * numberOfWeeks);
            } else if (legalTagExpiration.contains("m")) {
                int numberOfMonths = Integer.parseInt(legalTagExpiration.replace("m", ""));
                cal.add(Calendar.MONTH, - numberOfMonths);                
            } else if (legalTagExpiration.contains("y")) {
                int numberOfYears = Integer.parseInt(legalTagExpiration.replace("y", ""));
                cal.add(Calendar.YEAR, - numberOfYears);
            } else {
                throw new AppException(500, "Server error", String.format("Invalid legalTagExpiration value: %s", legalTagExpiration));
            }
        } catch (NumberFormatException e) {
            log.error(String.format("Invalid legalTagExpiration value: %s", legalTagExpiration));
            throw new AppException(500, "Server error", String.format("Invalid legalTagExpiration value: %s", legalTagExpiration));
        }

        return cal.getTime();
    }

    private void publishLegalTagStatusUpdateEvents(boolean hasStatusChanges, String projectId, DpsHeaders headers, StatusChangedTags statusChangedTags) throws Exception {
        if (hasStatusChanges) {
            legalTagPublisher.publish(projectId, headers, statusChangedTags);
        }
    }

    private void publisAboutToExpireLegalTagEvents(boolean hasTags, String projectId, DpsHeaders headers, AboutToExpireLegalTags aboutToExpireLegalTags) throws Exception {
        if (hasTags) {
            aboutToExpireLegalTagPublisher.publish(projectId, headers, aboutToExpireLegalTags);
        }
    }

    private Boolean isAboutToExpireFeatureFlagEnabled() {
        return aboutToExpireLegalTagFeatureFlag.isFeatureEnabled(aboutToExpireFeatureFlagName);
    }
}
