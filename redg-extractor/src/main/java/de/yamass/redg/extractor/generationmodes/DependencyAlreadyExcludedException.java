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

package de.yamass.redg.extractor.generationmodes;

import de.yamass.redg.extractor.model.EntityModel;

public class DependencyAlreadyExcludedException extends RuntimeException{

    public DependencyAlreadyExcludedException(EntityModel entityModel) {
        super("A not-nullable dependency of this entity was already excluded, thus this entity could not be generated." +
                " Either mark the entity this entity depends on as existing or exclude this entity as well. The affected entity is " + entityModel);
    }
}
