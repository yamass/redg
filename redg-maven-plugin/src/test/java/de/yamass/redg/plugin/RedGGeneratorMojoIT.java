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
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.yamass.redg.plugin;

import io.takari.maven.testing.TestMavenRuntime;
import io.takari.maven.testing.TestResources;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for RedGGeneratorMojo.
 * Note: These tests use JUnit 4 because takari-plugin-testing requires @Rule annotations.
 */
public class RedGGeneratorMojoIT {

	@Rule
	public final TestResources resources = new TestResources();

	@Rule
	public final TestMavenRuntime maven = new TestMavenRuntime();

	@Test
	public void generatesCodeWithBasicConfiguration() throws Exception {
		File basedir = resources.getBasedir("basic-test");
		maven.executeMojo(basedir, "redg");

		// Verify generated files exist
		Path generatedDir = basedir.toPath().resolve("target/generated-test-sources/redg/de/yamass/redg/generated");
		assertThat(generatedDir).exists();
		assertThat(generatedDir.resolve("RedG.java")).exists();
		assertThat(generatedDir.resolve("GUser.java")).exists();
		assertThat(generatedDir.resolve("DummyGUser.java")).exists();
		assertThat(generatedDir.resolve("ExistingGUser.java")).exists();

		// Verify the generated code has correct package
		String redgCode = Files.readString(generatedDir.resolve("RedG.java"));
		assertThat(redgCode).contains("package de.yamass.redg.generated;");

		// Verify the generated class has expected structure
		String userCode = Files.readString(generatedDir.resolve("GUser.java"));
		assertThat(userCode).contains("package de.yamass.redg.generated;");
		assertThat(userCode).contains("public class GUser");
		assertThat(userCode).contains("public GUser username(java.lang.String value)");
	}

	@Test
	public void generatesCodeWithCustomPackage() throws Exception {
		File basedir = resources.getBasedir("custom-package-test");
		maven.executeMojo(basedir, "redg");

		// Verify generated files with custom package
		Path generatedDir = basedir.toPath().resolve("target/generated-test-sources/redg/com/example/custom");
		assertThat(generatedDir).exists();
		assertThat(generatedDir.resolve("RedG.java")).exists();
		assertThat(generatedDir.resolve("GUser.java")).exists();
		assertThat(generatedDir.resolve("DummyGUser.java")).exists();

		// Verify the generated code uses custom package
		String redgCode = Files.readString(generatedDir.resolve("RedG.java"));
		assertThat(redgCode).contains("package com.example.custom;");

		// Verify the generated class uses custom package but default prefix
		String userCode = Files.readString(generatedDir.resolve("GUser.java"));
		assertThat(userCode).contains("package com.example.custom;");
		assertThat(userCode).contains("public class GUser");
		assertThat(userCode).doesNotContain("package de.yamass.redg.generated;"); // Should not use default package
	}

	@Test
	public void generatesCodeWithCustomPrefix() throws Exception {
		File basedir = resources.getBasedir("custom-prefix-test");
		maven.executeMojo(basedir, "redg");

		// Verify generated files with custom prefix
		Path generatedDir = basedir.toPath().resolve("target/generated-test-sources/redg/de/yamass/redg/generated");
		assertThat(generatedDir).exists();
		assertThat(generatedDir.resolve("RedG.java")).exists();
		assertThat(generatedDir.resolve("CustomUser.java")).exists();
		assertThat(generatedDir.resolve("DummyCustomUser.java")).exists();

		// Verify the generated code uses default package
		String redgCode = Files.readString(generatedDir.resolve("RedG.java"));
		assertThat(redgCode).contains("package de.yamass.redg.generated;");

		// Verify the generated class uses custom prefix
		String userCode = Files.readString(generatedDir.resolve("CustomUser.java"));
		assertThat(userCode).contains("package de.yamass.redg.generated;");
		assertThat(userCode).contains("public class CustomUser");
		assertThat(userCode).doesNotContain("public class GUser"); // Should not use default prefix
	}

	@Test
	public void filtersTablesWithRegex() throws Exception {
		File basedir = resources.getBasedir("table-filter-test");
		maven.executeMojo(basedir, "redg");

		Path generatedDir = basedir.toPath().resolve("target/generated-test-sources/redg/de/yamass/redg/generated");
		assertThat(generatedDir).exists();
		// Should only generate code for tables matching the regex "^user$"
		assertThat(generatedDir.resolve("GUser.java")).exists();
		assertThat(generatedDir.resolve("GProduct.java")).doesNotExist();

		// Verify the generated RedG class only references the filtered table
		String redgCode = Files.readString(generatedDir.resolve("RedG.java"));
		assertThat(redgCode).contains("GUser"); // Should contain user table
		assertThat(redgCode).doesNotContain("GProduct"); // Should not contain product table
	}

	@Test
	public void filtersSchemasWithRegex() throws Exception {
		File basedir = resources.getBasedir("schema-filter-test");
		maven.executeMojo(basedir, "redg");

		Path generatedDir = basedir.toPath().resolve("target/generated-test-sources/redg/de/yamass/redg/generated");
		assertThat(generatedDir).exists();
		// Should only generate code for tables in schemas matching the regex ".*PUBLIC.*"
		// In H2, schema names are reported as "CATALOG.SCHEMA" (e.g., "SCHEMA_FILTER_TEST.PUBLIC")
		assertThat(generatedDir.resolve("GUser.java")).exists();
		assertThat(generatedDir.resolve("GProduct.java")).doesNotExist();

		// Verify the generated RedG class only references tables from the filtered schema
		String redgCode = Files.readString(generatedDir.resolve("RedG.java"));
		assertThat(redgCode).contains("GUser"); // Should contain user table from PUBLIC schema
		assertThat(redgCode).doesNotContain("GProduct"); // Should not contain product table from test_schema
	}

	@Test
	public void generatesCodeWithCustomTypeMappings() throws Exception {
		File basedir = resources.getBasedir("custom-type-mappings-test");
		maven.executeMojo(basedir, "redg");

		Path generatedDir = basedir.toPath().resolve("target/generated-test-sources/redg/de/yamass/redg/generated");
		assertThat(generatedDir).exists();

		// Verify the generated code uses custom type mapping
		// The type-mappings.json maps VARCHAR to java.lang.String
		String userCode = Files.readString(generatedDir.resolve("GUser.java"));
		assertThat(userCode).contains("java.lang.String username"); // Should use String type
		assertThat(userCode).contains("public GUser username(java.lang.String value)");
	}

	@Test
	public void generatesCodeWithExplicitAttributesConfig() throws Exception {
		File basedir = resources.getBasedir("explicit-attributes-test");
		maven.executeMojo(basedir, "redg");

		Path generatedDir = basedir.toPath().resolve("target/generated-test-sources/redg/de/yamass/redg/generated");
		assertThat(generatedDir).exists();

		String userCode = Files.readString(generatedDir.resolve("RedG.java"));
		assertThat(userCode).contains("public GUser addUser(java.lang.String description, GA aIdA) {");
	}

	@Test
	public void generatesCodeWithCustomNameMappings() throws Exception {
		File basedir = resources.getBasedir("custom-name-mappings-test");
		maven.executeMojo(basedir, "redg");

		Path generatedDir = basedir.toPath().resolve("target/generated-test-sources/redg/de/yamass/redg/generated");
		assertThat(generatedDir).exists();

		// Verify custom names are used
		// The name-mappings.json maps USERNAME column to customUserName attribute
		// Note: H2 reports table/column names in uppercase
		String userCode = Files.readString(generatedDir.resolve("GUser.java"));

		assertThat(userCode).contains("public GUser customUserName(");
		assertThat(userCode).contains("public java.lang.String customUserName()");
	}

	@Test
	public void generatesCodeWithConvenienceSetters() throws Exception {
		File basedir = resources.getBasedir("convenience-setters-test");
		maven.executeMojo(basedir, "redg");

		Path generatedDir = basedir.toPath().resolve("target/generated-test-sources/redg/de/yamass/redg/generated");
		assertThat(generatedDir).exists();

		// Verify the generated code contains the convenience setter
		String userCode = Files.readString(generatedDir.resolve("GUser.java"));
		// The convenience setter should accept int and convert it using the specified converter method
		assertThat(userCode).contains("public GUser username(int value)");
		assertThat(userCode).contains("de.yamass.redg.generated.StringConverter.convertIntToString(value, java.lang.String.class)");
	}

	@Test
	public void generatesCodeToCustomGeneratedSourcesDirectory() throws Exception {
		File basedir = resources.getBasedir("generated-sources-test");
		maven.executeMojo(basedir, "redg");

		// Verify generated files in generated-sources (not generated-test-sources)
		Path generatedDir = basedir.toPath().resolve("target/generated-sources/redg/de/yamass/redg/generated");
		assertThat(generatedDir).exists();
		assertThat(generatedDir.resolve("RedG.java")).exists();
		assertThat(generatedDir.resolve("GUser.java")).exists();

		// Verify generated-test-sources does NOT exist
		Path generatedTestSources = basedir.toPath().resolve("target/generated-test-sources/redg");
		assertThat(generatedTestSources).doesNotExist();

		// Verify the generated code has correct package
		String redgCode = Files.readString(generatedDir.resolve("RedG.java"));
		assertThat(redgCode).contains("package de.yamass.redg.generated;");
	}
}

