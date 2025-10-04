package de.yamass.redg.generator.testutil;

import de.yamass.redg.generator.DatabaseType;
import org.assertj.core.util.Files;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.extension.*;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SharedDatabasesInvocationProvider implements TestTemplateInvocationContextProvider {

	private static final Set<DatabaseType> globallyEnabledDatabases = parseEnabledDatabases(System.getProperty("test.databases", "postgres,mariadb,h2"));

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		return true;
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
		List<DatabaseType> enabledDatabases = context.getTestMethod()
				.flatMap(m -> Optional.ofNullable(m.getAnnotation(Databases.class)))
				.map(a -> List.copyOf(Arrays.asList(a.value())))
				.or(() -> context.getTestClass()
						.flatMap(c -> Optional.ofNullable(c.getAnnotation(Databases.class)))
						.map(a -> List.copyOf(Arrays.asList(a.value()))))
				.orElse(List.of(DatabaseType.H2, DatabaseType.POSTGRES, DatabaseType.MARIADB));

		enabledDatabases = enabledDatabases.stream()
				.filter(globallyEnabledDatabases::contains)
				.collect(Collectors.toList());

		return enabledDatabases.stream().map(this::toInvocation);
	}

	private TestTemplateInvocationContext toInvocation(DatabaseType type) {
		return containerBasedInvocation(type);
	}

	private TestTemplateInvocationContext containerBasedInvocation(DatabaseType databaseType) {
		DataSource dataSource = switch (databaseType) {
			case POSTGRES -> DataSourceFactory.create(TestDatabaseContainers.postgres());
			case MARIADB -> DataSourceFactory.create(TestDatabaseContainers.mariadb());
			case H2 -> {
				yield JdbcConnectionPool.create("jdbc:h2:file:" + Files.newTemporaryFile().getAbsolutePath() + ";AUTO_SERVER=TRUE", "", "");
			}
			default -> throw new IllegalArgumentException(databaseType + " database type is not supported yet!");
		};

		return new TestTemplateInvocationContext() {
			@Override
			public String getDisplayName(int invocationIndex) {
				return databaseType.name();
			}

			@Override
			public List<Extension> getAdditionalExtensions() {
				return List.of(
						new TestInstancePostProcessor() {
							@Override
							public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
								Arrays.stream(testInstance.getClass().getDeclaredFields())
										.filter(f -> f.isAnnotationPresent(DbContext.class))
										.forEach(f -> {
											f.setAccessible(true);
											try {
												f.set(testInstance, getObjectToInject(f.getType()));
											} catch (IllegalAccessException e) {
												throw new RuntimeException(e);
											}
										});
							}
						},
						new BeforeEachCallback() {
							@Override
							public void beforeEach(ExtensionContext context) throws Exception {
								ArrayList<String> allScripts = new ArrayList<>();

								allScripts.add("de/yamass/redg/generator/sql/drop-db.sql");

								context.getTestClass()
										.flatMap(c -> Optional.ofNullable(c.getAnnotation(Scripts.class)))
										.map(a -> List.copyOf(Arrays.asList(a.value())))
										.ifPresent(allScripts::addAll);
								context.getTestMethod()
										.flatMap(m -> Optional.ofNullable(m.getAnnotation(Scripts.class)))
										.map(a -> List.copyOf(Arrays.asList(a.value())))
										.ifPresent(allScripts::addAll);

								SqlScripts.executeScripts(dataSource, databaseType, allScripts);
							}
						},
						new ParameterResolver() {
							@Override
							public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
								var type = parameterContext.getParameter().getType();
								return DataSource.class.isAssignableFrom(type);
							}

							@Override
							public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
								return getObjectToInject(parameterContext.getParameter().getType());
							}
						}
				);
			}

			private Object getObjectToInject(Class<?> type) {
				if (DataSource.class.isAssignableFrom(type)) {
					return dataSource;
				} else if (type == DatabaseType.class) {
					return databaseType;
				} else {
					throw new IllegalArgumentException("Unsupported @DbContext type: " + type.getName());
				}
			}
		};
	}

	private static Set<DatabaseType> parseEnabledDatabases(String csv) {
		return Arrays.stream(csv.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.map(s -> DatabaseType.valueOf(s.toUpperCase()))
				.collect(Collectors.toSet());
	}
}