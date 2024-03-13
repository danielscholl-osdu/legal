package org.opengroup.osdu.legal.tags;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.legal.logging.AuditLogger;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.legal.jobs.LegalTagCompliance;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTag;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagPublisher;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepositoryFactory;
import org.opengroup.osdu.legal.tags.dto.*;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.legal.tags.util.PersistenceExceptionToAppExceptionMapper;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@Service
public class LegalTagService {

    @Inject
    public PersistenceExceptionToAppExceptionMapper exceptionMapper; //public for testing purposes only

    @Inject
    private ILegalTagRepositoryFactory repositories;
    @Inject
    private LegalTagConstraintValidator validator;
    @Inject
    private AuditLogger auditLogger;
    @Inject
    private ILegalTagPublisher legalTagPublisher;
    @Inject
    private JaxRsDpsLog log;

    public LegalTagDto create(LegalTagDto legalTagDto, String tenantName) {
        if (legalTagDto == null)
            return null;
        validator.isValidThrows(legalTagDto);

        ILegalTagRepository legalTagRepository = repositories.get(tenantName);

        LegalTag legalTag = LegalTagDto.convertFrom(legalTagDto);
        String prefix = tenantName + "-";
        if (!legalTag.getName().startsWith(prefix)) {
            legalTag.setName(prefix + legalTag.getName());
        }

        validator.isValidThrows(legalTag);
        legalTag.setDefaultId();//set id based on final name
        legalTag.setIsValid(true);
        exceptionMapper.run(legalTagRepository::create, legalTag, "Error creating LegalTag.");

        auditLogger.createdLegalTagSuccess(singletonList(legalTag.toString()));

        return LegalTagDto.convertTo(legalTag);
    }

    public Boolean delete(String projectId, String name, DpsHeaders requestHeaders, String tenantName) {
        if (Strings.isNullOrEmpty(name) || requestHeaders == null)
            return false;

        LegalTag legalTag = getLegalTag(name, tenantName);
        if (legalTag == null)
            return true;
        ILegalTagRepository legalTagRepository = repositories.get(tenantName);
        Boolean result = exceptionMapper.run(legalTagRepository::delete, legalTag, "Error deleting LegalTag.");
        if (result) {
            publishMessageToPubSubOnDeletion(projectId, legalTag, requestHeaders);
            auditLogger.deletedLegalTagSuccess(singletonList(legalTag.toString()));
        }
        return result;
    }

    public LegalTagDto get(String name, String tenantName) {
        if (Strings.isNullOrEmpty(name))
            return null;

        LegalTagDtos tags = getBatch(new String[]{name}, tenantName);
        if (tags == null || tags.getLegalTags() == null || tags.getLegalTags().isEmpty())
            return null;
        else{
            auditLogger.readLegalTagSuccess(Collections.singletonList(name));
            return Iterables.get(tags.getLegalTags(), 0);
        }
    }

    public Collection<LegalTag> listLegalTag(boolean valid, String tenantName) {
        ILegalTagRepository legalTagRepository = repositories.get(tenantName);
        ListLegalTagArgs args = new ListLegalTagArgs();
        args.setIsValid(valid);
        return exceptionMapper.run(legalTagRepository::list, args, "Error retrieving LegalTag(s).");
    }

    public LegalTagDtos list(boolean valid, String tenantName) {
        Collection<LegalTag> tags = listLegalTag(valid, tenantName);
        LegalTagDtos outputs = legalTagsToReadableLegalTags(tags);
        List<String> names = outputs.getLegalTags().stream().map(x -> x.getName()).collect(Collectors.toList());
        auditLogger.readLegalTagSuccess(names);
        return outputs;
    }

    public LegalTagDtos getBatch(String[] names, String tenantName) {
        if (names == null)
            return null;

        Collection<LegalTag> legalTags = getLegalTags(names, tenantName);
        auditLogger.readLegalTagSuccess(Collections.singletonList(String.join(", ", names)));
        return legalTagsToReadableLegalTags(legalTags);
    }

    public InvalidTagsWithReason validate(String[] names, String tenantName) {
        List<InvalidTagWithReason> invalidTagsWithReason = new ArrayList<>();

        if (names == null || names.length == 0){
            auditLogger.validateLegalTagSuccess();
            return new InvalidTagsWithReason(invalidTagsWithReason);
        }

        List<String> notFoundNames = new ArrayList<>(asList(names));

        Collection<LegalTag> legalTags = getLegalTags(names, tenantName);
        if (legalTags == null || legalTags.size() == 0) {
            for (String name : names) generateInvalidTagsWithReason(invalidTagsWithReason, name, "LegalTag not found");
            return new InvalidTagsWithReason(invalidTagsWithReason);
        }

        for (LegalTag tag : legalTags) {
            notFoundNames.remove(tag.getName());
            String errors = validator.getErrors(tag);
            if (errors != null)
                generateInvalidTagsWithReason(invalidTagsWithReason, tag.getName(), errors);
        }

        if (notFoundNames.size() > 0) {
            for (String notFoundName : notFoundNames)
                generateInvalidTagsWithReason(invalidTagsWithReason, notFoundName, "LegalTag not found");
        }

        auditLogger.validateLegalTagSuccess();

        return new InvalidTagsWithReason(invalidTagsWithReason);
    }

    public LegalTagDto update(UpdateLegalTag newLegalTag, String tenantName) {
        if (newLegalTag == null)
            return null;

        LegalTag currentLegalTag = getLegalTag(newLegalTag.getName(), tenantName);

        if (currentLegalTag == null)
            throw AppException.legalTagDoesNotExistError(newLegalTag.getName());

        currentLegalTag.getProperties().setContractId(newLegalTag.getContractId());
        currentLegalTag.getProperties().setExpirationDate(newLegalTag.getExpirationDate());
        currentLegalTag.getProperties().setExtensionProperties(newLegalTag.getExtensionProperties());
        currentLegalTag.setDescription(newLegalTag.getDescription());

        validator.isValidThrows(currentLegalTag);

        auditLogger.updatedLegalTagSuccess(Collections.singletonList(currentLegalTag.toString()));

        return update(currentLegalTag, tenantName);
    }

    public LegalTagDto updateStatus(String legalTagName, Boolean isValid, String tenantName) {
        if (legalTagName == null)
            return null;

        LegalTag currentLegalTag = getLegalTag(legalTagName, tenantName);

        if (currentLegalTag == null)
            throw AppException.legalTagDoesNotExistError(legalTagName);

        currentLegalTag.setIsValid(isValid);
        return update(currentLegalTag, tenantName);
    }

    private LegalTagDto update(LegalTag currentLegalTag, String tenantName) {
        ILegalTagRepository legalTagRepository = repositories.get(tenantName);
        LegalTag output = exceptionMapper.run(legalTagRepository::update, currentLegalTag, "error");

        if (output == null)
            return null;
        auditLogger.updatedLegalTagSuccess(singletonList(currentLegalTag.toString()));
        return LegalTagDto.convertTo(output);
    }

    private LegalTagDtos legalTagsToReadableLegalTags(Collection<LegalTag> legalTags) {
        if (legalTags == null || legalTags.isEmpty())
            return new LegalTagDtos();

        List<LegalTagDto> convertedTags = new ArrayList<>();
        for (LegalTag tag : legalTags) {
            if (tag == null) {
                continue;
            }
            convertedTags.add(LegalTagDto.convertTo(tag));
        }
        LegalTagDtos output = new LegalTagDtos();
        output.setLegalTags(convertedTags);
        return output;
    }

    private Collection<LegalTag> getLegalTags(String[] names, String tenantName) {
        long[] ids = new long[names.length];
        String prefix = tenantName + "-";
        for (int i = 0; i < ids.length; i++) {
            var legalTag = names[i];
            if (!legalTag.startsWith(prefix)) {
                legalTag = prefix + legalTag;
            }
            ids[i] = LegalTag.getDefaultId(legalTag);
        }
        ILegalTagRepository legalTagRepository = repositories.get(tenantName);
        return exceptionMapper.run(legalTagRepository::get, ids, "Error retrieving LegalTag(s).");
    }

    private LegalTag getLegalTag(String name, String tenantName) {
        Collection<LegalTag> output = getLegalTags(new String[]{name}, tenantName);
        return output == null || output.size() == 0 ? null : Iterables.get(output, 0);
    }

    private void generateInvalidTagsWithReason(List<InvalidTagWithReason> invalidTagsWithReason, String name, String reason) {
        invalidTagsWithReason.add(new InvalidTagWithReason(name, reason));
    }

    private void publishMessageToPubSubOnDeletion(String projectId, LegalTag legalTag, DpsHeaders headers) {
        StatusChangedTags statusChangedTags = new StatusChangedTags();
        statusChangedTags.getStatusChangedTags().add(new StatusChangedTag(legalTag.getName(), LegalTagCompliance.incompliant));
        try {
            legalTagPublisher.publish(projectId, headers, statusChangedTags);
        } catch (Exception e) {
            log.error("Error when publishing legaltag status change to pubsub", e);
        }
    }
}
