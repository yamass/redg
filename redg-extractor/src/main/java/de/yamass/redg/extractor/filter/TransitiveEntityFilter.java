/*
 * Copyright Yann Massard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.yamass.redg.extractor.filter;

import de.yamass.redg.extractor.model.EntityModel;

import java.util.*;
import java.util.function.Predicate;

public class TransitiveEntityFilter implements Predicate<EntityModel> {

    private List<EntityModel> excludedEntities = new LinkedList<>();

    private Predicate<EntityModel> tester;

    public TransitiveEntityFilter(final Predicate<EntityModel> tester) {
        this.tester = tester;
    }

    @Override
    public boolean test(final EntityModel entityModel) {
        if (!tester.test(entityModel)) {
            excludedEntities.add(entityModel);
            return false;
        }
        for (EntityModel notNullModel : entityModel.getNotNullRefs()) {
            if (excludedEntities.contains(notNullModel)) {
                // This entity depends on an already excluded one, so exclude this as well
                return false;
            }
        }
        entityModel.getNullableRefs().entrySet().removeIf(entry -> excludedEntities.contains(entry.getValue()));
        return true;
    }
}
