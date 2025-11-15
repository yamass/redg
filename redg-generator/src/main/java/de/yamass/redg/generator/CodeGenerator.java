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

package de.yamass.redg.generator;

import de.yamass.redg.generator.exceptions.RedGGenerationException;
import de.yamass.redg.generator.utils.FileUtils;
import de.yamass.redg.generator.utils.JavaSqlStringEscapeMap;
import de.yamass.redg.generator.utils.JavaStringEscapeMap;
import de.yamass.redg.models.TableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupString;
import org.stringtemplate.v4.StringRenderer;

import java.io.*;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The class that manages the templates and does all the code generation
 */
public class CodeGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(CodeGenerator.class);

    private STGroup stGroup;

    /**
     * Creates a new code generator using the default 'templates.stg' template file and escaping of identifiers
     * conforming to the SQL standard.
     */
    public CodeGenerator() {
        this("templates.stg", null);
    }

    /**
     * Creates a new code generator using the passed template resource.
     * The template group file <b>must</b> include templates
     * <ul>
     * <li>mainClass(package, prefix, tables)</li>
     * <li>tableClass(package, className, columns, foreignKeys)</li>
     * </ul>
     * There is no check build in, code generation will fail if these templates do not exist.
     *
     * @param templateResource the resource to use. Has to be a .stg template group file
     * @param sqlEscapeString  the string to escape SQL identifiers. Has to be Java-Escaped. Defaults to "\\\""
     */
    public CodeGenerator(final String templateResource, final String sqlEscapeString) {
        try {
            LOG.info("Loading code templates...");
            final InputStream is = getClass().getClassLoader().getResourceAsStream(templateResource);
            this.stGroup = new STGroupString(this.getStreamAsString(is));
            this.stGroup.registerRenderer(String.class, new StringRenderer());
            this.stGroup.defineDictionary("escape", new JavaStringEscapeMap());
            if (sqlEscapeString != null) {
                this.stGroup.defineDictionary("escapeSql", new JavaSqlStringEscapeMap(sqlEscapeString));
            } else {
                this.stGroup.defineDictionary("escapeSql", new JavaSqlStringEscapeMap());
            }
            LOG.info("Loading successful.");
        } catch (IOException e) {
            LOG.error("Loading failed.", e);
            throw new RedGGenerationException("Loading of template file failed", e);
        }
    }

    /**
     * Creates a new code generator using the passed template resource.
     * The template group file <b>must</b> include templates
     * <ul>
     * <li>mainClass(package, prefix, tables)</li>
     * <li>tableClass(package, className, columns, foreignKeys)</li>
     * </ul>
     * There is no check build in, code generation will fail if these templates do not exist.
     *
     * @param templateResource the resource to use. Has to be a .stg template group file
     */
    public CodeGenerator(final String templateResource) {
        this(templateResource, null);
    }

    /**
     * Generates the code from the given set of tables and writes the output into the passed folder. Each table results
     * in two files (one for the main entity, one for the existing entity reference) and a "main" manager class is generated as well,
     * which contains all the instantiation and management methods.
     *
     * @param tables                     The table models that code should be generated for.
     * @param targetWithPkgFolders       The path pointing to the location the .java files should be places at.
     * @param enableVisualizationSupport If {@code true}, the RedG visualization features will be enabled for the generated code. This will result in a small
     *                                   performance hit and slightly more memory usage if activated.
     */
    public void generate(List<TableModel> tables, Path targetWithPkgFolders, final boolean enableVisualizationSupport) {
        LOG.info("Starting code generation...");
        LOG.info("Generation code for tables...");
        for (TableModel table : tables) {
            LOG.debug("Generating code for table {} ({})", table.getSqlFullName(), table.getName());
            try {
                final String result = generateCodeForTable(table, enableVisualizationSupport);
                FileUtils.writeCodeFile(targetWithPkgFolders, table.getClassName(), result);

                if (!table.getPrimaryKeyColumns().isEmpty()) {
                    final String existingResult = generateExistingClassCodeForTable(table);
                    FileUtils.writeCodeFile(targetWithPkgFolders, "Existing" + table.getClassName(), existingResult);
                }

                final String dummyResult = generateDummyClassCodeForTable(table);
                FileUtils.writeCodeFile(targetWithPkgFolders, "Dummy" + table.getClassName(), dummyResult);

                LOG.debug("Code for table {} ({}) generated successfully.", table.getSqlFullName(), table.getName());
            } catch (IOException e) {
                LOG.error("Failed writing code to file", e);
                throw new RedGGenerationException("Failed writing code to file", e);
            }
        }
        LOG.info("Generating code for main builder class...");
        final String mainCode = generateMainClass(tables, enableVisualizationSupport);
        try {
            FileUtils.writeCodeFile(targetWithPkgFolders, "RedG", mainCode);
        } catch (IOException e) {
            LOG.error("Failed writing code to file", e);
            throw new RedGGenerationException("Failed writing code to file", e);
        }
        LOG.info("Code generation finished successfully.");
    }

    private String getStreamAsString(final InputStream inputStream) throws IOException {
        try (final BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            LOG.error("Got IO exception while reading resource", e);
            throw new RedGGenerationException(e);
        }
    }

    /**
     * Generates a java code file that can later be used to generate SQL insert strings for the given extractor table.
     * <p>
     * For each column in this table the generated class will contain a private variable holding the value and a builder method to set the value. Foreign key
     * columns will be a reference to the generated class/object for that table and need to be passed via the constructor to ensure that foreign key constraints
     * are met and inserts will be in the right order. Proper {@code null}-checks will be generated.
     *
     * @param table                      The extracted table model to generate code for
     * @param enableVisualizationSupport If {@code true}, the RedG visualization features will be enabled for the generated code. This will result in a small
     *                                   performance hit and slightly more memory usage if activated.
     * @return The generated source code
     */
    public String generateCodeForTable(final TableModel table, final boolean enableVisualizationSupport) {
        final ST template = this.stGroup.getInstanceOf("tableClass");
        LOG.debug("Filling template...");
        template.add("table", table);
        LOG.debug("Package is {} | Class name is {}", table.getPackageName(), table.getClassName());

        template.add("colAndForeignKeys", table.hasColumnsAndForeignKeys());
        template.add("firstRowComma", (!table.getNullableForeignKeys().isEmpty() || table.hasColumnsAndForeignKeys()) && !table.getNotNullForeignKeys().isEmpty());
        template.add("secondRowComma", table.hasColumnsAndForeignKeys() && !table.getNullableForeignKeys().isEmpty());

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(table);
            oos.close();
            String serializedData = Base64.getEncoder().encodeToString(baos.toByteArray());
            template.add("serializedTableModelString", serializedData);
        } catch (IOException e) {
            LOG.error("Could not serialize table model. Model will not be included in the file", e);
        }
        template.add("enableVisualizationSupport", enableVisualizationSupport);

        LOG.debug("Rendering template...");
        return template.render();
    }

    /**
     * Generates the java code for the class representing a entity that is already in the database but needs to be referenced from entities that
     * should be created by RedG.
     * <p>
     * The generated class extends the normal redg class and overwrites its methods, throwing
     * {@link UnsupportedOperationException} for each field access except getting the value of the primary keys.
     *
     * @param table The extracted table model to generate code for
     * @return The generated source code
     */
    public String generateExistingClassCodeForTable(final TableModel table) {
        final ST template = this.stGroup.getInstanceOf("existingTableClass");
        LOG.debug("Filling template...");
        template.add("table", table);

        LOG.debug("Rendering template...");
        return template.render();
    }

    public String generateDummyClassCodeForTable(TableModel table) {
        final ST template = this.stGroup.getInstanceOf("dummyTableClass");
        LOG.debug("Filling template...");
        template.add("table", table);

        LOG.debug("Rendering template...");
        return template.render();
    }

    /**
     * Generates the main class used for creating the extractor objects and later generating the insert statements.
     * For each passed table a appropriate creation method will be generated that will return the new object and internally add it to the list of objects that
     * will be used to generate the insert strings
     *
     * @param tables                     All tables that should get a creation method in the main class
     * @param enableVisualizationSupport If {@code true}, the RedG visualization features will be enabled for the generated code. This will result in a small
     *                                   performance hit and slightly more memory usage if activated.
     * @return The generated source code
     */
    public String generateMainClass(final Collection<TableModel> tables, final boolean enableVisualizationSupport) {
        Objects.requireNonNull(tables);

        //get package from the table models
        final String targetPackage = ((TableModel) tables.toArray()[0]).getPackageName();
        final ST template = this.stGroup.getInstanceOf("mainClass");
        LOG.debug("Filling main class template containing helpers for {} classes...", tables.size());
        template.add("package", targetPackage);
        // TODO: make prefix usable
        template.add("prefix", "");
        template.add("enableVisualizationSupport", enableVisualizationSupport);
        LOG.debug("Package is {} | Prefix is {}", targetPackage, "");

        template.add("tables", tables);

        return template.render();
    }
}
