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


import de.yamass.redg.extractor.model.EntityModel;
import de.yamass.redg.extractor.tablemodelextractor.TableModelExtractor;
import de.yamass.redg.models.TableModel;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

class Test {

    public static final JsonMapper OBJECT_MAPPER = JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build();
    private static final String EXCHANGE_RATE = "{\"name\":\"ExchangeRate\",\"className\":\"MyExchangeRate\",\"sqlName\":\"EXCHANGE_RATE\",\"sqlFullName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"packageName\":\"com.demo.pkg\",\"foreignKeys\":[{\"javaTypeName\":\"MyExchangeRef\",\"name\":\"referenceIdExchangeRef\",\"references\":{\"REFERENCE_ID\":{\"primaryKeyAttributeName\":\"id\",\"localName\":\"referenceId\",\"localType\":\"java.math.BigDecimal\",\"dbName\":\"REFERENCE_ID\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"DECIMAL\",\"sqlTypeInt\":3}},\"notNull\":true},{\"javaTypeName\":\"MyExchangeRate\",\"name\":\"composite\",\"references\":{\"REFERENCE_ID\":{\"primaryKeyAttributeName\":\"referenceId\",\"localName\":\"referenceId\",\"localType\":\"java.math.BigDecimal\",\"dbName\":\"REFERENCE_ID\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"DECIMAL\",\"sqlTypeInt\":3},\"PREV_FIRST_NAME\":{\"primaryKeyAttributeName\":\"firstName\",\"localName\":\"prevFirstName\",\"localType\":\"java.lang.String\",\"dbName\":\"PREV_FIRST_NAME\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"VARCHAR\",\"sqlTypeInt\":12}},\"notNull\":false}],\"incomingForeignKeys\":[{\"referencingEntityName\":\"ExchangeRate\",\"referencingJavaTypeName\":\"MyExchangeRate\",\"referencingAttributeName\":\"composite\",\"attributeName\":\"exchangeRatesForComposite\",\"notNull\":true}],\"columns\":[{\"name\":\"id\",\"dbName\":\"ID\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"DECIMAL\",\"javaTypeName\":\"java.math.BigDecimal\",\"sqlTypeInt\":3,\"notNull\":true,\"partOfPrimaryKey\":true,\"partOfForeignKey\":false,\"explicitAttribute\":false,\"unique\":true,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.math.BigDecimal\"},{\"name\":\"referenceId\",\"dbName\":\"REFERENCE_ID\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"DECIMAL\",\"javaTypeName\":\"java.math.BigDecimal\",\"sqlTypeInt\":3,\"notNull\":true,\"partOfPrimaryKey\":false,\"partOfForeignKey\":true,\"explicitAttribute\":false,\"unique\":false,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.math.BigDecimal\"},{\"name\":\"firstName\",\"dbName\":\"FIRST_NAME\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"VARCHAR\",\"javaTypeName\":\"java.lang.String\",\"sqlTypeInt\":12,\"notNull\":false,\"partOfPrimaryKey\":false,\"partOfForeignKey\":false,\"explicitAttribute\":false,\"unique\":false,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.lang.String\"},{\"name\":\"prevFirstName\",\"dbName\":\"PREV_FIRST_NAME\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"VARCHAR\",\"javaTypeName\":\"java.lang.String\",\"sqlTypeInt\":12,\"notNull\":false,\"partOfPrimaryKey\":false,\"partOfForeignKey\":true,\"explicitAttribute\":false,\"unique\":false,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.lang.String\"}],\"joinTableSimplifierData\":{},\"nullableForeignKeys\":[{\"javaTypeName\":\"MyExchangeRate\",\"name\":\"composite\",\"references\":{\"REFERENCE_ID\":{\"primaryKeyAttributeName\":\"referenceId\",\"localName\":\"referenceId\",\"localType\":\"java.math.BigDecimal\",\"dbName\":\"REFERENCE_ID\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"DECIMAL\",\"sqlTypeInt\":3},\"PREV_FIRST_NAME\":{\"primaryKeyAttributeName\":\"firstName\",\"localName\":\"prevFirstName\",\"localType\":\"java.lang.String\",\"dbName\":\"PREV_FIRST_NAME\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"VARCHAR\",\"sqlTypeInt\":12}},\"notNull\":false}],\"notNullForeignKeys\":[{\"javaTypeName\":\"MyExchangeRef\",\"name\":\"referenceIdExchangeRef\",\"references\":{\"REFERENCE_ID\":{\"primaryKeyAttributeName\":\"id\",\"localName\":\"referenceId\",\"localType\":\"java.math.BigDecimal\",\"dbName\":\"REFERENCE_ID\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"DECIMAL\",\"sqlTypeInt\":3}},\"notNull\":true}],\"nullableIncomingForeignKeys\":[],\"primaryKeyColumns\":[{\"name\":\"id\",\"dbName\":\"ID\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"DECIMAL\",\"javaTypeName\":\"java.math.BigDecimal\",\"sqlTypeInt\":3,\"notNull\":true,\"partOfPrimaryKey\":true,\"partOfForeignKey\":false,\"explicitAttribute\":false,\"unique\":true,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.math.BigDecimal\"}],\"foreignKeyColumns\":[{\"name\":\"referenceId\",\"dbName\":\"REFERENCE_ID\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"DECIMAL\",\"javaTypeName\":\"java.math.BigDecimal\",\"sqlTypeInt\":3,\"notNull\":true,\"partOfPrimaryKey\":false,\"partOfForeignKey\":true,\"explicitAttribute\":false,\"unique\":false,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.math.BigDecimal\"},{\"name\":\"prevFirstName\",\"dbName\":\"PREV_FIRST_NAME\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"VARCHAR\",\"javaTypeName\":\"java.lang.String\",\"sqlTypeInt\":12,\"notNull\":false,\"partOfPrimaryKey\":false,\"partOfForeignKey\":true,\"explicitAttribute\":false,\"unique\":false,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.lang.String\"}],\"nonPrimaryKeyColumns\":[{\"name\":\"referenceId\",\"dbName\":\"REFERENCE_ID\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"DECIMAL\",\"javaTypeName\":\"java.math.BigDecimal\",\"sqlTypeInt\":3,\"notNull\":true,\"partOfPrimaryKey\":false,\"partOfForeignKey\":true,\"explicitAttribute\":false,\"unique\":false,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.math.BigDecimal\"},{\"name\":\"firstName\",\"dbName\":\"FIRST_NAME\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"VARCHAR\",\"javaTypeName\":\"java.lang.String\",\"sqlTypeInt\":12,\"notNull\":false,\"partOfPrimaryKey\":false,\"partOfForeignKey\":false,\"explicitAttribute\":false,\"unique\":false,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.lang.String\"},{\"name\":\"prevFirstName\",\"dbName\":\"PREV_FIRST_NAME\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"VARCHAR\",\"javaTypeName\":\"java.lang.String\",\"sqlTypeInt\":12,\"notNull\":false,\"partOfPrimaryKey\":false,\"partOfForeignKey\":true,\"explicitAttribute\":false,\"unique\":false,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.lang.String\"}],\"nonPrimaryKeyNonFKColumns\":[{\"name\":\"firstName\",\"dbName\":\"FIRST_NAME\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"VARCHAR\",\"javaTypeName\":\"java.lang.String\",\"sqlTypeInt\":12,\"notNull\":false,\"partOfPrimaryKey\":false,\"partOfForeignKey\":false,\"explicitAttribute\":false,\"unique\":false,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.lang.String\"}],\"nonForeignKeyColumns\":[{\"name\":\"id\",\"dbName\":\"ID\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"DECIMAL\",\"javaTypeName\":\"java.math.BigDecimal\",\"sqlTypeInt\":3,\"notNull\":true,\"partOfPrimaryKey\":true,\"partOfForeignKey\":false,\"explicitAttribute\":false,\"unique\":true,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.math.BigDecimal\"},{\"name\":\"firstName\",\"dbName\":\"FIRST_NAME\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"VARCHAR\",\"javaTypeName\":\"java.lang.String\",\"sqlTypeInt\":12,\"notNull\":false,\"partOfPrimaryKey\":false,\"partOfForeignKey\":false,\"explicitAttribute\":false,\"unique\":false,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.lang.String\"}],\"explicitAttributes\":[],\"nonExplicitAttributes\":[{\"name\":\"id\",\"dbName\":\"ID\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"DECIMAL\",\"javaTypeName\":\"java.math.BigDecimal\",\"sqlTypeInt\":3,\"notNull\":true,\"partOfPrimaryKey\":true,\"partOfForeignKey\":false,\"explicitAttribute\":false,\"unique\":true,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.math.BigDecimal\"},{\"name\":\"referenceId\",\"dbName\":\"REFERENCE_ID\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"DECIMAL\",\"javaTypeName\":\"java.math.BigDecimal\",\"sqlTypeInt\":3,\"notNull\":true,\"partOfPrimaryKey\":false,\"partOfForeignKey\":true,\"explicitAttribute\":false,\"unique\":false,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.math.BigDecimal\"},{\"name\":\"firstName\",\"dbName\":\"FIRST_NAME\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"VARCHAR\",\"javaTypeName\":\"java.lang.String\",\"sqlTypeInt\":12,\"notNull\":false,\"partOfPrimaryKey\":false,\"partOfForeignKey\":false,\"explicitAttribute\":false,\"unique\":false,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.lang.String\"},{\"name\":\"prevFirstName\",\"dbName\":\"PREV_FIRST_NAME\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"VARCHAR\",\"javaTypeName\":\"java.lang.String\",\"sqlTypeInt\":12,\"notNull\":false,\"partOfPrimaryKey\":false,\"partOfForeignKey\":true,\"explicitAttribute\":false,\"unique\":false,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.lang.String\"}],\"nonExplicitNonFKAttributes\":[{\"name\":\"id\",\"dbName\":\"ID\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"DECIMAL\",\"javaTypeName\":\"java.math.BigDecimal\",\"sqlTypeInt\":3,\"notNull\":true,\"partOfPrimaryKey\":true,\"partOfForeignKey\":false,\"explicitAttribute\":false,\"unique\":true,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.math.BigDecimal\"},{\"name\":\"firstName\",\"dbName\":\"FIRST_NAME\",\"dbTableName\":\"EXCHANGE_RATE\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_RATE\",\"sqlType\":\"VARCHAR\",\"javaTypeName\":\"java.lang.String\",\"sqlTypeInt\":12,\"notNull\":false,\"partOfPrimaryKey\":false,\"partOfForeignKey\":false,\"explicitAttribute\":false,\"unique\":false,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.lang.String\"}]}";
    private static final String EXCHANGE_REF = "{\"name\":\"ExchangeRef\",\"className\":\"MyExchangeRef\",\"sqlName\":\"EXCHANGE_REF\",\"sqlFullName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_REF\",\"packageName\":\"com.demo.pkg\",\"foreignKeys\":[],\"incomingForeignKeys\":[{\"referencingEntityName\":\"ExchangeRate\",\"referencingJavaTypeName\":\"MyExchangeRate\",\"referencingAttributeName\":\"referenceIdExchangeRef\",\"attributeName\":\"exchangeRatesForReferenceIdExchangeRef\",\"notNull\":true}],\"columns\":[{\"name\":\"id\",\"dbName\":\"ID\",\"dbTableName\":\"EXCHANGE_REF\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_REF\",\"sqlType\":\"DECIMAL\",\"javaTypeName\":\"java.math.BigDecimal\",\"sqlTypeInt\":3,\"notNull\":true,\"partOfPrimaryKey\":true,\"partOfForeignKey\":false,\"explicitAttribute\":false,\"unique\":true,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.math.BigDecimal\"},{\"name\":\"name\",\"dbName\":\"NAME\",\"dbTableName\":\"EXCHANGE_REF\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_REF\",\"sqlType\":\"VARCHAR\",\"javaTypeName\":\"java.lang.String\",\"sqlTypeInt\":12,\"notNull\":true,\"partOfPrimaryKey\":false,\"partOfForeignKey\":false,\"explicitAttribute\":false,\"unique\":false,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.lang.String\"}],\"joinTableSimplifierData\":{},\"nullableForeignKeys\":[],\"notNullForeignKeys\":[],\"nullableIncomingForeignKeys\":[],\"primaryKeyColumns\":[{\"name\":\"id\",\"dbName\":\"ID\",\"dbTableName\":\"EXCHANGE_REF\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_REF\",\"sqlType\":\"DECIMAL\",\"javaTypeName\":\"java.math.BigDecimal\",\"sqlTypeInt\":3,\"notNull\":true,\"partOfPrimaryKey\":true,\"partOfForeignKey\":false,\"explicitAttribute\":false,\"unique\":true,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.math.BigDecimal\"}],\"foreignKeyColumns\":[],\"nonPrimaryKeyColumns\":[{\"name\":\"name\",\"dbName\":\"NAME\",\"dbTableName\":\"EXCHANGE_REF\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_REF\",\"sqlType\":\"VARCHAR\",\"javaTypeName\":\"java.lang.String\",\"sqlTypeInt\":12,\"notNull\":true,\"partOfPrimaryKey\":false,\"partOfForeignKey\":false,\"explicitAttribute\":false,\"unique\":false,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.lang.String\"}],\"nonPrimaryKeyNonFKColumns\":[{\"name\":\"name\",\"dbName\":\"NAME\",\"dbTableName\":\"EXCHANGE_REF\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_REF\",\"sqlType\":\"VARCHAR\",\"javaTypeName\":\"java.lang.String\",\"sqlTypeInt\":12,\"notNull\":true,\"partOfPrimaryKey\":false,\"partOfForeignKey\":false,\"explicitAttribute\":false,\"unique\":false,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.lang.String\"}],\"nonForeignKeyColumns\":[{\"name\":\"id\",\"dbName\":\"ID\",\"dbTableName\":\"EXCHANGE_REF\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_REF\",\"sqlType\":\"DECIMAL\",\"javaTypeName\":\"java.math.BigDecimal\",\"sqlTypeInt\":3,\"notNull\":true,\"partOfPrimaryKey\":true,\"partOfForeignKey\":false,\"explicitAttribute\":false,\"unique\":true,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.math.BigDecimal\"},{\"name\":\"name\",\"dbName\":\"NAME\",\"dbTableName\":\"EXCHANGE_REF\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_REF\",\"sqlType\":\"VARCHAR\",\"javaTypeName\":\"java.lang.String\",\"sqlTypeInt\":12,\"notNull\":true,\"partOfPrimaryKey\":false,\"partOfForeignKey\":false,\"explicitAttribute\":false,\"unique\":false,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.lang.String\"}],\"explicitAttributes\":[],\"nonExplicitAttributes\":[{\"name\":\"id\",\"dbName\":\"ID\",\"dbTableName\":\"EXCHANGE_REF\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_REF\",\"sqlType\":\"DECIMAL\",\"javaTypeName\":\"java.math.BigDecimal\",\"sqlTypeInt\":3,\"notNull\":true,\"partOfPrimaryKey\":true,\"partOfForeignKey\":false,\"explicitAttribute\":false,\"unique\":true,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.math.BigDecimal\"},{\"name\":\"name\",\"dbName\":\"NAME\",\"dbTableName\":\"EXCHANGE_REF\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_REF\",\"sqlType\":\"VARCHAR\",\"javaTypeName\":\"java.lang.String\",\"sqlTypeInt\":12,\"notNull\":true,\"partOfPrimaryKey\":false,\"partOfForeignKey\":false,\"explicitAttribute\":false,\"unique\":false,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.lang.String\"}],\"nonExplicitNonFKAttributes\":[{\"name\":\"id\",\"dbName\":\"ID\",\"dbTableName\":\"EXCHANGE_REF\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_REF\",\"sqlType\":\"DECIMAL\",\"javaTypeName\":\"java.math.BigDecimal\",\"sqlTypeInt\":3,\"notNull\":true,\"partOfPrimaryKey\":true,\"partOfForeignKey\":false,\"explicitAttribute\":false,\"unique\":true,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.math.BigDecimal\"},{\"name\":\"name\",\"dbName\":\"NAME\",\"dbTableName\":\"EXCHANGE_REF\",\"dbFullTableName\":\"\\\"RT-TE\\\".PUBLIC.EXCHANGE_REF\",\"sqlType\":\"VARCHAR\",\"javaTypeName\":\"java.lang.String\",\"sqlTypeInt\":12,\"notNull\":true,\"partOfPrimaryKey\":false,\"partOfForeignKey\":false,\"explicitAttribute\":false,\"unique\":false,\"convenienceSetters\":[],\"primitiveType\":false,\"javaTypeAsClass\":\"java.lang.String\"}]}";

    @org.junit.jupiter.api.Test
    @Disabled
    void test() throws Exception {
        Class.forName("oracle.jdbc.OracleDriver");
        final Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE",
                "system", "oracle");
        // TODO: don't make this rely on any local path... then remove @Ignore and add real test here
        final Path classDir = Paths.get("D:\\redg\\redg-playground\\target\\test-classes");
        List<TableModel> tableModels = TableModelExtractor.extractTableModelFromClasses(classDir,
                "de.yamass.redg.generated", "G");


        final DataExtractor dataExtractor = new DataExtractor();
        List<EntityModel> entities = dataExtractor.extractAllData(connection, tableModels);
        System.out.println(new CodeGenerator().generateCode("hallo", "TestClass", "RedG", entities));

    }

    @org.junit.jupiter.api.Test
    void testExtractAllDataFromSelfReferencingCompositeForeignKey() throws SQLException {
        final Connection connectionMock = Mockito.mock(Connection.class);
        final Statement statementMock = Mockito.mock(Statement.class);
        final ResultSet resultSetMock = Mockito.mock(ResultSet.class);

        Mockito
                .doNothing()
                .when(connectionMock).setAutoCommit(false);

        Mockito
                .when(connectionMock.createStatement())
                .thenReturn(statementMock);

        Mockito
                .doNothing()
                .when(statementMock).setFetchSize(50);

        Mockito
                .doReturn(resultSetMock)
                .when(statementMock).executeQuery(Mockito.anyString());

        Mockito
                .doReturn(resultSetMock)
                .when(statementMock).executeQuery(Mockito.anyString());

        Mockito
                .doReturn(true, true, false, true, false)
                .when(resultSetMock).next();

        Mockito
                .doReturn("Lea")
                .when(resultSetMock).getObject("NAME");

        Mockito
                .doReturn(1)
                .when(resultSetMock).getObject("REFERENCE_ID");

        Mockito
                .doReturn(null, "Flo1")
                .when(resultSetMock).getObject("PREV_FIRST_NAME");

        Mockito
                .doReturn("Flo1", "Flo2")
                .when(resultSetMock).getObject("FIRST_NAME");

        Mockito
                .doReturn(11L, 12L, 1L)
                .when(resultSetMock).getObject("ID");

        List<EntityModel> entityModels = new DataExtractor().extractAllData(
                connectionMock, Arrays.asList(deserializeTableModel2(EXCHANGE_RATE), deserializeTableModel2(EXCHANGE_REF)));

        Assertions.assertEquals(Helpers.getResourceAsString("test-exchange-rate.java"), new CodeGenerator().generateCode("com.github.zemke", "RedG", "RedG", entityModels));
    }

    private TableModel deserializeTableModel2(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, TableModel.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
