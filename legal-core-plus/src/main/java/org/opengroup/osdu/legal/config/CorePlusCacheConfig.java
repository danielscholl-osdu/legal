/*
 *  Copyright 2020-2024 Google LLC
 *  Copyright 2020-2024 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.legal.config;

import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.cache.VmCache;
import org.opengroup.osdu.core.common.partition.PartitionInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CorePlusCacheConfig {

  @Bean
  public ICache<String, PartitionInfo> partitionInfoCache() {
    return new VmCache<>(600, 2000);
  }
}
