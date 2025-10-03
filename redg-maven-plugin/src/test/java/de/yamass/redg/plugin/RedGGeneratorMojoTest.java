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

package de.yamass.redg.plugin;

import io.takari.maven.testing.TestMavenRuntime5;
import io.takari.maven.testing.TestResources5;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;

class RedGGeneratorMojoTest {

	@RegisterExtension
	final TestResources5 resources = new TestResources5();

	@RegisterExtension
	final TestMavenRuntime5 maven = new TestMavenRuntime5();

	@Test
	void test() throws Exception {
		File baseDir = resources.getBasedir("full-project-test");
		maven.executeMojo(baseDir, "redg", TestHelpers.getArrayParameters("sqlScripts", "test.sql"));
		// TODO: actually test something
	}

}
