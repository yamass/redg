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

import de.yamass.redg.DatabaseType;
import de.yamass.redg.generator.exceptions.RedGGenerationException;
import de.yamass.redg.generator.extractor.SchemaModelToGeneratorModelTransformer;
import de.yamass.redg.generator.extractor.conveniencesetterprovider.ConvenienceSetterProvider;
import de.yamass.redg.generator.extractor.datatypeprovider.DataTypeProvider;
import de.yamass.redg.generator.extractor.datatypeprovider.DefaultDataTypeProvider;
import de.yamass.redg.generator.extractor.explicitattributedecider.DefaultExplicitAttributeDecider;
import de.yamass.redg.generator.extractor.explicitattributedecider.ExplicitAttributeDecider;
import de.yamass.redg.generator.extractor.nameprovider.DefaultNameProvider;
import de.yamass.redg.generator.extractor.nameprovider.NameProvider;
import de.yamass.redg.generator.utils.DatabaseTypeDetector;
import de.yamass.redg.generator.utils.FileUtils;
import de.yamass.redg.models.TableModel;
import de.yamass.redg.schema.inspector.SchemaInspector;
import de.yamass.redg.schema.model.SchemaInspectionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
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
	 * First, this method will use the provided JDBC connection to inspect the database schemas using {@link SchemaInspector}.
	 * The {@code schemas} parameter specifies which schemas to inspect. If {@code null} or empty, all schemas will be inspected.
	 * <p>Afterwards the {@link SchemaModelToGeneratorModelTransformer} gets called and transforms the schema model into a list of {@link TableModel}s.
	 * <p>The third step generates the code and writes it into the source files at the specified location.
	 *
	 * @param dataSource                 The data source for connecting to the database that will be used as the data source for the analysis
	 * @param schemas                    The list of schema names to inspect. If {@code null} or empty, all schemas will be inspected.
	 * @param targetPackage              The java package for the generated code. May not be default package. If {@code null},
	 *                                   defaults to {@link Constants#DEFAULT_TARGET_PACKAGE}
	 * @param classPrefix                The prefix for the generated java class names
	 * @param targetDirectory            The directory to put the generated source files. The package directory structure will be generated automatically
	 * @param dataTypeProvider           The data type provider for customization of the SQL type to java type mapping. If {@code null},
	 *                                   a {@link DefaultDataTypeProvider} will be used.
	 * @param nameProvider               The {@link NameProvider} used to determine the names in the generated code. If {@code null},
	 *                                   a {@link DefaultNameProvider} will be used.
	 * @param convenienceSetterProvider  A provider that determines convenience setters
	 * @param explicitAttributeDecider   The {@link ExplicitAttributeDecider} that will be used to determine whether a attribute / foreign key should be treated
	 *                                   as explicitly required. If {@code null}, a {@link DefaultExplicitAttributeDecider} will be used.
	 * @param enableVisualizationSupport If {@code true}, the RedG visualization features will be enabled for the generated code. This will result in a small
	 *                                   performance hit and slightly more memory usage if activated.
	 */
	public static void generateCode(final DataSource dataSource,
	                                final List<String> schemas,
	                                String targetPackage,
	                                String classPrefix,
	                                final Path targetDirectory,
	                                final DataTypeProvider dataTypeProvider,
	                                final NameProvider nameProvider,
	                                final ExplicitAttributeDecider explicitAttributeDecider,
	                                final ConvenienceSetterProvider convenienceSetterProvider,
	                                final boolean enableVisualizationSupport) {
		Objects.requireNonNull(dataSource, "RedG requires a JDBC connection to a database to perform an analysis");
		targetPackage = targetPackage != null ? targetPackage : Constants.DEFAULT_TARGET_PACKAGE;
		classPrefix = classPrefix != null ? classPrefix : Constants.DEFAULT_CLASS_PREFIX;

		DataTypeProvider effectiveDataTypeProvider = dataTypeProvider != null ? dataTypeProvider : new DefaultDataTypeProvider();
		NameProvider effectiveNameProvider = nameProvider != null ? nameProvider : new DefaultNameProvider();
		ExplicitAttributeDecider effectiveExplicitAttributeDecider = explicitAttributeDecider != null
				? explicitAttributeDecider : new DefaultExplicitAttributeDecider();
		ConvenienceSetterProvider effectiveConvenienceSetterProvider = convenienceSetterProvider != null
				? convenienceSetterProvider : ConvenienceSetterProvider.NONE;

		Objects.requireNonNull(targetDirectory, "RedG needs a target directory for the generated source code");

		LOG.info("Starting the RedG all-in-one code generation.");

		SchemaInspectionResult schemaResult = inspectSchemas(dataSource, schemas);
		final List<TableModel> tables = transformSchemaModel(schemaResult, classPrefix, targetPackage,
				effectiveDataTypeProvider, effectiveNameProvider, effectiveExplicitAttributeDecider, effectiveConvenienceSetterProvider);
		Path targetWithPkgFolders = createPackageFolderStructure(targetDirectory, targetPackage);
		new CodeGenerator().generate(tables, targetWithPkgFolders, enableVisualizationSupport);
	}

	/**
	 * Inspects the specified schemas using SchemaInspector and combines the results.
	 *
	 * @param dataSource The data source for connecting to the database
	 * @param schemas    The list of schema names to inspect. If {@code null} or empty, inspects all schemas (passes null to SchemaInspector)
	 * @return A combined SchemaInspectionResult containing all tables, constraints, and UDTs from the inspected schemas
	 */
	public static SchemaInspectionResult inspectSchemas(final DataSource dataSource, final List<String> schemas) {
		try {
			DatabaseType databaseType = DatabaseTypeDetector.detectDatabaseType(dataSource);
			SchemaInspector inspector = new SchemaInspector(databaseType, dataSource);

			List<SchemaInspectionResult> results = new ArrayList<>();

			if (schemas == null) {
				// If null, inspect with null (which typically means all schemas or default schema)
				LOG.info("Inspecting all schemas...");
				results.add(inspector.inspectSchema(null));
			} else if (schemas.isEmpty()) {
				// Empty list means inspect no schemas, which will result in no tables
				LOG.info("Empty schemas list provided - no schemas will be inspected");
				// Don't add any results - results list will remain empty
			} else {
				LOG.info("Inspecting {} schema(s)...", schemas.size());
				for (String schema : schemas) {
					LOG.debug("Inspecting schema: {}", schema);
					results.add(inspector.inspectSchema(schema));
				}
			}

			SchemaInspectionResult combined = SchemaInspectionResult.combine(results);
			LOG.info("Schema inspection complete. Found {} tables, {} constraints, {} UDTs",
					combined.tables().size(), combined.constraints().size(), combined.udts().size());
			return combined;
		} catch (SQLException e) {
			LOG.error("Schema inspection failed with an exception.", e);
			throw new RedGGenerationException("Schema inspection failed", e);
		}
	}

	/**
	 * Transforms a SchemaInspectionResult into a list of TableModel objects.
	 *
	 * @param schemaModel               The schema inspection result to transform
	 * @param classPrefix               The prefix for generated class names
	 * @param targetPackage             The target package for generated code
	 * @param dataTypeProvider          The data type provider
	 * @param nameProvider              The name provider
	 * @param explicitAttributeDecider  The explicit attribute decider
	 * @param convenienceSetterProvider The convenience setter provider
	 * @return A list of table models ready for code generation
	 */
	public static List<TableModel> transformSchemaModel(
			final SchemaInspectionResult schemaModel,
			final String classPrefix,
			final String targetPackage,
			final DataTypeProvider dataTypeProvider,
			final NameProvider nameProvider,
			final ExplicitAttributeDecider explicitAttributeDecider,
			final ConvenienceSetterProvider convenienceSetterProvider) {
		LOG.info("Transforming schema model to generator model...");
		SchemaModelToGeneratorModelTransformer transformer = new SchemaModelToGeneratorModelTransformer(
				classPrefix, targetPackage, dataTypeProvider, nameProvider,
				explicitAttributeDecider, convenienceSetterProvider);
		final List<TableModel> tables = transformer.transform(schemaModel);
		LOG.info("Transformation done.");

		if (tables.isEmpty()) {
			LOG.error("No tables extracted! The database is either empty or everything is excluded");
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
