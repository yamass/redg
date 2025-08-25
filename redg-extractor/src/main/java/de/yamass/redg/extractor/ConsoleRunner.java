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

import ch.qos.logback.classic.Level;
import de.yamass.redg.extractor.model.EntityModel;
import de.yamass.redg.extractor.tablemodelextractor.TableModelExtractor;
import de.yamass.redg.models.TableModel;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class ConsoleRunner {

    private static Logger LOG = LoggerFactory.getLogger(ConsoleRunner.class);

    public static void main(String[] args) {
        final Options options = getOptions();

        final CommandLineParser parser = new DefaultParser();
        final HelpFormatter formatter = new HelpFormatter();

        try {
            final CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("verbose")) {
                setLogLevel(Level.ALL);
                LOG.info("Verbose logging activated");
            } else {
                setLogLevel(Level.ERROR);
            }

            if (cmd.hasOption("help")) {
                formatter.printHelp("java -jar redg-extractor.jar ", options);
                return;
            }
            final Path directory = Paths.get(cmd.getOptionValue("directory"));
            if (!Files.exists(directory) || !Files.isDirectory(directory)) {
                LOG.error("Specified directory does not exist!");
                System.exit(1);
                return;
            }
            final String codePackage = cmd.hasOption("package") ? cmd.getOptionValue("package") : "de.yamass.redg.generated";
            final String classPrefix = cmd.hasOption("prefix") ? cmd.getOptionValue("prefix") : "G";

            List<TableModel> tableModels;
            if (cmd.hasOption("source")) {
                tableModels = TableModelExtractor.extractTableModelsFromSourceCode(directory, codePackage, classPrefix);
            } else {
                tableModels = TableModelExtractor.extractTableModelFromClasses(directory, codePackage, classPrefix);
            }

            LOG.debug("Establishing database connection...");
            if (cmd.hasOption("jdbcDriver")) {
                Class.forName(cmd.getOptionValue("jdbcDriver"));
            }
            final String password = cmd.hasOption("password") ? cmd.getOptionValue("password") : readPassword(cmd.getOptionValue("username"));
            final Connection connection = DriverManager.getConnection(cmd.getOptionValue("connection"), cmd.getOptionValue("username"), password);

            LOG.debug("Connection established. Starting extraction...");
            final DataExtractor dataExtractor = new DataExtractor();
            if (cmd.hasOption("schemaName")) {
                dataExtractor.setSqlSchemaName(cmd.getOptionValue("schemaName"));
            }
            final List<EntityModel> entityModels = dataExtractor.extractAllData(connection, tableModels);
            LOG.debug("Extraction finished.");
            LOG.debug("Generating code...");

            final String redGClass = cmd.hasOption("redGClass") ? cmd.getOptionValue("redGClass") : "RedG";
            final String code = new CodeGenerator()
                    .generateCode(codePackage, redGClass, "ExtractedData", entityModels);
            LOG.debug("Code generation successful");
            LOG.debug("----- Code following now -----");
            System.out.println(code);

        } catch (ParseException e) {
            LOG.error("Could not parse parameters: {}", e.getMessage());

            formatter.printHelp("java -jar redg-extractor.jar ", options);
            System.exit(1);
        } catch (IOException e) {
            LOG.error("An error occurred while performing an I/O operation", e);
            System.exit(1);
        } catch (ClassNotFoundException e) {
            LOG.error("A class definition could not be found. Is your CLASSPATH correct?", e);
            System.exit(1);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOG.error("Reflection failed. Could not load the table model.", e);
            System.exit(1);
        } catch (SQLException e) {
            LOG.error("Failed to run database operation.", e);
            System.exit(1);
        }
    }

    private static String readPassword(final String user) {
        if (System.console() != null) {
            return new String(System.console().readPassword("Enter DB password for user %s:", user));
        }
        LOG.error("No console found. Please use a real console and no pseudo device or supply the password via the -p parameter.");
        System.exit(1);
        return null;
    }

    private static Options getOptions() {
        final Options options = new Options();

        final Option classDirectory = new Option("d", "directory", true,
                "The package root folder containing either the compiled RedG entity classes or their source code.\n" +
                        " If the source code is used, set the -s flag.\n" +
                        "Example: If folder with RedG main class is /home/users/kermit/code/me/kermit/redg/generated/, only use /home/users/kermit/code/ " +
                        "as directory and me.kermit.redg.generated as package.");
        classDirectory.setRequired(true);
        options.addOption(classDirectory);

        final Option packageName = new Option("p", "package", true,
                "The package name of the generated source code. Defaults to \"de.yamass.redg.generated\" if not specified");
        packageName.setRequired(false);
        options.addOption(packageName);

        final Option prefix = new Option("P", "prefix", true,
                "The name prefix used for the generated classes. Defaults to \"G\" if not specified");
        prefix.setRequired(false);
        options.addOption(prefix);

        final Option helpFlag = new Option("h", "help", false, "Prints this help message");
        helpFlag.setRequired(false);
        options.addOption(helpFlag);

        final Option verbose = new Option("v", "verbose", false, "Activates detailed log messages");
        verbose.setRequired(false);
        options.addOption(verbose);

        final Option source = new Option("s", "source", false, "If set, the specified directory contains Java source files");
        source.setRequired(false);
        options.addOption(source);

        final Option jdbcDriver = new Option("jdbcDriver", true, "The JDBC driver class. Has to be in the CLASSPATH.");
        options.addOption(jdbcDriver);

        final Option schemaName = new Option("schemaName", true, "The name of the SQL schema if different from the one in the table models.");
        options.addOption(schemaName);

        final Option connectionString = new Option("c", "connection", true, "The JDBC connection string");
        connectionString.setRequired(true);
        options.addOption(connectionString);

        final Option username = new Option("u", "username", true, "The DB username");
        username.setRequired(true);
        options.addOption(username);

        final Option password = new Option("p", "password", true,
                "The database password. Will be read from stdin if not provided");
        options.addOption(password);

        final Option redGClass = new Option("redGClass", true, "The class name of the generated RedG main class. Defaults to \"RedG\".");
        options.addOption(redGClass);

        return options;
    }

    private static void setLogLevel(Level level) {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        root.setLevel(level);
    }
}
