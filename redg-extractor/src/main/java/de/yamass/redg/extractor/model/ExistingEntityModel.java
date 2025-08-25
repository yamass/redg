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

package de.yamass.redg.extractor.model;

import de.yamass.redg.models.ColumnModel;
import de.yamass.redg.models.TableModel;

import java.util.ArrayList;
import java.util.List;

public class ExistingEntityModel extends EntityModel {

    public ExistingEntityModel(final TableModel tableModel) {
        super(tableModel);
    }

    public List<ValueModel> getPrimaryKeyValues() {
        List<ColumnModel> pkColumns = getTableModel().getPrimaryKeyColumns();
        List<ValueModel> result = new ArrayList<>(pkColumns.size());

        for (final ColumnModel cm : pkColumns) {
            result.add(getValues().get(cm.getName()));
        }

        return result;
    }

    public static ExistingEntityModel fromEntityModel(final EntityModel entityModel) {
        ExistingEntityModel existingEntityModel = new ExistingEntityModel(entityModel.getTableModel());
        existingEntityModel.setVariableName(entityModel.getVariableName());
        existingEntityModel.setValues(entityModel.getValues());
        existingEntityModel.setNotNullRefs(entityModel.getNotNullRefs());
        existingEntityModel.setNullableRefs(entityModel.getNullableRefs());
        return existingEntityModel;
    }

    @Override
    public boolean isGenerateAsNewEntity() {
        return false;
    }
}
