package org.opengroup.osdu.legal.tags;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.*;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.legal.logging.AuditLogger;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.legal.jobs.LegalTagCompliance;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagPublisher;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepositoryFactory;
import org.opengroup.osdu.legal.tags.dto.*;
import org.opengroup.osdu.legal.tags.dto.InvalidTagWithReason;
import org.opengroup.osdu.legal.tags.dto.InvalidTagsWithReason;
import org.opengroup.osdu.legal.tags.util.PersistenceExceptionToAppExceptionMapper;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import javax.inject.Inject;

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

    public LegalTagDtos searchLegalTag(String searchQuery, boolean valid, TenantInfo tenantInfo) {
        log.info("input search query " + searchQuery);
        Collection<LegalTag> legalTags = listLegalTag(valid, tenantInfo.getName());

        log.info("list of legal tags retrieved... " + legalTags.size());

        //everything down here should be handled in a different method
        LegalTagDtos outputs;

        Collection<LegalTag> matchedTags = parseInputAndSearch(searchQuery, legalTags);

        log.info("number of legaltags matched with input criteria " + matchedTags.size());


        outputs = legalTagsToReadableLegalTags(matchedTags);
        List<String> names = outputs.getLegalTags().stream().map(x -> x.getName()).collect(Collectors.toList());
        auditLogger.readLegalTagSuccess(names);



        //LegalTag.class.getFields();
        //legalTags.forEach();
        return outputs;
    }

    private Collection<LegalTag> parseInputAndSearch(String searchQuery, Collection<LegalTag> legalTags) {

        ObjectMapper objectMapper = new ObjectMapper();

        Collection<LegalTag> matchedTags = new ArrayList<LegalTag>();


        try {
            Map<String, Object> readInputMap = objectMapper.readValue(searchQuery, new TypeReference<Map<String, Object>>() {});

            List<String> queryList = (List<String>) readInputMap.get("query");

            log.info("input query list size = " + queryList.size());

            Iterator<String> iterator = queryList.listIterator();
            String firstQuery = null;

            while (iterator.hasNext())
            {
                firstQuery = iterator.next();
            }

            StringTokenizer st = null;
            String attribute = null;
            String pattern = null;
            String searchedString = null;
            String fromDate = null;
            String toDate = null;
            LocalDate fromlocalDate = null;
            LocalDate toLocalDate = null;
            boolean matchedTag = false;
            LocalDate expirationDate = null;




            Iterator<LegalTag> legaliterator = legalTags.iterator();

            if(firstQuery.contains("=")) {
                st = new StringTokenizer(firstQuery, "=");
                int tokens = st.countTokens();
                if(tokens == 2)
                {
                    attribute = st.nextToken().trim();
                    pattern = st.nextToken().trim();
                }

                log.info("attribute is " + attribute);
                log.info("pattern is " + pattern);


            }
            if(firstQuery.contains("("))
            {
                searchedString = firstQuery.substring(firstQuery.indexOf("(") + 1,firstQuery.lastIndexOf(')'));
                if(searchedString.contains(","))
                {
                    st = new StringTokenizer(searchedString, ",");
                    fromDate = st.nextToken().trim();
                    toDate = st.nextToken().trim();
                }
                log.info("fromDate is " + fromDate);
                log.info("toDate is " + toDate);

                fromlocalDate = LocalDate.parse(fromDate);
                toLocalDate = LocalDate.parse(toDate);

                log.info("after parsing fromDate is " + fromlocalDate);
                log.info("after parsing toDate is " + toLocalDate);


                /*LocalDate dateToValidate = LocalDate.now();


*/
            }






            while (null != legaliterator && legaliterator.hasNext())
            {
                LegalTag oneLegalTag = legaliterator.next();
                if(firstQuery.contains("=")){
                    matchedTag = searchInLegalTag(attribute, pattern, oneLegalTag);
                }
                if(firstQuery.contains("(")){
                    expirationDate = oneLegalTag.getProperties().getExpirationDate().toLocalDate();
                    matchedTag = expirationDate.isAfter(fromlocalDate) && expirationDate.isBefore(toLocalDate) ? true : false;
                    log.info("does expiration is between " + fromlocalDate + " and " + toDate + " dates? " + matchedTag);
                }

                log.info("match found? " + matchedTag);
                if(matchedTag)
                {
                    matchedTags.add(oneLegalTag);
                    log.info("added to matched tag list");

                }


            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            log.error("JSON exception");
        }
        return matchedTags;


    }

    public boolean searchInLegalTag(String attribute, String pattern, LegalTag legalTag) {

        log.info("Inside searchInLegalTag");

        log.info("attribute searching for " + attribute);
        log.info("pattern searching for " + pattern);
        //boolean matchingTag = false;


        /*if(null != pattern)
            pattern.toLowerCase();*/




        Map<String, Object> extensionPropertiesMap;


        boolean fieldExists;
        boolean matchFound = false;

        Object value;

        //Alternate approach using BeanWrapper
        {
            BeanWrapper beanWrapper = new BeanWrapperImpl(legalTag);

            if (beanWrapper.isReadableProperty(attribute)) {
                log.info("attribute exists in LegalTag.class");
                value = beanWrapper.getPropertyValue(attribute);
                System.out.println("Field value: " + value);
                log.info("value of " + attribute + " is " + value);
                if (value instanceof String) {
                    log.info("checking to see if value is a string object");
                    //if (value.toString().toLowerCase().contains(pattern)) {
                    if (StringUtils.containsAnyIgnoreCase(value.toString(), pattern)) {
                        matchFound = true;
                        log.info("value match pattern " + matchFound);
                        return matchFound;
                    }
                }

            } else {
                //System.out.println("Field '" + fieldName + "' not found or not accessible in legaltag.");
                log.info("checking in Properties");
                Properties findInProperties = legalTag.getProperties();
                beanWrapper = new BeanWrapperImpl(findInProperties);
                if (beanWrapper.isReadableProperty(attribute)) {
                    log.info("is it in properties?");
                    switch (attribute){
                        case "countryOfOrigin":
                            List<String> countryList = findInProperties.getCountryOfOrigin();
                            matchFound = countryList.stream().anyMatch(pattern::equalsIgnoreCase);
                            log.info("countryOfOrigin matched ? " + matchFound);
                            break;
                        case "expirationDate":
                            Date expirationDate = findInProperties.getExpirationDate();
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
                            String strDate = dateFormat.format(expirationDate);
                            log.info("Converted expirationDate: " + strDate);
                            if (StringUtils.contains(strDate, pattern)) {
                                matchFound = true;
                                log.info("does expirationDate match pattern " + matchFound);
                                return matchFound;
                            }
                            break;
                        default:
                            value = beanWrapper.getPropertyValue(attribute);
                            log.info("value of " + attribute + " is " + value + " in Properties");
                            if (value instanceof String) {
                                log.info("checking to see if value is a string object");
                                if (StringUtils.containsAnyIgnoreCase(value.toString(), pattern)) {
                                    matchFound = true;
                                    log.info("does value match pattern " + matchFound);
                                    return matchFound;
                                }
                            }
                    }


                } else {
                    log.info("attribute in extensionproperties? ");
                    //extensionPropertiesMap = findInProperties.getExtensionProperties();
                    //matchFound = matchInExtensionProperties(attribute, pattern, extensionPropertiesMap);


                    //well, this part of the code involves converting the hashmap to a JSON Object and then searching for the pattern
                    JSONObject jsonObject = new JSONObject(findInProperties.getExtensionProperties());
                    matchFound = matchInExtensionPropertiesJSON(attribute, pattern, jsonObject);
                    log.info("did it match in extensionproperties? " + matchFound);
                }
            }
        }


        /*try {
            //fields = Class.forName(String.valueOf(LegalTag.class)).getDeclaredFields();
            fields = LegalTag.class.getDeclaredFields();
            Method getMethod = null;

            //Arrays.asList(fields).stream().forEach(s -> System.out.println("field name " + s));

        /* check to see if attribute belong to Legaltag.class */
            /*fieldExists = Arrays.stream(fields).map(Field::getName).anyMatch(f -> f.equalsIgnoreCase(attribute));

            System.out.println("check to see if in LegalTag " + fieldExists);
            if(fieldExists)
            {


                getMethod = LegalTag.class.getMethod("get" + StringUtils.capitalize(attribute));

                fieldVal = (String) getMethod.invoke(legalTag);
                if(fieldVal.contains(pattern))
                {
                    matchFound = true;
                    return matchFound;
                }

                //System.out.println(result);

                //LegalTag.class.getDeclaredField(attribute);

            }
            else
            {
                //fields = Properties.class.getDeclaredFields();
                Properties findInProperties = legalTag.getProperties();
                fields = findInProperties.getClass().getDeclaredFields();
                fieldExists = Arrays.stream(fields).map(Field::getName).anyMatch(f -> f.equalsIgnoreCase(attribute));
                System.out.println("check to see if in Properties " + fieldExists);

                if(fieldExists)
                {
                    getMethod = findInProperties.getClass().getMethod("get" + StringUtils.capitalize(attribute));

                    fieldVal = (String) getMethod.invoke(findInProperties);
                    if(fieldVal.contains(pattern))
                    {
                        matchFound = true;
                        return matchFound;
                    }
                }

                else
                {
                    extensionPropertiesMap = findInProperties.getExtensionProperties();
                    matchFound = matchInExtensionProperties(attribute, pattern, extensionPropertiesMap);

                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }*/



        return matchFound;
    }

    private boolean matchInExtensionPropertiesJSON(String attribute, String pattern, JSONObject jsonObject) {
        {

            //System.out.println("Method entry " + jsonObject);
            boolean didItMatch = false;

            if (didItMatch) {
                return didItMatch;
            }


            String key = null;
            Object value = null;
            //boolean matchFound = false;
            if (null != jsonObject && !didItMatch) {
                Iterator<String> jsonKeyIter = jsonObject.keySet().iterator();
                while (null != jsonKeyIter && jsonKeyIter.hasNext()) {
                    key = jsonKeyIter.next();

                    log.info("key is " + key);


                    if (jsonObject.get(key) instanceof JSONObject) {
                        JSONObject nestedJson = (JSONObject) jsonObject.get(key);
                        didItMatch = matchInExtensionPropertiesJSON(attribute, pattern, nestedJson);
                    } else if (jsonObject.get(key) instanceof String) {
                        value = (String) jsonObject.get(key);

                        System.out.println("value is  " + value + " instance of String");

                        if (key.equalsIgnoreCase(attribute)) {
                            if (StringUtils.containsAnyIgnoreCase(value.toString(), pattern)) {
                                System.out.println("match found");
                                didItMatch = true;
                                return didItMatch;

                            }
                        }
                    } else if (jsonObject.get(key) instanceof JSONArray) {
                        JSONArray jsonArray = (JSONArray) jsonObject.get(key);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            if (jsonArray.get(i) instanceof JSONObject) {
                                JSONObject newJsonObj = (JSONObject) jsonArray.get(i);
                                didItMatch = matchInExtensionPropertiesJSON(attribute, pattern, newJsonObj);
                            }
                            if (jsonArray.get(i) instanceof String) {
                                value = jsonArray.get(i);
                                System.out.println("value is  " + value + " instance of String");

                                if (key.equalsIgnoreCase(attribute)) {
                                    if (StringUtils.containsAnyIgnoreCase(value.toString(), pattern)) {
                                        System.out.println("match found");
                                        didItMatch = true;
                                        return didItMatch;

                                    }
                                }


                            }
                            if (didItMatch)
                                return didItMatch;
                        }
                    } else if (jsonObject.get(key).toString().equalsIgnoreCase("true") || jsonObject.get(key).toString().equalsIgnoreCase("false")) {
                        value = (Boolean) jsonObject.get(key);
                        System.out.println("value is  " + value);
                        if ((key.equalsIgnoreCase(attribute) && StringUtils.containsAnyIgnoreCase(value.toString(), pattern))) {
                            System.out.println("match found");
                            //didItMatch =true;
                            return true;

                        }

                    }


                }

            }


            //System.out.println("matchFound " + matchFound);
            System.out.println("record matched? " + didItMatch);
            //return matchFound;
            return didItMatch;


        }
    }

    private boolean matchInExtensionProperties(String attribute, String pattern, Map<String, Object> extensionPropertiesMap) {

        boolean didItMatch = false;

        String key = null;
        Object value = null;





        //this part of the code handles key/value match reading from hashmap itself
        if (null != extensionPropertiesMap && !didItMatch) {
            Iterator<String> mapItr = extensionPropertiesMap.keySet().iterator();
            while (null != mapItr && mapItr.hasNext()) {
                key = mapItr.next();

                //System.out.println("key is " + key);


                //if(legalTagJson.get(key) instanceof JSONObject)
                if (extensionPropertiesMap.get(key).toString().startsWith("{")) {
                    System.out.println("value contains nested object");
                    Map<String, Object> nestedMap = (Map<String, Object>) extensionPropertiesMap.get(key);
                    didItMatch = matchInExtensionProperties(attribute, pattern, nestedMap);
                } else if (extensionPropertiesMap.get(key) instanceof String) {
                    value = (String) extensionPropertiesMap.get(key);

                    System.out.println("value is  " + value + " instance of String");

                    if (key.equalsIgnoreCase(attribute)) {
                        if (value.toString().contains(pattern)) {
                            //System.out.println("match found");
                            didItMatch = true;
                            return true;

                        }
                    }
                }
                //else if(legalTagJson.get(key) instanceof  JSONArray)
                else if (extensionPropertiesMap.get(key).toString().startsWith("[")) {
                    System.out.println("value instance of a list");
                    List<Map<String, Object>> listOfNestedArray = (List<Map<String, Object>>) extensionPropertiesMap.get(key);
                    ListIterator<Map<String, Object>> li = listOfNestedArray.listIterator();
                    while (li.hasNext()) {
                        Map<String, Object> innerMap = li.next();
                        didItMatch = matchInExtensionProperties(attribute, pattern, innerMap);
                    }


                    }
                if(didItMatch)
                    return true;




            }

        }


        return didItMatch;
    }
}
