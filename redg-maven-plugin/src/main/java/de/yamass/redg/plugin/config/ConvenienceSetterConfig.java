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

import java.io.Serializable;

public class ConvenienceSetterConfig implements Serializable{

	/**
	 * The fully qualified class name of the (first and only!) parameter of the convenience setter.
	 */
	@Parameter
    private String convenienceType;

	/**
	 * The fully qualified name of the converter method. This must be a static method with one parameter.
	 * E.g. ${@code com.example.MyConverter.convert}
	 */
	@Parameter
    private String converterMethod;

    public ConvenienceSetterConfig(String convenienceType, String converterMethod) {
        this.convenienceType = convenienceType;
        this.converterMethod = converterMethod;
    }

    public String getConvenienceType() {
        return convenienceType;
    }

    public void setConvenienceType(String convenienceType) {
        this.convenienceType = convenienceType;
    }

    public String getConverterMethod() {
        return converterMethod;
    }

    public void setConverterMethod(String converterMethod) {
        this.converterMethod = converterMethod;
    }

	ConvenienceSetterModel toModel() {
		return new ConvenienceSetterModel(convenienceType, converterMethod);
	}
}
