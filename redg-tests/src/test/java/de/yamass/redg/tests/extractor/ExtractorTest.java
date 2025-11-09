package de.yamass.redg.tests.extractor;

import de.yamass.redg.extractor.CodeGenerator;
import de.yamass.redg.extractor.DataExtractor;
import de.yamass.redg.extractor.model.EntityModel;
import de.yamass.redg.generated.extractor.GConfiguration;
import de.yamass.redg.generated.extractor.GUser;
import de.yamass.redg.runtime.AbstractRedG;
import de.yamass.redg.tests.Helpers;
import de.yamass.redg.util.ScriptRunner;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class ExtractorTest {

    @BeforeEach
    public void initializeDatabase() throws Exception {
        Class.forName("org.h2.Driver");
        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:redg-extractor-source", "", "");
        assertThat(dataSource).isNotNull();
        final File sqlSchemaFile = Helpers.getResourceAsFile("extractor-schema.sql");
        ScriptRunner.executeScripts(dataSource, new File[]{sqlSchemaFile});
        final File sqlDataFile = Helpers.getResourceAsFile("extractor-data.sql");
        ScriptRunner.executeScripts(dataSource, new File[]{sqlDataFile});
    }

    @Test
    void test() throws Exception {
        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:redg-extractor-source", "", "");

        final DataExtractor dataExtractor = new DataExtractor();
        dataExtractor.setSqlSchemaName("\"REDG-EXTRACTOR-SOURCE\".PUBLIC");
        List<EntityModel> models = dataExtractor.extractAllData(dataSource, Arrays.asList(GUser.getTableModel(), GConfiguration.getTableModel()));

        final String code = new CodeGenerator().generateCode(
                "de.yamass.redg.generated.extractor",
                "RedG",
                "ExtractedDataSet",
                models);

        final Path codeFile = Helpers.getStringAsTempFile(code, "ExtractedDataSet.java");
        assertThat(codeFile).exists();
        assertThat(codeFile).hasContent(code);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler).isNotNull();
        compiler.run(null, null, null, codeFile.toAbsolutePath().toString());

        URLClassLoader cl = URLClassLoader.newInstance(new URL[] {codeFile.getParent().toUri().toURL()});
        Class<?> cls = Class.forName("ExtractedDataSet", true, cl);
        Object dataSet = cls.newInstance();
        assertThat(dataSet).isNotNull();
        Method method = cls.getMethod("createDataSet");
        assertThat(method).isNotNull();
        AbstractRedG redG = (AbstractRedG) method.invoke(dataSet);
        assertThat(redG).isNotNull();
        assertThat(redG.getEntities()).hasSize(12);

        final DataSource targetDataSource = JdbcConnectionPool.create("jdbc:h2:mem:redg-extractor-target", "", "");
        final File sqlSchemaFile = Helpers.getResourceAsFile("extractor-schema.sql");
        ScriptRunner.executeScripts(targetDataSource, new File[]{sqlSchemaFile});

        final Connection targetConnection = targetDataSource.getConnection();
        redG.insertDataIntoDatabase(targetConnection);

        final Statement statement = targetConnection.createStatement();
        final ResultSet userSet = statement.executeQuery("select * from \"user\" order by id");
        assertThat(userSet.next()).isTrue();
        Helpers.assertResultSet(userSet, 0, "admin", "redg@yamass.de");
        assertThat(userSet.next()).isTrue();
        Helpers.assertResultSet(userSet, 1, "max", "redg@yamass.de");
        assertThat(userSet.next()).isTrue();
        Helpers.assertResultSet(userSet, 2, "maria", "redg@yamass.de");
        assertThat(userSet.next()).isFalse();

        final ResultSet configSet = statement.executeQuery("select * from configuration order by user_id, name");
        assertThat(configSet.next()).isTrue();
        Helpers.assertResultSet(configSet, 0, "confirm_all_actions", "true");
        assertThat(configSet.next()).isTrue();
        Helpers.assertResultSet(configSet, 0, "is_admin", "true");
        assertThat(configSet.next()).isTrue();
        Helpers.assertResultSet(configSet, 0, "show_dashboard", "true");

        assertThat(configSet.next()).isTrue();
        Helpers.assertResultSet(configSet, 1, "confirm_all_actions", "true");
        assertThat(configSet.next()).isTrue();
        Helpers.assertResultSet(configSet, 1, "is_admin", "false");
        assertThat(configSet.next()).isTrue();
        Helpers.assertResultSet(configSet, 1, "show_dashboard", "false");

        assertThat(configSet.next()).isTrue();
        Helpers.assertResultSet(configSet, 2, "confirm_all_actions", "false");
        assertThat(configSet.next()).isTrue();
        Helpers.assertResultSet(configSet, 2, "is_admin", "false");
        assertThat(configSet.next()).isTrue();
        Helpers.assertResultSet(configSet, 2, "show_dashboard", "true");

        assertThat(configSet.next()).isFalse();

    }
}
