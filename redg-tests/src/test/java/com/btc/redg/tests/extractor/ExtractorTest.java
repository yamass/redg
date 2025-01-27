package com.btc.redg.tests.extractor;

import com.btc.redg.extractor.CodeGenerator;
import com.btc.redg.extractor.DataExtractor;
import com.btc.redg.extractor.model.EntityModel;
import com.btc.redg.generated.extractor.GConfiguration;
import com.btc.redg.generated.extractor.GUser;
import com.btc.redg.generator.extractor.DatabaseManager;
import com.btc.redg.runtime.AbstractRedG;
import com.btc.redg.tests.Helpers;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.Before;
import org.junit.Test;

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


public class ExtractorTest {

    @Before
    public void initializeDatabase() throws Exception {
        Class.forName("org.h2.Driver");
        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:redg-extractor-source", "", "");
        assertThat(dataSource).isNotNull();
        final File sqlSchemaFile = Helpers.getResourceAsFile("extractor-schema.sql");
        DatabaseManager.executePreparationScripts(dataSource, new File[]{sqlSchemaFile});
        final File sqlDataFile = Helpers.getResourceAsFile("extractor-data.sql");
        DatabaseManager.executePreparationScripts(dataSource, new File[]{sqlDataFile});
    }

    @Test
    public void test() throws Exception {
        DataSource dataSource = JdbcConnectionPool.create("jdbc:h2:mem:redg-extractor-source", "", "");

        final DataExtractor dataExtractor = new DataExtractor();
        dataExtractor.setSqlSchemaName("\"REDG-EXTRACTOR-SOURCE\".PUBLIC");
        List<EntityModel> models = dataExtractor.extractAllData(dataSource, Arrays.asList(GUser.getTableModel(), GConfiguration.getTableModel()));

        final String code = new CodeGenerator().generateCode(
                "com.btc.redg.generated.extractor",
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
        DatabaseManager.executePreparationScripts(targetDataSource, new File[]{sqlSchemaFile});

        final Connection targetConnection = targetDataSource.getConnection();
        redG.insertDataIntoDatabase(targetConnection);

        final Statement statement = targetConnection.createStatement();
        final ResultSet userSet = statement.executeQuery("select * from \"user\" order by id");
        assertThat(userSet.next()).isTrue();
        Helpers.assertResultSet(userSet, 0, "admin", "redg@btc-ag.com");
        assertThat(userSet.next()).isTrue();
        Helpers.assertResultSet(userSet, 1, "max", "redg@btc-ag.com");
        assertThat(userSet.next()).isTrue();
        Helpers.assertResultSet(userSet, 2, "maria", "redg@btc-ag.com");
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
