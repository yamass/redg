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

import de.yamass.redg.extractor.model.EntityModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupString;
import org.stringtemplate.v4.StringRenderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class CodeGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(CodeGenerator.class);

    private STGroup stGroup;

    /**
     * Creates a new code generator using the default 'extractor-templates.stg' template file
     */
    public CodeGenerator() {
        this("extractor-templates.stg");
    }

    /**
     * Creates a new code generator using the passed template resource.
     * The template group file <b>must</b> include template
     * <ul>
     * <li>codeClass(package, className, redGClass, entities)</li>
     * </ul>
     * There is no check build in, code generation will fail if this template does not exist.
     *
     * @param templateResource The name of the template resource, relative to class loader
     */
    public CodeGenerator(final String templateResource) {
        try {
            LOG.info("Loading code templates...");
            final InputStream is = getClass().getClassLoader().getResourceAsStream(templateResource);
            this.stGroup = new STGroupString(this.getStreamAsString(is));
            this.stGroup.registerRenderer(String.class, new StringRenderer());
            LOG.info("Loading successful.");
        } catch (IOException e) {
            LOG.error("Loading failed.", e);
            throw new RuntimeException(e);
        }
    }

    private String getStreamAsString(final InputStream inputStream) throws IOException {
        try (final BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            LOG.error("Got IO exception while reading resource", e);
            throw new RuntimeException(e);
        }
    }

    public String generateCode(final String codePackage, final String redGClassName, final String className,  final List<EntityModel> entities) {
        final ST template = this.stGroup.getInstanceOf("codeClass");
        LOG.debug("Filling template...");
        template.add("package", codePackage);
        template.add("className", className);
        template.add("redGClass", redGClassName);
        template.add("entities", entities);

        return template.render();
    }

}
