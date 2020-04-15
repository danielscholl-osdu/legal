//  Copyright © Microsoft Corporation
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

package org.opengroup.osdu.legal.byoc.tags.dataaccess;

import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class LegalTagRepositoryImpl implements ILegalTagRepository {
    private Map<Long, LegalTag> memMap = new HashMap<>();

    @Override
    public Long create(LegalTag legalTag) {
        Long id = -1L;

        if(legalTag != null) {
            memMap.put(legalTag.getId(), legalTag);
            id = legalTag.getId();
        }
        return id;
    }

    @Override
    public Collection<LegalTag> get(long[] ids) {
        List<LegalTag> output = new ArrayList<>();

        if(ids != null && ids.length > 0) {
            for(long id : ids)
            {
                if (memMap.containsKey(id))
                    output.add(memMap.get(id));
            }
        }
        return output;
    }

    @Override
    public Boolean delete(LegalTag legalTag) {
        boolean removed = false;

        Long id = legalTag.getId();
        if (memMap.containsKey(id)) {
            memMap.remove(id);
            removed = true;
        }

        return removed;
    }

    @Override
    public LegalTag update(LegalTag newLegalTag) {
        if(newLegalTag == null)
            return null;

        Long id = newLegalTag.getId();
        LegalTag currentLegalTag = memMap.get(id);
        if (currentLegalTag == null)
            throw AppException.legalTagDoesNotExistError(newLegalTag.getName());

        memMap.remove(id);
        memMap.put(id, newLegalTag);

        return newLegalTag;
    }

    @Override
    public Collection<LegalTag> list(ListLegalTagArgs args) {
        List<LegalTag> output = new ArrayList<>();

        for (LegalTag legalTag : memMap.values()) {
            if (legalTag.getIsValid() == args.getIsValid())
                output.add(legalTag);
        }

        return output;
    }
}
