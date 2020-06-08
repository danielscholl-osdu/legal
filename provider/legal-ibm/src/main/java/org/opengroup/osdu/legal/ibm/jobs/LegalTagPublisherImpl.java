//  Copyright 2020 IBM Corp. All Rights Reserved
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package org.opengroup.osdu.legal.ibm.jobs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTag;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.core.ibm.messagebus.IMessageFactory;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagPublisher;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import net.minidev.json.JSONObject;

@Component
public class LegalTagPublisherImpl implements ILegalTagPublisher {
	
	@Inject
	IMessageFactory mq;

	@Override
	public void publish(String projectId, DpsHeaders headers, StatusChangedTags tags) throws Exception {

		final int BATCH_SIZE = 50;
		Gson gson = new Gson();
		Map<String, String> message = new HashMap<>();
		
		for (int i = 0; i < tags.getStatusChangedTags().size(); i += BATCH_SIZE) {
			
			List<StatusChangedTag> batch = tags.getStatusChangedTags().subList(i,
					Math.min(tags.getStatusChangedTags().size(), i + BATCH_SIZE));
			JSONObject statusChangedTags = new JSONObject();
			statusChangedTags.appendField("statusChangedTags", batch);
			String json = gson.toJson(statusChangedTags);
			message.put("data", json);
			message.put(DpsHeaders.ACCOUNT_ID, headers.getPartitionIdWithFallbackToAccountId());
			message.put(DpsHeaders.DATA_PARTITION_ID, headers.getPartitionIdWithFallbackToAccountId());
			headers.addCorrelationIdIfMissing();
			message.put(DpsHeaders.CORRELATION_ID, headers.getCorrelationId());
			mq.sendMessage(IMessageFactory.LEGAL_QUEUE_NAME, gson.toJson(message));
			// TODO discover where this message is consumed!

		}

	}
}
