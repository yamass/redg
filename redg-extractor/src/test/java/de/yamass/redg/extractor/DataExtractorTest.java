package de.yamass.redg.extractor;

import de.yamass.redg.models.TableModel;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;


public class DataExtractorTest {

    @Test
    public void testGetFullSqlName() throws Exception {
        final TableModel tm1 = new TableModel();
        tm1.setSqlFullName("TEST.TABLE");
        assertThat(wrapGetFullTableName(new DataExtractor(), tm1)).isEqualTo("TEST.TABLE");

        final TableModel tm2 = new TableModel();
        tm2.setSqlFullName("TEST.\"TABLE\"");
        assertThat(wrapGetFullTableName(new DataExtractor(), tm2)).isEqualTo("TEST.\"TABLE\"");

        final TableModel tm3 = new TableModel();
        tm3.setSqlFullName("\"TEST\".TABLE");
        assertThat(wrapGetFullTableName(new DataExtractor(), tm3)).isEqualTo("\"TEST\".TABLE");

        final TableModel tm4 = new TableModel();
        tm4.setSqlFullName("TEST.TABLE");
        final DataExtractor de1 = new DataExtractor();
        de1.setSqlSchemaName("TEST2");
        assertThat(wrapGetFullTableName(de1, tm4)).isEqualTo("TEST2.TABLE");

        final TableModel tm5 = new TableModel();
        tm5.setSqlFullName("TEST.TABLE");
        final DataExtractor de2 = new DataExtractor();
        de2.setSqlSchemaName("TEST2.");
        assertThat(wrapGetFullTableName(de2, tm5)).isEqualTo("TEST2.TABLE");

        final TableModel tm6 = new TableModel();
        tm6.setSqlFullName("TEST.\"TABLE\"");
        final DataExtractor de3 = new DataExtractor();
        de3.setSqlSchemaName("TEST2");
        assertThat(wrapGetFullTableName(de3, tm6)).isEqualTo("TEST2.\"TABLE\"");

        final TableModel tm7 = new TableModel();
        tm7.setSqlFullName("TEST.\"TABLE\"");
        final DataExtractor de4 = new DataExtractor();
        de4.setSqlSchemaName("TEST2.");
        assertThat(wrapGetFullTableName(de4, tm7)).isEqualTo("TEST2.\"TABLE\"");
    }

    private String wrapGetFullTableName(final DataExtractor extractor, final TableModel tm) throws Exception {
        final Method m = extractor.getClass().getDeclaredMethod("getFullTableName", TableModel.class);
        m.setAccessible(true);
        return (String) m.invoke(extractor, tm);
    }

}