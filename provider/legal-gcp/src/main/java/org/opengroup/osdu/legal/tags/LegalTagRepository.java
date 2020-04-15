//  Copyright © Amazon Web Services
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

package org.opengroup.osdu.legal.tags;

import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class LegalTagRepository implements ILegalTagRepository {

    @Override
    public Long create(LegalTag legalTag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<LegalTag> get(long[] ids) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean delete(LegalTag legalTag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LegalTag update(LegalTag newLegalTag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<LegalTag> list(ListLegalTagArgs args) {
        throw new UnsupportedOperationException();
    }
}
