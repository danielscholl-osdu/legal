package org.opengroup.osdu.legal.api;

import com.google.gson.Gson;
import org.opengroup.osdu.legal.countries.LegalTagCountriesService;
import org.opengroup.osdu.legal.logging.AuditLogger;
import org.opengroup.osdu.legal.tags.LegalTagService;
import org.opengroup.osdu.legal.tags.dto.*;
import org.opengroup.osdu.core.common.model.legal.AllowedLegaltagPropertyValues;
import org.opengroup.osdu.core.common.model.legal.validation.ValidName;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.opengroup.osdu.core.common.model.legal.ServiceConfig;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.inject.Inject;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path= "/")
@Validated
public class LegalTagApi {

    private RequestInfo requestInfo;
    @Inject //injectMock only works on setter DI
    public void setRequestInfo(RequestInfo requestInfo)
    {
        this.requestInfo = requestInfo;
    }

    private LegalTagService legalTagService;
    @Inject //injectMock only works on setter DI
    public void setLegalTagService(LegalTagService legalTagService)
    {
        this.legalTagService = legalTagService;
    }

    @Inject
    private AllowedLegaltagPropertyValues allowedLegaltagPropertyValues;

    @Inject
    private AuditLogger auditLogger;
    
    @Inject
    private LegalTagCountriesService legalTagCountriesService;

    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_EDITOR + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @PostMapping("/legaltags")
    public ResponseEntity<LegalTagDto> createLegalTag(@NotNull @RequestBody LegalTagDto legalTag) {
        LegalTagDto output = legalTagService.create(legalTag, requestInfo.getTenantInfo().getName());
        return new ResponseEntity<LegalTagDto>(output, HttpStatus.CREATED);
    }

    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_ADMIN + "')")
    @DeleteMapping("/legaltags/{name}")
    public ResponseEntity<HttpStatus> deleteLegalTag(@PathVariable("name") @ValidName String name) {
        if (legalTagService.delete(requestInfo.getTenantInfo().getProjectId(), name, requestInfo.getHeaders(), requestInfo.getTenantInfo().getName()))
            return new ResponseEntity<HttpStatus>(HttpStatus.NO_CONTENT);
        else
            return new ResponseEntity<HttpStatus>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_USER + "', '" + ServiceConfig.LEGAL_EDITOR + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @GetMapping("/legaltags/{name}")
    public ResponseEntity getLegalTag(@PathVariable("name") @ValidName String name) {
        LegalTagDto output = legalTagService.get(name, requestInfo.getTenantInfo().getName());

        if (output == null)
            return new ResponseEntity<>(createNotFoundBody(), HttpStatus.NOT_FOUND);
        else {
            return new ResponseEntity<LegalTagDto>(output, HttpStatus.OK);
        }
    }

    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_USER + "', '" + ServiceConfig.LEGAL_EDITOR + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @GetMapping("/legaltags")
    public ResponseEntity<LegalTagDtos> listLegalTags(@RequestParam(name = "valid", required = false, defaultValue = "true") boolean valid) {
    	if (requestInfo.getTenantInfo() == null) {
    		throw new ValidationException("No tenant supplied");
    	}
        LegalTagDtos output = legalTagService.list(valid, requestInfo.getTenantInfo().getName());
        return new ResponseEntity<LegalTagDtos>(output, HttpStatus.OK);
    }

    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_EDITOR + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @PutMapping("/legaltags")
    public ResponseEntity<LegalTagDto> updateLegalTag(@Valid @NotNull @RequestBody UpdateLegalTag legalTag) {
        LegalTagDto output = legalTagService.update(legalTag, requestInfo.getTenantInfo().getName());
        return new ResponseEntity<LegalTagDto>(output, HttpStatus.OK);
    }

    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_USER + "', '" + ServiceConfig.LEGAL_EDITOR + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @PostMapping("/legaltags:batchRetrieve")
    public ResponseEntity<LegalTagDtos> getLegalTags(@Valid @NotNull @RequestBody RequestLegalTags requestedTags) {
        String[] names =  requestedTags.getNames().stream().toArray(String[]::new);
        LegalTagDtos result = legalTagService.getBatch(names, requestInfo.getTenantInfo().getName());

        if (result == null || result.getLegalTags().size() != requestedTags.getNames().size()) {
            return new ResponseEntity<LegalTagDtos>(result, HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<LegalTagDtos>(result, HttpStatus.OK);
        }
    }

    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_USER + "', '" + ServiceConfig.LEGAL_EDITOR + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @PostMapping("/legaltags:validate")
    public ResponseEntity<InvalidTagsWithReason> validateLegalTags(@Valid @NotNull @RequestBody RequestLegalTags requestedTags) {
        InvalidTagsWithReason result = legalTagService.validate(requestedTags.getNames().toArray(new String[0]),
                requestInfo.getTenantInfo().getName());

        return new ResponseEntity<InvalidTagsWithReason>(result, HttpStatus.OK);
    }

    @PreAuthorize("@authorizationFilter.hasPermission('" + ServiceConfig.LEGAL_USER + "', '" + ServiceConfig.LEGAL_EDITOR + "', '" + ServiceConfig.LEGAL_ADMIN + "')")
    @GetMapping("/legaltags:properties")
    public ResponseEntity<ReadablePropertyValues> getLegalTagProperties() {
        ReadablePropertyValues output = new ReadablePropertyValues();
        output.setCountriesOfOrigin(legalTagCountriesService.getValidCOOs());
        output.setOtherRelevantDataCountries(legalTagCountriesService.getValidORDCs());
        output.setExportClassificationControlNumbers(allowedLegaltagPropertyValues.getEccns());
        output.setPersonalDataTypes(allowedLegaltagPropertyValues.getPersonalDataType());
        output.setSecurityClassifications(allowedLegaltagPropertyValues.getSecurityClassifications());
        output.setDataTypes(allowedLegaltagPropertyValues.getDataTypes());
        auditLogger.readLegalPropertiesSuccess();

        return new ResponseEntity<ReadablePropertyValues>(output, HttpStatus.OK);
    }

    private String createNotFoundBody() {
        final Map<String, String> body = new HashMap<>();
        body.put("error", "Not found.");
        return new Gson().toJson(body);
    }
}
