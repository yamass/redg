package de.yamass.redg.tests;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Helpers {

    public static String getResourceAsString(String resourcePath) {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(Helpers.class.getClassLoader().getResourceAsStream(resourcePath)))) {
            return buffer.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            return "ERROR";
        }
    }

    public static Path getStringAsTempFile(final String data, final String fileName) throws Exception {
        Path tempDir = Files.createTempDirectory("redg");
        Path tempFile = tempDir.resolve(fileName);
        Files.write(tempFile, data.getBytes(StandardCharsets.UTF_8));
        return tempFile;
    }

    /**
     * Copies a resource to a temporary file and returns the associated file object.
     * Taken from http://stackoverflow.com/a/35465681
     *
     * @param resourcePath The resource
     * @return the temp file
     * @see <a href="http://stackoverflow.com/a/35465681">Answer on StackOverflow</a>
     */
    public static File getResourceAsFile(String resourcePath) {
        try {
            InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath);
            if (in == null) {
                return null;
            }

            File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
            tempFile.deleteOnExit();

            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                //copy stream
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void assertResultSet(final ResultSet resultSet, final Object... values) throws Exception {
        for (int i = 0; i < values.length; i++) {
            if (values[i] instanceof Integer) {
                assertEquals(values[i], resultSet.getInt(i + 1));
            } else if (values[i] instanceof Long) {
                assertEquals(values[i], resultSet.getLong(i + 1));
            } else if (values[i] instanceof Float) {
                assertEquals(values[i], resultSet.getFloat(i + 1));
            } else if (values[i] instanceof Double) {
                assertEquals(values[i], resultSet.getDouble(i + 1));
            } else if (values[i] instanceof String) {
                assertEquals(values[i], resultSet.getString(i + 1));
            } else if (values[i] instanceof Timestamp) {
                assertEquals(values[i], resultSet.getTimestamp(i + 1));
            }
        }
    }
}
