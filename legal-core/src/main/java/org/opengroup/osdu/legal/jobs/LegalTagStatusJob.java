package org.opengroup.osdu.legal.jobs;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTag;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagPublisher;
import org.opengroup.osdu.legal.tags.LegalTagConstraintValidator;
import org.opengroup.osdu.legal.tags.LegalTagService;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.springframework.stereotype.Component;

import java.util.Collection;
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
    private JaxRsDpsLog log;

    public StatusChangedTags run(String projectId, DpsHeaders headers, String tenantName) throws Exception {
        StatusChangedTags statusChangedTags = new StatusChangedTags();
        validator.setHeaders(headers);
        statusChangedTags = checkAndUpdateLegalTagStatus(true, tenantName, statusChangedTags);
        statusChangedTags = checkAndUpdateLegalTagStatus(false, tenantName, statusChangedTags);
        if (!statusChangedTags.getStatusChangedTags().isEmpty()) {
            legalTagPublisher.publish(projectId, headers, statusChangedTags);
        }
        return statusChangedTags;
    }

    private StatusChangedTags checkAndUpdateLegalTagStatus(Boolean isCurrentlyValid, String tenantName, StatusChangedTags statusChangedTags) {
        Collection<LegalTag> validLegalTags = legalTagService.listLegalTag(isCurrentlyValid, tenantName);
        for (LegalTag tag : validLegalTags) {
            String errors = validator.getErrors(tag);
            Boolean hasErrors = errors != null;
            if (isCurrentlyValid.equals(hasErrors)) {
                log.info(String.format("Changing state of %s from %s to %s. Errors found in legaltag %s", tag.getName(),
                        isCurrentlyValid ? "Valid" : "Invalid",
                        hasErrors ? "Invalid" : "Valid", errors));
                statusChangedTags.getStatusChangedTags().add(new StatusChangedTag(tag.getName(), hasErrors ? LegalTagCompliance.incompliant : LegalTagCompliance.compliant));
                legalTagService.updateStatus(tag.getName(), !isCurrentlyValid, tenantName);
            }
        }
        return statusChangedTags;
    }
}
