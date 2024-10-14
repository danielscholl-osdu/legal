/**
* Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*      http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.opengroup.osdu.legal.aws.cache;

import org.opengroup.osdu.core.aws.cache.CacheParameters;
import org.opengroup.osdu.core.aws.cache.NameSpacedCache;
import org.opengroup.osdu.core.aws.cache.DefaultCache;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GroupCache<K,V> extends DefaultCache<K,V> {
    static final String KEY_NAMESPACE = "groupCache";

    // overloaded constructor for testing
    public GroupCache(
        @Value("${aws.elasticache.cluster.endpoint:null}") String redisEndpoint,
        @Value("${aws.elasticache.cluster.port:null}") String redisPort,
        @Value("${aws.elasticache.cluster.key:null}") String redisPassword
    ) {
        super((ICache<K, V>) new NameSpacedCache<>(CacheParameters.<String, V>builder()
                                                                  .expTimeSeconds(60)
                                                                  .maxSize(10)
                                                                  .defaultHost(redisEndpoint)
                                                                  .defaultPort(redisPort)
                                                                  .defaultPassword(redisPassword)
                                                                  .keyNamespace(KEY_NAMESPACE)
                                                                  .build()
                                                                  .initFromLocalParameters(String.class, (Class<V>) Groups.class)));
    }


    public GroupCache() {
        this(null, null, null);
    }
}
