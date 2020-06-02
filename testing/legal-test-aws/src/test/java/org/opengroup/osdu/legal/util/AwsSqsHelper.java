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

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

import java.util.List;

public class AwsSqsHelper {
    public static List<Message> getMessages(){
        String amazonSqsEndpoint = System.getenv("LEGAL_QUEUE");
        AmazonSQS sqs = AmazonSQSClientBuilder.standard().withRegion("us-east-1").build();
        List<Message> messages = sqs.receiveMessage(amazonSqsEndpoint).getMessages();
        return messages;
    }

    public static void purgeQueue(){
        String amazonSqsEndpoint = System.getenv("LEGAL_QUEUE");;
        AmazonSQS sqs = AmazonSQSClientBuilder.standard().withRegion("us-east-1").build();
        List<Message> messages = sqs.receiveMessage(amazonSqsEndpoint).getMessages();
        PurgeQueueRequest request = new PurgeQueueRequest();
        request.setQueueUrl(amazonSqsEndpoint);
        sqs.purgeQueue(request);
    }

    public static boolean checkLegalTagNameSent(Message message, String name) throws Exception {
        if(message == null)
            return false;
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(message.getBody());
        String data = root.path("Message").toString();
        // comes wrapped in non-escaped double quotes
        data = data.substring(1);
        data = data.substring(0, data.length() - 1);
        String dataCheck = "{\"statusChangedTags\":[{\"changedTagName\":\"" + name + "\",\"changedTagStatus\":\"incompliant\"}]}";
        dataCheck = dataCheck.replaceAll("\"", "\\\\\"");
        return data.equals(dataCheck);
    }
}
