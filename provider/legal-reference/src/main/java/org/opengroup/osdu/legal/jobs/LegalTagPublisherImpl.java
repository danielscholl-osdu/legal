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

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTag;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.legal.messagebus.IMessageFactory;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LegalTagPublisherImpl implements ILegalTagPublisher {

  private final IMessageFactory mq;

  @Autowired
  public LegalTagPublisherImpl(IMessageFactory mq) {
    this.mq = mq;
  }

  @Override
  public void publish(String projectId, DpsHeaders headers, StatusChangedTags tags) {

    final int BATCH_SIZE = 50;
    Gson gson = new Gson();
    Map<String, String> message = new HashMap<>();

    for (int i = 0; i < tags.getStatusChangedTags().size(); i += BATCH_SIZE) {

      List<StatusChangedTag> batch = tags.getStatusChangedTags().subList(i,
          Math.min(tags.getStatusChangedTags().size(), i + BATCH_SIZE));
      String json = gson.toJson(batch);
      message.put("data", json);
      message.put(DpsHeaders.DATA_PARTITION_ID, headers.getPartitionIdWithFallbackToAccountId());
      headers.addCorrelationIdIfMissing();
      message.put(DpsHeaders.CORRELATION_ID, headers.getCorrelationId());
      mq.sendMessage(IMessageFactory.LEGAL_QUEUE_NAME, gson.toJson(message));
    }
  }
}
