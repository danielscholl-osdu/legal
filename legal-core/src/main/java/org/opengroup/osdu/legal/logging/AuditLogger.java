package org.opengroup.osdu.legal.logging;

import java.util.List;

import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@RequestScope
@Component
public class AuditLogger {
	private final JaxRsDpsLog logger;
	private final RequestInfo requestInfo;
	private AuditEvents events = null;

	public AuditLogger(JaxRsDpsLog logger, RequestInfo requestInfo) {
		this.logger = logger;
		this.requestInfo = requestInfo;
	}

	private AuditEvents getEvents() {
		if (this.events == null) {
			this.events = new AuditEvents(this.requestInfo.getUser());
		}
		return this.events;
	}

	public void createdLegalTagSuccess(List<String> resources) {
		this.writeLog(this.getEvents().getCreateLegalTagEventSuccess(resources));
	}

	public void deletedLegalTagSuccess(List<String> resources) {
		this.writeLog(this.getEvents().getDeleteLegalTagEventSuccess(resources));
	}

	public void publishedStatusChangeSuccess(List<String> resources) {
		this.writeLog(this.getEvents().getPublishStatusEventSuccess(resources));
	}

	public void readLegalTagSuccess(List<String> resources) {
		this.writeLog(this.getEvents().getReadLegalTagEventSuccess(resources));
	}

	public void readLegalTagFail(List<String> resources) {
		this.writeLog(this.getEvents().getReadLegalTagEventFail(resources));
	}

	public void updatedLegalTagSuccess(List<String> resources) {
		this.writeLog(this.getEvents().getUpdateLegalTagEventSuccess(resources));
	}

	public void updatedLegalTagFail(List<String> resources) {
		this.writeLog(this.getEvents().getUpdateLegalTagEventFail(resources));
	}

	public void legalTagJobRanSuccess(List<String> resources) {
		this.writeLog(this.getEvents().getLegalTagStatusJobEventSuccess(resources));
	}

	public void legalTagJobRanFail(List<String> resources) {
		this.writeLog(this.getEvents().getLegalTagStatusJobEventFail(resources));
	}

	public void readLegalPropertiesSuccess(List<String> resources) {
		this.writeLog(this.getEvents().getReadLegalPropertiesEventSuccess(resources));
	}

	public void readLegalPropertiesFail(List<String> resources) {
		this.writeLog(this.getEvents().getReadLegalPropertiesEventFail(resources));
	}

	public void validateLegalTagSuccess() {
		this.writeLog(this.getEvents().getValidateLegalTagEventSuccess());
	}

	public void validateLegalTagFail() {
		this.writeLog(this.getEvents().getValidateLegalTagEventFail());
	}

	public void legalTagsBackup(String tenant) {
		this.writeLog(this.getEvents().getLegalTagBackupEvent(tenant));
	}

	public void legalTagRestored(String tenant) {
		this.writeLog(this.getEvents().getLegalTagRestoreEvent(tenant));
	}

	private void writeLog(AuditPayload log) {
		this.logger.audit(log);
	}
}