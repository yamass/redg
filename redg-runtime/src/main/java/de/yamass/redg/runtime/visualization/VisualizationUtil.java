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

package de.yamass.redg.runtime.visualization;

import de.yamass.redg.models.ColumnModel;
import de.yamass.redg.models.ForeignKeyColumnModel;
import de.yamass.redg.models.ForeignKeyModel;
import de.yamass.redg.models.TableModel;
import de.yamass.redg.runtime.RedGEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The static util class that generate the JSON representation of the object graph
 */
public class VisualizationUtil {

    public static String getVisualizationJson(List<RedGEntity> entities, Predicate<RedGEntity> dummyTester) {
        final RedGVisualization visualization = new RedGVisualization();

        final Map<RedGEntity, UUID> uuidMap = new HashMap<>();
        entities.forEach(entity -> {
            final UUID uuid = UUID.randomUUID();
            uuidMap.put(entity, uuid);
        });
        for (RedGEntity entity : entities) {
            try {
                final UUID uuid = uuidMap.get(entity);
                final RedGVisualizationObject object = new RedGVisualizationObject();
                object.setId(uuid.toString());

                final Method getTableModel = entity.getClass().getMethod("getTableModel");
                final TableModel tableModel = (TableModel) getTableModel.invoke(null);

                object.setType(tableModel.getName());
                object.setSqlName(tableModel.getSqlName());
                object.setExistingEntity(entity.getClass().getSimpleName().startsWith("Existing"));
                object.setDummy(dummyTester.test(entity));

                final Method getModifiedFields = entity.getClass().getMethod("getModifiedFields");
                final Set<String> modifiedFields = (Set<String>) getModifiedFields.invoke(entity);

                for (final ColumnModel column : tableModel.getColumns()) {
                    final Method getter = entity.getClass().getMethod(column.getName());
                    final Object value = getter.invoke(entity);
                    final RedGVisualizationField field = new RedGVisualizationField(column.getName(), column.getDbName(), (value != null) ? value.toString() : "null");
                    if (modifiedFields.contains(column.getName())) {
                        object.getExplicitFields().add(field);
                    } else {
                        object.getImplicitFields().add(field);
                    }
                }
                for (final ForeignKeyModel fkModel : tableModel.getForeignKeys()) {
                    Method getter = entity.getClass().getMethod(fkModel.getName());
                    RedGEntity refEntity = (RedGEntity) getter.invoke(entity);
                    String sqlNames = fkModel.getReferences().values().stream()
                            .map(ForeignKeyColumnModel::getDbName)
                            .collect(Collectors.joining(", "));
                    RedGVisualizationRelation relation = new RedGVisualizationRelation(
                            uuid.toString(),
                            (refEntity != null) ? uuidMap.get(refEntity).toString() : "null",
                            fkModel.getName(),
                            sqlNames
                    );
                    visualization.getRelationships().add(relation);
                }
                visualization.getObjects().add(object);
            } catch (Exception e) {
                throw new RuntimeException("Could not process entity", e);
            }
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(visualization);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not generate JSON", e);
        }
    }
}
