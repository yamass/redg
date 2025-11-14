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

package de.yamass.redg.plugin.config;

import de.yamass.redg.models.ConvenienceSetterModel;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

public class ConvenienceSettersConfig {

	/**
	 * The fully qualified java type name for which the convenience setter(s) should be generated.
	 */
	@Parameter
    private String originalType;

	/**
	 * The list of convenience setters.
	 */
	@Parameter
    private List<ConvenienceSetterModel> setters;

    public String getOriginalType() {
        return originalType;
    }

    public void setOriginalType(String originalType) {
        this.originalType = originalType;
    }

    public List<ConvenienceSetterModel> getSetters() {
        return setters;
    }

    public void setSetters(List<ConvenienceSetterModel> setters) {
        this.setters = setters;
    }
}
