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

package org.opengroup.osdu.legal.aws.tags.dataaccess;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opengroup.osdu.core.common.model.legal.Properties;

import java.io.IOException;

public class PropertiesTypeConverter implements DynamoDBTypeConverter<String, Properties> {
    @Override
    public String convert(Properties properties) {
        String propString = "";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            propString = objectMapper.writeValueAsString(properties);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return propString;
    }

    @Override
    public Properties unconvert(String propString) {
        Properties properties = new Properties();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            properties = objectMapper.readValue(propString, new TypeReference<Properties>(){});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}