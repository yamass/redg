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
import de.yamass.redg.generator.extractor.DataTypeExtractor;
import de.yamass.redg.generator.extractor.DatabaseManager;
import de.yamass.redg.generator.extractor.MetadataExtractor;
import de.yamass.redg.generator.extractor.TableExtractor;
import de.yamass.redg.generator.extractor.conveniencesetterprovider.ConvenienceSetterProvider;
import de.yamass.redg.generator.extractor.datatypeprovider.DataTypeProvider;
import de.yamass.redg.generator.extractor.datatypeprovider.DefaultDataTypeProvider;
import de.yamass.redg.generator.extractor.explicitattributedecider.ExplicitAttributeDecider;
import de.yamass.redg.generator.extractor.nameprovider.NameProvider;
import de.yamass.redg.generator.utils.FileUtils;
import de.yamass.redg.models.TableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schemacrawler.inclusionrule.IncludeAll;
import schemacrawler.inclusionrule.InclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.exceptions.SchemaCrawlerException;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * The main class for the RedG generator.
 */
public class RedGGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(RedGGenerator.class);


    /**
     * This is a convenience method that combines the whole metadata extraction and code generation process into a single method while still offering
     * most of the customizability.
     * <p>
     * First, this method will use the provided JDBC connection to run SchemaCrawler over the database and extract the metadata. The {@code schemaRule} and
     * {@code tableRule} will be passed on to SchemaCrawler and can be used to specify the tables for which metadata should be extracted.
     * <p>Afterwards the {@link MetadataExtractor} gets called and extracts the relevant data into a list of {@link TableModel}s.
     * <p>The third step generates the code and writes it into the source files at the specified location.
     *
     * @param dataSource                 The data source for connecting to the database that will be used as the data source for the analysis
     * @param schemaRule                 The rule used for inclusion/exclusion of database schemas. Use {@code null} or {@link IncludeAll} for no filtering.
     * @param tableRule                  The rule used for inclusion/Exclusion of database tables. Use {@code null} or {@link IncludeAll} for no filtering.
     * @param targetPackage              The java package for the generated code. May not be default package. If {@code null},
     *                                   defaults to {@link TableExtractor#DEFAULT_TARGET_PACKAGE}
     * @param classPrefix                The prefix for the generated java class names
     * @param targetDirectory            The directory to put the generated source files. The package directory structure will be generated automatically
     * @param dataTypeProvider           The data type provider for customization of the SQL type to java type mapping. If {@code null},
     *                                   a {@link DefaultDataTypeProvider} will be used.
     * @param nameProvider               The {@link NameProvider} used to determine the names in the generated code
     * @param convenienceSetterProvider  A provider that determines convenience setters
     * @param explicitAttributeDecider   The {@link ExplicitAttributeDecider} that will be used to determine whether a attribute / foreign key should be treated
     *                                   as explicitly required
     * @param enableVisualizationSupport If {@code true}, the RedG visualization features will be enabled for the generated code. This will result in a small
     *                                   performance hit and slightly more memory usage if activated.
     */
    public static void generateCode(final DataSource dataSource,
                                    final InclusionRule schemaRule,
                                    final InclusionRule tableRule,
                                    String targetPackage,
                                    String classPrefix,
                                    final Path targetDirectory,
                                    final DataTypeProvider dataTypeProvider,
                                    final NameProvider nameProvider,
                                    final ExplicitAttributeDecider explicitAttributeDecider,
                                    final ConvenienceSetterProvider convenienceSetterProvider,
                                    final boolean enableVisualizationSupport) {
        Objects.requireNonNull(dataSource, "RedG requires a JDBC connection to a database to perform an analysis");
        targetPackage = targetPackage != null ? targetPackage : TableExtractor.DEFAULT_TARGET_PACKAGE;
        classPrefix = classPrefix != null ? classPrefix : TableExtractor.DEFAULT_CLASS_PREFIX;
        DataTypeExtractor dataTypeExtractor = new DataTypeExtractor();
        final TableExtractor tableExtractor = new TableExtractor(classPrefix, targetPackage, dataTypeProvider, nameProvider, explicitAttributeDecider,
                convenienceSetterProvider);
        Objects.requireNonNull(targetDirectory, "RedG needs a target directory for the generated source code");

        LOG.info("Starting the RedG all-in-one code generation.");

        Catalog databaseCatalog = crawlDatabase(dataSource, schemaRule, tableRule);
        final List<TableModel> tables = extractTableModel(dataTypeExtractor, tableExtractor, databaseCatalog);
        Path targetWithPkgFolders = createPackageFolderStructure(targetDirectory, targetPackage);
        new CodeGenerator().generate(tables, targetWithPkgFolders, enableVisualizationSupport);
    }

    public static Catalog crawlDatabase(DataSource dataSource, InclusionRule schemaRule, InclusionRule tableRule) {
        Catalog database;
        try {
            LOG.info("Crawling the database for metadata...");
            database = DatabaseManager.crawlDatabase(dataSource, schemaRule, tableRule);
            LOG.info("Crawling done. Metadata completely assembled.");
        } catch (SchemaCrawlerException e) {
            LOG.error("Crawling failed with an exception.", e);
            throw new RedGGenerationException("Crawling failed", e);
        }
        return database;
    }

    public static List<TableModel> extractTableModel(DataTypeExtractor dataTypeExtractor, TableExtractor tableExtractor, Catalog database) {
        LOG.info("Extracting the required information from the metadata...");
        MetadataExtractor metadataExtractor = new MetadataExtractor(dataTypeExtractor, tableExtractor);
        final List<TableModel> tables = metadataExtractor.extract(database);
        LOG.info("Extraction done.");

        if (tables.size() == 0) {
            LOG.error("No tables extracted! The database is either empty or everything is excluded by rules");
            throw new RedGGenerationException("No tables to process!");
        }
        return tables;
    }

    public static Path createPackageFolderStructure(Path targetDirectory, String targetPackage) {
        Path targetWithPkgFolders;
        try {
            LOG.info("Creating package folder structure...");
            targetWithPkgFolders = FileUtils.generateJavaPackageStructure(targetDirectory, targetPackage);
            LOG.info("Creation successful.");
        } catch (IOException e) {
            LOG.error("Creation failed.", e);
            throw new RedGGenerationException("Creation of package folder structure failed", e);
        }
        return targetWithPkgFolders;
    }

}
