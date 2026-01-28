package org.opengroup.osdu.legal.logging;

import static java.util.Collections.singletonList;

import java.util.List;

import com.google.common.base.Strings;
import org.opengroup.osdu.core.common.logging.audit.AuditAction;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;

public class AuditEvents {

	private static final String UNKNOWN = "unknown";
	private static final String UNKNOWN_IP = "0.0.0.0";

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

	private final String user;
	private final String userIpAddress;
	private final String userAgent;
	private final String userAuthorizedGroupName;

	public AuditEvents(String user, String userIpAddress, String userAgent, String userAuthorizedGroupName) {
		this.user = Strings.isNullOrEmpty(user) ? UNKNOWN : user;
		this.userIpAddress = Strings.isNullOrEmpty(userIpAddress) ? UNKNOWN_IP : userIpAddress;
		this.userAgent = Strings.isNullOrEmpty(userAgent) ? UNKNOWN : userAgent;
		this.userAuthorizedGroupName = Strings.isNullOrEmpty(userAuthorizedGroupName) ? UNKNOWN : userAuthorizedGroupName;
	}

	public AuditPayload getPublishStatusEventSuccess(List<String> resources, List<String> requiredGroupsForAction) {
		return AuditPayload.builder()
				.action(AuditAction.PUBLISH)
				.status(AuditStatus.SUCCESS)
				.user(this.user)
				.actionId(PUBLISH_ACTION_ID)
				.message(PUBLISH_MESSAGE_SUCCESS)
				.resources(resources)
				.requiredGroupsForAction(requiredGroupsForAction)
				.userIpAddress(this.userIpAddress)
				.userAgent(this.userAgent)
				.userAuthorizedGroupName(this.userAuthorizedGroupName)
				.build();
	}

	public AuditPayload getReadLegalTagEventSuccess(List<String> resources, List<String> requiredGroupsForAction) {
		return AuditPayload.builder()
				.action(AuditAction.READ)
				.status(AuditStatus.SUCCESS)
				.user(this.user)
				.actionId(READ_ACTION_ID)
				.message(READ_MESSAGE_SUCCESS)
				.resources(resources)
				.requiredGroupsForAction(requiredGroupsForAction)
				.userIpAddress(this.userIpAddress)
				.userAgent(this.userAgent)
				.userAuthorizedGroupName(this.userAuthorizedGroupName)
				.build();
	}

	public AuditPayload getReadLegalTagEventFail(List<String> resources, List<String> requiredGroupsForAction) {
		return AuditPayload.builder()
				.action(AuditAction.READ)
				.status(AuditStatus.FAILURE)
				.user(this.user)
				.actionId(READ_ACTION_ID)
				.message(READ_MESSAGE_FAILURE)
				.resources(resources)
				.requiredGroupsForAction(requiredGroupsForAction)
				.userIpAddress(this.userIpAddress)
				.userAgent(this.userAgent)
				.userAuthorizedGroupName(this.userAuthorizedGroupName)
				.build();
	}

	public AuditPayload getReadLegalPropertiesEventSuccess(List<String> resources, List<String> requiredGroupsForAction) {
		return AuditPayload.builder()
				.action(AuditAction.READ)
				.status(AuditStatus.SUCCESS)
				.user(this.user)
				.actionId(READ_ACTION_ID)
				.message(READ_MESSAGE_SUCCESS)
				.resources(resources)
				.requiredGroupsForAction(requiredGroupsForAction)
				.userIpAddress(this.userIpAddress)
				.userAgent(this.userAgent)
				.userAuthorizedGroupName(this.userAuthorizedGroupName)
				.build();
	}

	public AuditPayload getReadLegalPropertiesEventFail(List<String> resources, List<String> requiredGroupsForAction) {
		return AuditPayload.builder()
				.action(AuditAction.READ)
				.status(AuditStatus.FAILURE)
				.user(this.user)
				.actionId(READ_ACTION_ID)
				.message(READ_MESSAGE_FAILURE)
				.resources(resources)
				.requiredGroupsForAction(requiredGroupsForAction)
				.userIpAddress(this.userIpAddress)
				.userAgent(this.userAgent)
				.userAuthorizedGroupName(this.userAuthorizedGroupName)
				.build();
	}

	public AuditPayload getCreateLegalTagEventSuccess(List<String> resources, List<String> requiredGroupsForAction) {
		return AuditPayload.builder()
				.action(AuditAction.CREATE)
				.status(AuditStatus.SUCCESS)
				.user(this.user)
				.actionId(CREATE_ACTION_ID)
				.message(CREATE_MESSAGE_SUCCESS)
				.resources(resources)
				.requiredGroupsForAction(requiredGroupsForAction)
				.userIpAddress(this.userIpAddress)
				.userAgent(this.userAgent)
				.userAuthorizedGroupName(this.userAuthorizedGroupName)
				.build();
	}

	public AuditPayload getDeleteLegalTagEventSuccess(List<String> resources, List<String> requiredGroupsForAction) {
		return AuditPayload.builder()
				.action(AuditAction.DELETE)
				.status(AuditStatus.SUCCESS)
				.user(this.user)
				.actionId(DELETE_ACTION_ID)
				.message(DELETE_MESSAGE_SUCCESS)
				.resources(resources)
				.requiredGroupsForAction(requiredGroupsForAction)
				.userIpAddress(this.userIpAddress)
				.userAgent(this.userAgent)
				.userAuthorizedGroupName(this.userAuthorizedGroupName)
				.build();
	}

	public AuditPayload getUpdateLegalTagEventSuccess(List<String> resources, List<String> requiredGroupsForAction) {
		return AuditPayload.builder()
				.action(AuditAction.UPDATE)
				.status(AuditStatus.SUCCESS)
				.user(this.user)
				.actionId(UPDATE_ACTION_ID)
				.message(UPDATE_MESSAGE_SUCCESS)
				.resources(resources)
				.requiredGroupsForAction(requiredGroupsForAction)
				.userIpAddress(this.userIpAddress)
				.userAgent(this.userAgent)
				.userAuthorizedGroupName(this.userAuthorizedGroupName)
				.build();
	}

	public AuditPayload getUpdateLegalTagEventFail(List<String> resources, List<String> requiredGroupsForAction) {
		return AuditPayload.builder()
				.action(AuditAction.UPDATE)
				.status(AuditStatus.FAILURE)
				.user(this.user)
				.actionId(UPDATE_ACTION_ID)
				.message(UPDATE_MESSAGE_FAILURE)
				.resources(resources)
				.requiredGroupsForAction(requiredGroupsForAction)
				.userIpAddress(this.userIpAddress)
				.userAgent(this.userAgent)
				.userAuthorizedGroupName(this.userAuthorizedGroupName)
				.build();
	}

	public AuditPayload getLegalTagStatusJobEventSuccess(List<String> resources, List<String> requiredGroupsForAction) {
		return AuditPayload.builder()
				.action(AuditAction.JOB_RUN)
				.status(AuditStatus.SUCCESS)
				.user(this.user)
				.actionId(RUN_JOB_ACTION_ID)
				.message(RUN_JOB_MESSAGE_SUCCESS)
				.resources(resources)
				.requiredGroupsForAction(requiredGroupsForAction)
				.userIpAddress(this.userIpAddress)
				.userAgent(this.userAgent)
				.userAuthorizedGroupName(this.userAuthorizedGroupName)
				.build();
	}

	public AuditPayload getLegalTagStatusJobEventFail(List<String> resources, List<String> requiredGroupsForAction) {
		return AuditPayload.builder()
				.action(AuditAction.JOB_RUN)
				.status(AuditStatus.FAILURE)
				.user(this.user)
				.actionId(RUN_JOB_ACTION_ID)
				.message(RUN_JOB_MESSAGE_FAILURE)
				.resources(resources)
				.requiredGroupsForAction(requiredGroupsForAction)
				.userIpAddress(this.userIpAddress)
				.userAgent(this.userAgent)
				.userAuthorizedGroupName(this.userAuthorizedGroupName)
				.build();
	}

	public AuditPayload getValidateLegalTagEventSuccess(List<String> requiredGroupsForAction) {
		return AuditPayload.builder()
				.action(AuditAction.READ)
				.status(AuditStatus.SUCCESS)
				.user(this.user)
				.actionId(READ_ACTION_ID)
				.message(READ_MESSAGE_SUCCESS)
				.resources(singletonList(PROPERTY_VALUE))
				.requiredGroupsForAction(requiredGroupsForAction)
				.userIpAddress(this.userIpAddress)
				.userAgent(this.userAgent)
				.userAuthorizedGroupName(this.userAuthorizedGroupName)
				.build();
	}

	public AuditPayload getValidateLegalTagEventFail(List<String> requiredGroupsForAction) {
		return AuditPayload.builder()
				.action(AuditAction.READ)
				.status(AuditStatus.FAILURE)
				.user(this.user)
				.actionId(READ_ACTION_ID)
				.message(READ_MESSAGE_FAILURE)
				.resources(singletonList(PROPERTY_VALUE))
				.requiredGroupsForAction(requiredGroupsForAction)
				.userIpAddress(this.userIpAddress)
				.userAgent(this.userAgent)
				.userAuthorizedGroupName(this.userAuthorizedGroupName)
				.build();
	}

	public AuditPayload getLegalTagBackupEvent(String tenant, List<String> requiredGroupsForAction) {
		return AuditPayload.builder()
				.action(AuditAction.READ)
				.status(AuditStatus.SUCCESS)
				.user(this.user)
				.actionId(LEGALTAG_BACKUP_ACTION_ID)
				.message(LEGALTAG_BACKUP_ACTION_MESSAGE)
				.resources(singletonList("LegalTags backup for tenant: " + tenant))
				.requiredGroupsForAction(requiredGroupsForAction)
				.userIpAddress(this.userIpAddress)
				.userAgent(this.userAgent)
				.userAuthorizedGroupName(this.userAuthorizedGroupName)
				.build();
	}

	public AuditPayload getLegalTagRestoreEvent(String tenant, List<String> requiredGroupsForAction) {
		return AuditPayload.builder()
				.action(AuditAction.READ)
				.status(AuditStatus.SUCCESS)
				.user(this.user)
				.actionId(LEGALTAG_RESTORE_ACTION_ID)
				.message(LEGALTAG_RESTORE_ACTION_MESSAGE)
				.resources(singletonList("LegalTags restored for tenant: " + tenant))
				.requiredGroupsForAction(requiredGroupsForAction)
				.userIpAddress(this.userIpAddress)
				.userAgent(this.userAgent)
				.userAuthorizedGroupName(this.userAuthorizedGroupName)
				.build();
	}
}
