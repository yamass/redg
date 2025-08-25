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

package de.yamass.redg.extractor;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.yamass.redg.extractor.model.EntityModel;

public class EntityModelSorter {

    public static List<EntityModel> sortEntityModels(List<EntityModel> entities) {
        Map<EntityModel, Integer> depths = new DepthCalculator().calculateDepths(entities);
        return entities.stream()
                .sorted(Comparator.comparing(depths::get))
                .collect(Collectors.toList());
    }

    private static class DepthCalculator {
        Map<EntityModel, Integer> depths;

        Map<EntityModel, Integer> calculateDepths(List<EntityModel> entities) {
            depths = new HashMap<>();
            entities.forEach(this::calculateDepth);
            return depths;
        }

        private int calculateDepth(EntityModel entity) {
            // Get depth if already calculated
            if (this.depths.containsKey(entity)) {
                return this.depths.get(entity);
            }
            // No depth if no dependencies
            if (entity.getAllRefs() == null || entity.getAllRefs().size() == 0) {
                this.depths.put(entity, 0);
                return 0;
            }

            // otherwise, find max
            int maxDepth = 0;
            for (final EntityModel childEntity : entity.getAllRefs()) {
                if (childEntity == entity) {
                    maxDepth = Math.max(maxDepth, 0);
                } else {
                    maxDepth = Math.max(maxDepth, this.calculateDepth(childEntity));
                }
            }
            // one higher than before
            maxDepth++;
            // save it for the future
            this.depths.put(entity, maxDepth);
            return maxDepth;

            /*return depths.computeIfAbsent(entity, e -> e.getAllRefs().stream()
                    .map(this::calculateDepth)
                    .max(Comparator.comparingInt(d -> d))
                    .map(depth -> depth + 1)
                    .orElse(0)
            );*/
        }
    }
}
