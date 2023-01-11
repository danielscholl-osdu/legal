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
package org.opengroup.osdu.legal.jobs;

import static java.util.Arrays.asList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.core.gcp.oqm.driver.OqmDriver;
import org.opengroup.osdu.core.gcp.oqm.model.OqmDestination;
import org.opengroup.osdu.core.gcp.oqm.model.OqmMessage;
import org.opengroup.osdu.core.gcp.oqm.model.OqmTopic;
import org.opengroup.osdu.legal.config.GcpAppServiceConfig;
import org.opengroup.osdu.legal.logging.AuditLogger;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LegalTagPublisherImpl implements ILegalTagPublisher {

  private final AuditLogger auditLogger;
  private final GcpAppServiceConfig config;
  private final OqmDriver driver;

  private OqmTopic oqmTopic = null;

  @PostConstruct
  void postConstruct() {
    oqmTopic = OqmTopic.builder().name(config.getPubSubLegalTagsTopic()).build();
  }

  public void publish(String projectId, DpsHeaders headers, StatusChangedTags statusChangedTags)
      throws Exception {

    OqmDestination oqmDestination = OqmDestination.builder().partitionId(headers.getPartitionId())
        .build();

    String json = generatePubSubMessage(statusChangedTags);

    Map<String, String> attributes = new HashMap<>();
    attributes.put(DpsHeaders.ACCOUNT_ID, headers.getPartitionIdWithFallbackToAccountId());
    attributes.put(DpsHeaders.DATA_PARTITION_ID, headers.getPartitionIdWithFallbackToAccountId());
    headers.addCorrelationIdIfMissing();
    attributes.put(DpsHeaders.CORRELATION_ID, headers.getCorrelationId());

    OqmMessage oqmMessage = OqmMessage.builder().data(json).attributes(attributes).build();

    driver.publish(oqmMessage, oqmTopic, oqmDestination);

    auditLogger.publishedStatusChangeSuccess(
        asList(Long.toString(Instant.now().getEpochSecond()), statusChangedTags.toString()));

  }

  private String generatePubSubMessage(StatusChangedTags statusChangedTags) {
    Gson gson = new GsonBuilder().create();
    JsonElement statusChangedTagsJson = gson.toJsonTree(statusChangedTags, StatusChangedTags.class);

    return statusChangedTagsJson.toString();
  }
}
