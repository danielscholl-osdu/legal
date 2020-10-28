package org.opengroup.osdu.legal.logging;

import static java.util.Collections.singletonList;

import java.util.List;

import com.google.common.base.Strings;
import org.opengroup.osdu.core.common.logging.audit.AuditAction;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;

public class AuditEvents {
	private static final String CREATE_ACTION_ID = "LG101";
	private static final String CREATE_MESSAGE_SUCCESS = "Legal tag created success";

	private static final String DELETE_ACTION_ID = "LG102";
	private static final String DELETE_MESSAGE_SUCCESS = "Legal tag deleted success";

	private static final String UPDATE_ACTION_ID = "LG103";
	private static final String UPDATE_MESSAGE_SUCCESS = "Legal tag updated success";
	private static final String UPDATE_MESSAGE_FAILURE = "Legal tag updated failure";

	private static final String RUN_JOB_ACTION_ID = "LG104";
	private static final String RUN_JOB_MESSAGE_SUCCESS = "Legal tag status job run success";
	private static final String RUN_JOB_MESSAGE_FAILURE = "Legal tag status job run failure";

	private static final String PUBLISH_ACTION_ID = "LG105";
	private static final String PUBLISH_MESSAGE_SUCCESS = "Published status changed event";

	private static final String READ_ACTION_ID = "LG106";
	private static final String READ_MESSAGE_SUCCESS = "Legal tag read success";
	private static final String READ_MESSAGE_FAILURE = "Legal tag read failure";

	private static final String LEGALTAG_BACKUP_ACTION_ID = "LG107";
	private static final String LEGALTAG_BACKUP_ACTION_MESSAGE = "LegalTags backup for tenant";

	private static final String LEGALTAG_RESTORE_ACTION_ID = "LG108";
	private static final String LEGALTAG_RESTORE_ACTION_MESSAGE = "LegalTags restored for tenant";

	private static final String PROPERTY_VALUE = "Property Values";

	public AuditEvents(String user) {
		if (Strings.isNullOrEmpty(user)) {
			throw new IllegalArgumentException("User not supplied for audit events");
		}

		this.user = user;
	}

	private final String user;

	public AuditPayload getPublishStatusEventSuccess(List<String> resources) {
		return AuditPayload.builder()
				.action(AuditAction.PUBLISH)
				.status(AuditStatus.SUCCESS)
				.user(this.user)
				.actionId(PUBLISH_ACTION_ID)
				.message(PUBLISH_MESSAGE_SUCCESS)
				.resources(resources)
				.build();
	}

	public AuditPayload getReadLegalTagEventSuccess(List<String> resources) {
		return AuditPayload.builder()
				.action(AuditAction.READ)
				.status(AuditStatus.SUCCESS)
				.user(this.user)
				.actionId(READ_ACTION_ID)
				.message(READ_MESSAGE_SUCCESS)
				.resources(resources)
				.build();
	}

	public AuditPayload getReadLegalTagEventFail(List<String> resources) {
		return AuditPayload.builder()
				.action(AuditAction.READ)
				.status(AuditStatus.FAILURE)
				.user(this.user)
				.actionId(READ_ACTION_ID)
				.message(READ_MESSAGE_FAILURE)
				.resources(resources)
				.build();
	}

	public AuditPayload getReadLegalPropertiesEventSuccess(List<String> resources) {
		return AuditPayload.builder()
				.action(AuditAction.READ)
				.status(AuditStatus.SUCCESS)
				.user(this.user)
				.actionId(READ_ACTION_ID)
				.message(READ_MESSAGE_SUCCESS)
				.resources(resources)
				.build();
	}

	public AuditPayload getReadLegalPropertiesEventFail(List<String> resources) {
		return AuditPayload.builder()
				.action(AuditAction.READ)
				.status(AuditStatus.FAILURE)
				.user(this.user)
				.actionId(READ_ACTION_ID)
				.message(READ_MESSAGE_FAILURE)
				.resources(resources)
				.build();
	}

	public AuditPayload getCreateLegalTagEventSuccess(List<String> resources) {
		return AuditPayload.builder()
				.action(AuditAction.CREATE)
				.status(AuditStatus.SUCCESS)
				.user(this.user)
				.actionId(CREATE_ACTION_ID)
				.message(CREATE_MESSAGE_SUCCESS)
				.resources(resources)
				.build();
	}

	public AuditPayload getDeleteLegalTagEventSuccess(List<String> resources) {
		return AuditPayload.builder()
				.action(AuditAction.DELETE)
				.status(AuditStatus.SUCCESS)
				.user(this.user)
				.actionId(DELETE_ACTION_ID)
				.message(DELETE_MESSAGE_SUCCESS)
				.resources(resources)
				.build();
	}

	public AuditPayload getUpdateLegalTagEventSuccess(List<String> resources) {
		return AuditPayload.builder()
				.action(AuditAction.UPDATE)
				.status(AuditStatus.SUCCESS)
				.user(this.user)
				.actionId(UPDATE_ACTION_ID)
				.message(UPDATE_MESSAGE_SUCCESS)
				.resources(resources)
				.build();
	}

	public AuditPayload getUpdateLegalTagEventFail(List<String> resources) {
		return AuditPayload.builder()
				.action(AuditAction.UPDATE)
				.status(AuditStatus.FAILURE)
				.user(this.user)
				.actionId(UPDATE_ACTION_ID)
				.message(UPDATE_MESSAGE_FAILURE)
				.resources(resources)
				.build();
	}

	public AuditPayload getLegalTagStatusJobEventSuccess(List<String> resources) {
		return AuditPayload.builder()
				.action(AuditAction.JOB_RUN)
				.status(AuditStatus.SUCCESS)
				.user(this.user)
				.actionId(RUN_JOB_ACTION_ID)
				.message(RUN_JOB_MESSAGE_SUCCESS)
				.resources(resources)
				.build();
	}

	public AuditPayload getLegalTagStatusJobEventFail(List<String> resources) {
		return AuditPayload.builder()
				.action(AuditAction.JOB_RUN)
				.status(AuditStatus.FAILURE)
				.user(this.user)
				.actionId(RUN_JOB_ACTION_ID)
				.message(RUN_JOB_MESSAGE_FAILURE)
				.resources(resources)
				.build();
	}

	public AuditPayload getValidateLegalTagEventSuccess() {
		return AuditPayload.builder()
				.action(AuditAction.READ)
				.status(AuditStatus.SUCCESS)
				.user(this.user)
				.actionId(READ_ACTION_ID)
				.message(READ_MESSAGE_SUCCESS)
				.resources(singletonList(PROPERTY_VALUE))
				.build();
	}

	public AuditPayload getValidateLegalTagEventFail() {
		return AuditPayload.builder()
				.action(AuditAction.READ)
				.status(AuditStatus.FAILURE)
				.user(this.user)
				.actionId(READ_ACTION_ID)
				.message(READ_MESSAGE_FAILURE)
				.resources(singletonList(PROPERTY_VALUE))
				.build();
	}

	public AuditPayload getLegalTagBackupEvent(String tenant) {
		return AuditPayload.builder()
				.action(AuditAction.READ)
				.status(AuditStatus.SUCCESS)
				.user(this.user)
				.actionId(LEGALTAG_BACKUP_ACTION_ID)
				.message(LEGALTAG_BACKUP_ACTION_MESSAGE)
				.resources(singletonList("LegalTags backup for tenant: " + tenant))
				.build();
	}

	public AuditPayload getLegalTagRestoreEvent(String tenant) {
		return AuditPayload.builder()
				.action(AuditAction.READ)
				.status(AuditStatus.SUCCESS)
				.user(this.user)
				.actionId(LEGALTAG_RESTORE_ACTION_ID)
				.message(LEGALTAG_RESTORE_ACTION_MESSAGE)
				.resources(singletonList("LegalTags restored for tenant: " + tenant))
				.build();
	}
}