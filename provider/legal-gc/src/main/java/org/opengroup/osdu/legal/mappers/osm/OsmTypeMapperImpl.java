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
package org.opengroup.osdu.legal.mappers.osm;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Key;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.gcp.osm.persistence.IdentityTranslator;
import org.opengroup.osdu.core.gcp.osm.translate.Instrumentation;
import org.opengroup.osdu.core.gcp.osm.translate.TypeMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * All Entity classes used in translation should be registered here even if don't need custom
 * settings. Each class is represented as Instrumentation object. At least class objects and rules
 * for Identity translation should be provided.oi
 */
@Component
@Scope(SCOPE_SINGLETON)
@ConditionalOnProperty(name = "osmDriver")
public class OsmTypeMapperImpl extends TypeMapper {

  public OsmTypeMapperImpl() {
    super(Arrays.asList(
        new Instrumentation<>(LegalTag.class,
            new HashMap<String, String>() {{
              put("countryOfOrigin", "COO");
              put("createdDate", "created");
            }},
            new HashMap<String, Class<?>>() {{
              put("created", Timestamp.class);
              put("expirationDate", Timestamp.class);
              put("isValid", Boolean.class);
            }},
            new IdentityTranslator<>(LegalTag::getId,
                (r, o) -> r.setId(((Key) o).getId())),
            Collections.singletonList("id"))));
  }
}
