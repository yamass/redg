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

package de.yamass.redg.generator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;


public class Helpers {

	public static boolean resourceExists(String resourcePath) {
		return ClassLoader.getSystemClassLoader().getResource(resourcePath) != null;
	}

	public static InputStream getResourceAsStream(String resourcePath) {
		return ClassLoader.getSystemClassLoader().getResourceAsStream(resourcePath);
	}

	public static Reader getResourceAsReader(String resourcePath) {
		InputStream stream = getResourceAsStream(resourcePath);
		if (stream == null) {
			return null;
		} else {
			return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
		}
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
			var in = getResourceAsStream(resourcePath);
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

	public static String getResourceAsString(String resourcePath) {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(Helpers.class.getClassLoader().getResourceAsStream(resourcePath)))) {
			return buffer.lines().collect(Collectors.joining(System.lineSeparator()));
		} catch (IOException e) {
			return "ERROR";
		}
	}
}
