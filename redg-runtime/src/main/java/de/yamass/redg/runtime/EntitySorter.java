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

package de.yamass.redg.runtime;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EntitySorter {

    public static List<RedGEntity> sortEntities(List<RedGEntity> entities) {
        Map<RedGEntity, Integer> depths = new DepthCalculator().calculateDepths(entities);
        return entities.stream()
                .sorted(Comparator.comparing(EntitySorter::isExisting).reversed().thenComparingInt(depths::get))
                .collect(Collectors.toList());
    }

    private static boolean isExisting(RedGEntity entity) {
        return entity.getClass().getSimpleName().startsWith("Existing");
    }

    private static class DepthCalculator {

        Map<RedGEntity, Integer> depths;

        Map<RedGEntity, Integer> calculateDepths(List<RedGEntity> entities) {
            depths = new HashMap<>();
            entities.forEach(this::calculateDepth);
            return depths;
        }

        private int calculateDepth(RedGEntity entity) {
            // Get depth if already calculated
            if (this.depths.containsKey(entity)) {
                return this.depths.get(entity);
            }
            // No depth if no dependencies
            if (entity.getDependencies() == null || entity.getDependencies().size() == 0) {
                this.depths.put(entity, 0);
                return 0;
            }

            // otherwise, find max
            int maxDepth = 0;
            for (final RedGEntity childEntity : entity.getDependencies()) {
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

            /* Old solution, returns ConcurrentModificationException in edge cases
            return depths.computeIfAbsent(entity, e -> e.getDependencies().stream()
                    .map(this::calculateDepth)
                    .max(Comparator.comparingInt(d -> d))
                    .map(depth -> depth + 1)
                    .orElse(0)
            );*/
        }

    }
}
