// Copyright © Amazon Web Services
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.legal.util;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import org.opengroup.osdu.core.common.model.legal.Properties;


@DynamoDBTable(tableName = "LegalRepository") // DynamoDB table name (without environment prefix)
public class LegalDoc {

    @DynamoDBHashKey(attributeName = "Id")
    private String id;

    @DynamoDBRangeKey(attributeName = "dataPartitionId")
    private String dataPartitionId;

    @DynamoDBAttribute(attributeName = "Name")
    private String name;

    @DynamoDBAttribute(attributeName = "Description")
    private String description;

    @DynamoDBTypeConverted(converter = PropertiesTypeConverter.class)
    @DynamoDBAttribute(attributeName = "Properties")
    private Properties properties;

    @DynamoDBAttribute(attributeName = "IsValid")
    private boolean isValid;

    // setters and getters, avoiding lombok to reduce code dependencies
    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getDataPartitionId(){
        return dataPartitionId;
    }

    public void setDataPartitionId(String tenant){
        this.dataPartitionId = tenant;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }

    public String getName(){
        return name;
    }
}

