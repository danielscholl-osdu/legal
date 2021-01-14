/*
 * Copyright 2021 Google LLC
 * Copyright 2021 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.legal.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.Properties;
import org.springframework.data.annotation.Id;

@Data
public class LegalTagMongoEntity {

  @Id
  private Long id;
  private String name;
  private String description;
  private PropertiesMongoEntity properties;
  private Boolean isValid;

  public LegalTagMongoEntity() {

  }

  public LegalTagMongoEntity(LegalTag legalTag) {
    this.id = legalTag.getId();
    this.name = legalTag.getName();
    this.description = legalTag.getDescription();
    this.properties = new PropertiesMongoEntity(legalTag.getProperties());
    this.isValid = legalTag.getIsValid();
  }

  public static LegalTag convertTo(LegalTagMongoEntity entity) {
    LegalTag legalTag = new LegalTag();

    legalTag.setId(entity.getId());
    legalTag.setProperties(PropertiesMongoEntity.convertTo(entity.getProperties()));
    legalTag.setDescription(entity.getDescription());
    legalTag.setName(entity.getName());
    legalTag.setIsValid(entity.getIsValid());

    return legalTag;
  }

  public static LegalTagMongoEntity convertFrom(LegalTag legalTag) {
    LegalTagMongoEntity entity = new LegalTagMongoEntity();

    entity.setId(legalTag.getId());
    entity.setProperties(new PropertiesMongoEntity(legalTag.getProperties()));
    entity.setDescription(legalTag.getDescription());
    entity.setName(legalTag.getName());
    entity.setIsValid(legalTag.getIsValid());

    return entity;
  }

  @Data
  private static class PropertiesMongoEntity {

    private List<String> countryOfOrigin = new ArrayList();
    private String contractId = "";
    private Date expirationDate;
    private String originator = "";
    private String dataType = "";
    private String securityClassification = "";
    private String personalData = "";
    private String exportClassification = "";

    public PropertiesMongoEntity() {
    }

    public PropertiesMongoEntity(Properties properties) {
      this.countryOfOrigin = properties.getCountryOfOrigin();
      this.contractId = properties.getContractId();
      this.expirationDate = new Date(properties.getExpirationDate().getTime());
      this.originator = properties.getOriginator();
      this.dataType = properties.getDataType();
      this.securityClassification = properties.getSecurityClassification();
      this.personalData = properties.getPersonalData();
      this.exportClassification = properties.getExportClassification();
    }

    public static Properties convertTo(PropertiesMongoEntity entity) {
      Properties properties = new Properties();

      properties.setCountryOfOrigin(entity.getCountryOfOrigin());
      properties.setContractId(entity.getContractId());

      properties.setExpirationDate(new java.sql.Date(entity.getExpirationDate().getTime()));

      properties.setOriginator(entity.getOriginator());
      properties.setDataType(entity.getDataType());
      properties.setSecurityClassification(entity.getSecurityClassification());
      properties.setPersonalData(entity.getPersonalData());
      properties.setExportClassification(entity.getExportClassification());

      return properties;
    }

    public static PropertiesMongoEntity convertFrom(Properties properties) {
      PropertiesMongoEntity entity = new PropertiesMongoEntity();

      entity.countryOfOrigin = properties.getCountryOfOrigin();
      entity.contractId = properties.getContractId();
      entity.expirationDate = new Date(properties.getExpirationDate().getTime());
      entity.originator = properties.getOriginator();
      entity.dataType = properties.getDataType();
      entity.securityClassification = properties.getSecurityClassification();
      entity.personalData = properties.getPersonalData();
      entity.exportClassification = properties.getExportClassification();

      return entity;
    }
  }
}
