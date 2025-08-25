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

package de.yamass.redg.jpa;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ReflectionUtil {

	public static List<Field> findFields(Class<?> clazz, Predicate<Field> predicate) {
		List<Field> matchingFields = new ArrayList<>();
		Class<?> c = clazz;
		while (c != null) {
			for (Field field : c.getDeclaredFields()) {
				if (predicate.test(field)) {
					matchingFields.add(field);
				}
			}
			c = c.getSuperclass();
		}
		return matchingFields;
	}

	public static List<Method> findMethods(Class<?> clazz, Predicate<Method> predicate) {
		List<Method> matchingFields = new ArrayList<>();
		Class<?> c = clazz;
		while (c != null) {
			for (Method method : c.getDeclaredMethods()) {
				if (predicate.test(method)) {
					matchingFields.add(method);
				}
			}
			c = c.getSuperclass();
		}
		return matchingFields;
	}

	public static List<Field> findFieldsByAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
		return findFields(clazz, field -> field.isAnnotationPresent(annotationClass));
	}

	public static List<Method> findMethodsByAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
		return findMethods(clazz, method -> method.isAnnotationPresent(annotationClass));
	}

	public static List<? extends AnnotatedElement> findMembersByAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
		List<AnnotatedElement> result = new ArrayList<>();
		result.addAll(findFields(clazz, field -> field.isAnnotationPresent(annotationClass)));
		result.addAll(findMethods(clazz, method -> method.isAnnotationPresent(annotationClass)));
		return result;
	}

}
