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

import de.yamass.redg.models.TableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TableModelExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(TableModelExtractor.class);
    private static final Pattern tableModelPattern = Pattern.compile("private static String serializedTableModel = \"(.+)\";");

    /**
     * Extracts all {@link TableModel}s from the Java source code of the RedG entity classes at the specified location.
     *
     * @param srcDir The source folder without the package structure directories
     * @param packageName The package name of the RedG classes
     * @param classPrefix The class prefix of the RedG classes ("G" if not changed during code generation)
     * @return a list of all found {@link TableModel}s
     * @throws IOException Thrown when a file could not be found or read
     * @throws ClassNotFoundException Thrown when the {@link TableModel} (and thus redg-models maven module) is not in classpath
     */
    public static List<TableModel> extractTableModelsFromSourceCode(final Path srcDir, final String packageName, final String classPrefix) throws IOException, ClassNotFoundException {
        LOG.debug("Starting table model extraction from source code...");
        final List<TableModel> results = new ArrayList<>();
        final String packageNameAsFolders = packageName.replace(".", "/");
        final Path codeFilePath = srcDir.resolve(packageNameAsFolders);
        LOG.debug("Folder with source files is {}", codeFilePath.toAbsolutePath().toString());

        final List<Path> paths = Files.list(codeFilePath)
                .filter(path -> path.getFileName().toString().matches(classPrefix + ".+\\.java"))
                .collect(Collectors.toList());
        LOG.debug("Found following java source files:\n{}", paths.stream().map(p -> p.toAbsolutePath().toString()).collect(Collectors.joining("\n")));
        for (Path p : paths) {
            LOG.debug("Loading source file {}", p.toAbsolutePath().toString());
            final String code = new String(Files.readAllBytes(p));
            final Matcher m = tableModelPattern.matcher(code);
            if (m.find()) {
                LOG.debug("Found serialized table model inside of file. Extracting...");
                final String encodedModel = m.group(1);
                final byte[] decodedModel = Base64.getDecoder().decode(encodedModel);
                final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(decodedModel));
                results.add((TableModel) ois.readObject());
                LOG.debug("Table model successfully extracted and deserialized.");
            }
        }

        return results;
    }

    /**
     * Extracts all {@link TableModel}s from the compiled RedG entity classes at the specified location.
     *
     * @param classDir The class folder without the package structure directories
     * @param packageName The package name of the RedG classes
     * @param classPrefix The class prefix of the RedG classes ("G" if not changed during code generation)
     * @return a list of all found {@link TableModel}s
     * @throws IOException Thrown when a file could not be found or read
     * @throws ClassNotFoundException Thrown when the {@link TableModel} (and thus redg-models maven module) is not in classpath
     * @throws NoSuchMethodException Only thrown if RedG code is invalid. Regenerate code.
     * @throws InvocationTargetException Only thrown if RedG code is invalid. Regenerate code.
     * @throws IllegalAccessException Only thrown if RedG code is invalid. Regenerate code.
     */
    public static List<TableModel> extractTableModelFromClasses(final Path classDir, final String packageName, final String classPrefix) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        LOG.debug("Starting table model extraction from compiled class files...");
        final List<TableModel> results = new ArrayList<>();
        final String packageNameAsFolders = packageName.replace(".", "/");
        final Path codeFilePath = classDir.resolve(packageNameAsFolders);
        LOG.debug("Folder with class files is {}", codeFilePath.toAbsolutePath().toString());

        final List<String> classNames = Files.list(codeFilePath)
                .filter(path -> path.getFileName().toString().matches(classPrefix + ".+\\.class"))
                .map(classDir::relativize)
                .map(Path::toString)
                .map(name -> name.replaceFirst("\\.class$", ""))
                .map(name -> name.replaceAll("(/|\\\\)", "."))
                .collect(Collectors.toList());
        LOG.debug("Found following class files:\n{}", classNames.stream().collect(Collectors.joining("\n")));
        LOG.debug("Loading all found classes...");
        final ClassLoader cl = new URLClassLoader(new URL[]{classDir.toUri().toURL()});
        for (String className : classNames) {
            LOG.debug("Loading class {}.", className);
            final Class cls = cl.loadClass(className);
            final Method m = cls.getDeclaredMethod("getTableModel");
            results.add((TableModel) m.invoke(null));
            LOG.debug("Successfully extracted table model.");
        }

        return results;
    }
}
