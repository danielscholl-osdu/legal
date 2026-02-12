package org.opengroup.osdu.legal.logging;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.util.IpAddressUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@RequestScope
@Component
@RequiredArgsConstructor
public class AuditLogger {
	private final JaxRsDpsLog logger;
	private final DpsHeaders headers;
	private final HttpServletRequest httpServletRequest;

	private AuditEvents events = null;

	private AuditEvents getEvents() {
		if (this.events == null) {
			String user = headers.getUserEmail();
			String userIpAddress = IpAddressUtil.getClientIpAddress(httpServletRequest);
			String userAgent = httpServletRequest.getHeader("User-Agent");
			String userAuthorizedGroupName = headers.getUserAuthorizedGroupName();
			this.events = new AuditEvents(user, userIpAddress, userAgent, userAuthorizedGroupName);
		}
		return this.events;
	}

	public void createdLegalTagSuccess(List<String> resources, List<String> requiredGroupsForAction) {
		this.writeLog(this.getEvents().getCreateLegalTagEventSuccess(resources, requiredGroupsForAction));
	}

	public void deletedLegalTagSuccess(List<String> resources, List<String> requiredGroupsForAction) {
		this.writeLog(this.getEvents().getDeleteLegalTagEventSuccess(resources, requiredGroupsForAction));
	}

	public void publishedStatusChangeSuccess(List<String> resources, List<String> requiredGroupsForAction) {
		this.writeLog(this.getEvents().getPublishStatusEventSuccess(resources, requiredGroupsForAction));
	}

	public void readLegalTagSuccess(List<String> resources, List<String> requiredGroupsForAction) {
		this.writeLog(this.getEvents().getReadLegalTagEventSuccess(resources, requiredGroupsForAction));
	}

	public void readLegalTagFail(List<String> resources, List<String> requiredGroupsForAction) {
		this.writeLog(this.getEvents().getReadLegalTagEventFail(resources, requiredGroupsForAction));
	}

	public void updatedLegalTagSuccess(List<String> resources, List<String> requiredGroupsForAction) {
		this.writeLog(this.getEvents().getUpdateLegalTagEventSuccess(resources, requiredGroupsForAction));
	}

	public void updatedLegalTagFail(List<String> resources, List<String> requiredGroupsForAction) {
		this.writeLog(this.getEvents().getUpdateLegalTagEventFail(resources, requiredGroupsForAction));
	}

	public void legalTagJobRanSuccess(List<String> resources, List<String> requiredGroupsForAction) {
		this.writeLog(this.getEvents().getLegalTagStatusJobEventSuccess(resources, requiredGroupsForAction));
	}

	public void legalTagJobRanFail(List<String> resources, List<String> requiredGroupsForAction) {
		this.writeLog(this.getEvents().getLegalTagStatusJobEventFail(resources, requiredGroupsForAction));
	}

	public void readLegalPropertiesSuccess(List<String> resources, List<String> requiredGroupsForAction) {
		this.writeLog(this.getEvents().getReadLegalPropertiesEventSuccess(resources, requiredGroupsForAction));
	}

	public void readLegalPropertiesFail(List<String> resources, List<String> requiredGroupsForAction) {
		this.writeLog(this.getEvents().getReadLegalPropertiesEventFail(resources, requiredGroupsForAction));
	}

	public void validateLegalTagSuccess(List<String> requiredGroupsForAction) {
		this.writeLog(this.getEvents().getValidateLegalTagEventSuccess(requiredGroupsForAction));
	}

	public void validateLegalTagFail(List<String> requiredGroupsForAction) {
		this.writeLog(this.getEvents().getValidateLegalTagEventFail(requiredGroupsForAction));
	}

	public void legalTagsBackup(String tenant, List<String> requiredGroupsForAction) {
		this.writeLog(this.getEvents().getLegalTagBackupEvent(tenant, requiredGroupsForAction));
	}

	public void legalTagRestored(String tenant, List<String> requiredGroupsForAction) {
		this.writeLog(this.getEvents().getLegalTagRestoreEvent(tenant, requiredGroupsForAction));
	}

	private void writeLog(AuditPayload log) {
		this.logger.audit(log);
	}
}
