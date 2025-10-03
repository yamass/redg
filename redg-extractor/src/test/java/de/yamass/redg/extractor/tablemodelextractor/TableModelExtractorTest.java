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

package de.yamass.redg.extractor.tablemodelextractor;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

class TableModelExtractorTest {

    // TODO: do not rely on external files
    @Test
    @Disabled
    void extractTableModelsFromSourceCode() throws Exception {
        TableModelExtractor.extractTableModelsFromSourceCode(Paths.get("D:\\redg\\redg-playground\\target\\generated-test-sources\\redg"),
                "de.yamass.redg.generated", "G");
    }

    @Test
    @Disabled
    void extractTableModelFromClasses() throws Exception {
        TableModelExtractor.extractTableModelFromClasses(Paths.get("D:\\redg\\redg-playground\\target\\test-classes"),
                "de.yamass.redg.generated", "G");
    }

}