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

import java.lang.reflect.AnnotatedElement;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Persistence;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MappedSuperclassType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import javax.xml.bind.annotation.XmlSchema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.yamass.redg.generator.extractor.datatypeprovider.DataTypeProvider;
import de.yamass.redg.generator.extractor.datatypeprovider.DefaultDataTypeProvider;
import de.yamass.redg.generator.extractor.nameprovider.DefaultNameProvider;
import de.yamass.redg.generator.extractor.nameprovider.NameProvider;
import de.yamass.redg.generator.utils.NameUtils;
import de.yamass.redg.schema.model.Column;
import de.yamass.redg.schema.model.ForeignKey;
import de.yamass.redg.schema.model.ForeignKeyColumn;
import de.yamass.redg.schema.model.Table;

/**
 * @author Yann Massard (yamass@gmail.com)
 */
public class JpaMetamodelRedGProvider implements NameProvider, DataTypeProvider {
	private static final Logger LOG = LoggerFactory.getLogger(JpaMetamodelRedGProvider.class);

	private final Map<? extends Class<?>, ? extends ManagedType<?>> managedTypesByClass;
	private final Map<String, ManagedType> managedTypesByTableName = new HashMap<>();
	private final Map<QualifiedColumnName, SingularAttribute> singularAttributesByColumnName = new HashMap<>();
	private final Map<ForeignKeyRelation, SingularAttribute> singularAttributesByForeignKeyRelation = new HashMap<>();

	private NameProvider fallbackNameProvider = new DefaultNameProvider();
	private DataTypeProvider fallbackDataTypeProvider = new DefaultDataTypeProvider();

	public static JpaMetamodelRedGProvider fromPersistenceUnit(String perstistenceUnitName, String hibernateDialect) {
		Properties properties = new Properties();
		if (hibernateDialect != null) {
			properties.setProperty("hibernate.dialect", hibernateDialect);
		}
		setupBindInfoPackage();
		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(perstistenceUnitName, properties);

		EntityManager entityManager = entityManagerFactory.createEntityManager();
		return new JpaMetamodelRedGProvider(entityManager.getMetamodel());
	}

	public static JpaMetamodelRedGProvider fromPersistenceUnit(String perstistenceUnitName) {
		return fromPersistenceUnit(perstistenceUnitName, null);
	}

	/**
	 * workaround for hibernate bug from a comment of https://hibernate.atlassian.net/browse/HHH-12893
	 * (JAXB exception using SchemaExport with Envers and Java 9+)
	 */
	private static void setupBindInfoPackage() {
		String nsuri = "http://www.hibernate.org/xsd/orm/hbm";
		String packageInfoClassName = "org.hibernate.boot.jaxb.hbm.spi.package-info";
		try {
			final Class<?> packageInfoClass = Class
					.forName(packageInfoClassName);
			final XmlSchema xmlSchema = packageInfoClass
					.getAnnotation(XmlSchema.class);
			if (xmlSchema == null) {
				LOG.warn(MessageFormat.format(
						"Class [{0}] is missing the [{1}] annotation. Processing bindings will probably fail.",
						packageInfoClassName, XmlSchema.class.getName()));
			} else {
				final String namespace = xmlSchema.namespace();
				if (nsuri.equals(namespace)) {
					LOG.warn(MessageFormat.format(
							"Namespace of the [{0}] annotation does not match [{1}]. Processing bindings will probably fail.",
							XmlSchema.class.getName(), nsuri));
				}
			}
		} catch (ClassNotFoundException cnfex) {
			LOG.warn(MessageFormat.format(
					"Class [{0}] could not be found. Processing bindings will probably fail.",
					packageInfoClassName), cnfex);
		}
	}

	public JpaMetamodelRedGProvider(Metamodel metaModel) {
		managedTypesByClass = metaModel.getManagedTypes().stream()
				.filter(type -> type.getJavaType() != null)
				.collect(Collectors.groupingBy(Type::getJavaType, Collectors.collectingAndThen(Collectors.toList(), values -> values.get(0))));

		for (ManagedType<?> managedType : metaModel.getManagedTypes()) {
			Class<?> javaType = managedType.getJavaType();
			if (javaType == null) {
				// envers makes problems here.
				continue;
			}
			managedTypesByTableName.put(getTableName(javaType), managedType);

			boolean isMappedSuperClassType = managedType instanceof MappedSuperclassType;
			Inheritance inheritanceAnnotation = javaType.getAnnotation(Inheritance.class);
			InheritanceType inheritanceType = inheritanceAnnotation == null ? InheritanceType.SINGLE_TABLE : inheritanceAnnotation.strategy();

			if (managedType instanceof EntityType
					|| isMappedSuperClassType && (inheritanceType == InheritanceType.JOINED || inheritanceType == InheritanceType.SINGLE_TABLE)) {
				analyzeAttributes(managedType, getTableName(javaType));
			}

			ManagedType superClassManagedType = managedTypesByClass.get(javaType.getSuperclass());
			if (superClassManagedType != null && superClassManagedType.getJavaType() != null) {
				analyzeAttributes(managedType, getTableName(superClassManagedType.getJavaType()));
			}
		}
	}

	private void analyzeAttributes(ManagedType<?> managedType, String targetTableName) {
		managedType.getSingularAttributes().forEach(attribute -> {
			ManagedType<?> targetEntity = managedTypesByClass.get(attribute.getJavaType());
			if (targetEntity != null && attribute.getType() instanceof EmbeddableType) {
				analyzeAttributes((EmbeddableType) attribute.getType(), targetTableName);
			} else if (targetEntity != null && attribute.getType() instanceof IdentifiableType) { // this is a relation
				Map<String, String> referenceColumnNamesMap =
						getReferenceColumnNamesMapForReferenceAttribute(attribute, targetEntity);
				singularAttributesByForeignKeyRelation.put(
						new ForeignKeyRelation(targetTableName, getTableName(targetEntity.getJavaType()), referenceColumnNamesMap),
						attribute
				);
			} else {
				String columnName = getSingularAttributeColumnName(attribute);
				singularAttributesByColumnName.put(new QualifiedColumnName(targetTableName, columnName), attribute);
			}
		});
	}

	private Map<String, String> getReferenceColumnNamesMapForReferenceAttribute(SingularAttribute<?, ?> attribute, ManagedType<?> targetEntity) {
		List<String> idAttributeNames = targetEntity.getSingularAttributes().stream()
                .filter(this::isIdAttribute)
                .map(this::getSingularAttributeColumnName)
                .collect(Collectors.toList());

		JoinColumns joinColumnsAnnotation =
                ((AnnotatedElement) attribute.getJavaMember()).getAnnotation(JoinColumns.class);
		JoinColumn joinColumnAnnotation =
                ((AnnotatedElement) attribute.getJavaMember()).getAnnotation(JoinColumn.class);
		JoinColumn[] joinColumns = joinColumnsAnnotation != null ? joinColumnsAnnotation.value() :
                joinColumnAnnotation != null ? new JoinColumn[]{joinColumnAnnotation} : null;
		Map<String, String> referenceColumnNamesMap;
		if (joinColumns != null) {
            referenceColumnNamesMap = Arrays.stream(joinColumns)
                    .collect(Collectors.toMap(JoinColumn::name, joinColumn ->
                            joinColumn.referencedColumnName().length() > 0 ? joinColumn.referencedColumnName() :
                                    idAttributeNames.get(0)));
        } else {
            referenceColumnNamesMap = idAttributeNames.stream()
                    .collect(Collectors.toMap(idAttributeName -> attribute.getName().toUpperCase() + "_"
                            + idAttributeName, idAttributeName -> idAttributeName));
        }
		return referenceColumnNamesMap;
	}

	@Override
	public String getClassNameForTable(Table table) {
		ManagedType managedType = managedTypesByTableName.get(table.name().toUpperCase());
		return managedType != null ? managedType.getJavaType().getSimpleName() : fallbackNameProvider.getClassNameForTable(table);
	}

	@Override
	public String getMethodNameForColumn(Column column, Table table) {
		String tableName = table.name().toUpperCase();
		SingularAttribute singularAttribute = singularAttributesByColumnName.get(new QualifiedColumnName(tableName, column.name().toUpperCase()));
		return singularAttribute != null ? singularAttribute.getName() : fallbackNameProvider.getMethodNameForColumn(column, table);
	}

	@Override
	public String getMethodNameForForeignKeyColumn(ForeignKeyColumn foreignKeyColumn, Table sourceTable) {
		// For foreign key columns, we need to find the corresponding JPA attribute
		// First, try to find it as a regular column
		String tableName = sourceTable.name().toUpperCase();
		SingularAttribute singularAttribute = singularAttributesByColumnName.get(new QualifiedColumnName(tableName, foreignKeyColumn.sourceColumn().name().toUpperCase()));
		
		// Find the foreign key that contains this column
		Optional<ForeignKey> foreignKeyOptional = sourceTable.outgoingForeignKeys().stream()
				.filter(fk -> fk.columns().stream()
						.anyMatch(fkCol -> fkCol.sourceColumn().equals(foreignKeyColumn.sourceColumn()) 
								&& fkCol.targetColumn().equals(foreignKeyColumn.targetColumn())))
				.findFirst();
		
		if (foreignKeyOptional.isPresent()) {
			ForeignKey foreignKey = foreignKeyOptional.get();
			String referenceMethodName = getMethodNameForReference(foreignKey);
			String primaryKeyColumnName = foreignKeyColumn.targetColumn().name();
			String primaryKeyJavaName = DefaultNameProvider.convertToJavaName(primaryKeyColumnName);
			return referenceMethodName + NameUtils.firstCharacterToUpperCase(primaryKeyJavaName);
		}
		
		// If not found, fall back to default behavior
		return fallbackNameProvider.getMethodNameForForeignKeyColumn(foreignKeyColumn, sourceTable);
	}

    private boolean isIdAttribute(SingularAttribute attribute) {
		return ((AnnotatedElement) attribute.getJavaMember()).isAnnotationPresent(Id.class);
	}

	private String getSingularAttributeColumnName(SingularAttribute attribute) {
		javax.persistence.Column columnAnnotation = ((AnnotatedElement) attribute.getJavaMember()).getAnnotation(javax.persistence.Column.class);
		if (columnAnnotation != null && columnAnnotation.name().length() > 0) {
			return columnAnnotation.name().toUpperCase();
		} else {
			return attribute.getName().toUpperCase();
		}
	}

	@Override
	public String getMethodNameForReference(ForeignKey foreignKey) {
		ForeignKeyRelation foreignKeyRelation = toForeignKeyRelation(foreignKey);
		SingularAttribute singularAttribute = singularAttributesByForeignKeyRelation.get(foreignKeyRelation);
		return singularAttribute != null ? singularAttribute.getName() : fallbackNameProvider.getMethodNameForReference(foreignKey);
	}

	private ForeignKeyRelation toForeignKeyRelation(ForeignKey foreignKey) {
		if (foreignKey.columns().isEmpty()) {
			throw new IllegalArgumentException("Foreign key must have at least one column");
		}
		ForeignKeyColumn firstColumn = foreignKey.columns().get(0);
		String referencingTableName = foreignKey.sourceTable().name().toUpperCase();
		String referencedTableName = foreignKey.targetTable().name().toUpperCase();
		Map<String, String> referenceColumnNamesMap = foreignKey.columns().stream()
				.collect(Collectors.toMap(
						fkCol -> fkCol.sourceColumn().name().toUpperCase(),
						fkCol -> fkCol.targetColumn().name().toUpperCase()
				));
		return new ForeignKeyRelation(referencingTableName, referencedTableName, referenceColumnNamesMap);
	}

	@Override
	public String getMethodNameForIncomingForeignKey(ForeignKey foreignKey) {
		return fallbackNameProvider.getMethodNameForIncomingForeignKey(foreignKey);
	}

	private static String getTableName(Class javaType) {
		javax.persistence.Table tableAnnotation = ((AnnotatedElement) javaType).getAnnotation(javax.persistence.Table.class);
		javax.persistence.Entity entityAnnotation = ((AnnotatedElement) javaType).getAnnotation(javax.persistence.Entity.class);
		boolean tableNameSet = tableAnnotation != null && tableAnnotation.name().length() > 0;
		boolean entityNameSet = entityAnnotation != null && entityAnnotation.name().length() > 0;
		return tableNameSet ? tableAnnotation.name().toUpperCase() : entityNameSet ? entityAnnotation.name().toUpperCase() : javaType.getSimpleName().toUpperCase();
	}

	@Override
	public String getCanonicalDataTypeName(Column column, Table table) {
		SingularAttribute singularAttribute;
		// Check if this column is part of a foreign key
		Optional<ForeignKeyColumn> foreignKeyColumnOptional = table.outgoingForeignKeys().stream()
				.flatMap(foreignKey -> foreignKey.columns().stream())
				.filter(fkCol -> fkCol.sourceColumn().equals(column))
				.findFirst();

		if (foreignKeyColumnOptional.isPresent()) {
			ForeignKeyColumn fkCol = foreignKeyColumnOptional.get();
			// For foreign key columns, use the target column's type
			SingularAttribute targetSingularAttribute =
					singularAttributesByColumnName.get(new QualifiedColumnName(
							fkCol.targetColumn().table().name().toUpperCase(),
							fkCol.targetColumn().name().toUpperCase()
					));
			if (targetSingularAttribute != null) {
				return targetSingularAttribute.getJavaType().getCanonicalName();
			} else {
				LOG.warn("Could not find target singular attribute for column {}.{}", table.name(), column.name());
				return fallbackDataTypeProvider.getCanonicalDataTypeName(column, table);
			}
		} else {
			singularAttribute =
					singularAttributesByColumnName.get(new QualifiedColumnName(table.name().toUpperCase(), column.name().toUpperCase()));
			if (singularAttribute != null) {
				return singularAttribute.getJavaType().getCanonicalName();
			} else {
				return fallbackDataTypeProvider.getCanonicalDataTypeName(column, table);
			}
		}
	}

	private static class QualifiedColumnName {
		String tableName;
		String columnName;

		public QualifiedColumnName(String tableName, String columnName) {
			this.tableName = tableName;
			this.columnName = columnName;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			QualifiedColumnName that = (QualifiedColumnName) o;
			return Objects.equals(tableName, that.tableName) &&
					Objects.equals(columnName, that.columnName);
		}

		@Override
		public int hashCode() {
			return Objects.hash(tableName, columnName);
		}

		@Override
		public String toString() {
			return tableName + "." + columnName;
		}
	}

	private static class ForeignKeyRelation {
		private final String referencingTableName;
		private final String referencedTableName;
		private final Map<String, String> referencingColumn2ReferencedColumn;

		public ForeignKeyRelation(String referencingTableName, String referencedTableName, Map<String, String> referencingColumn2ReferencedColumn) {
			this.referencingTableName = referencingTableName;
			this.referencedTableName = referencedTableName;
			this.referencingColumn2ReferencedColumn = referencingColumn2ReferencedColumn;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			ForeignKeyRelation that = (ForeignKeyRelation) o;
			return Objects.equals(referencingTableName, that.referencingTableName) &&
					Objects.equals(referencedTableName, that.referencedTableName) &&
					Objects.equals(referencingColumn2ReferencedColumn, that.referencingColumn2ReferencedColumn);
		}

		@Override
		public int hashCode() {
			return Objects.hash(referencingTableName, referencedTableName, referencingColumn2ReferencedColumn);
		}
	}

	public void setFallbackNameProvider(NameProvider fallbackNameProvider) {
		this.fallbackNameProvider = fallbackNameProvider;
	}

	public void setFallbackDataTypeProvider(DataTypeProvider fallbackDataTypeProvider) {
		this.fallbackDataTypeProvider = fallbackDataTypeProvider;
	}
}
