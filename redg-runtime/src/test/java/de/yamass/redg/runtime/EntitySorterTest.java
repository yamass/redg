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

package de.yamass.redg.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class EntitySorterTest {
    @Test
    public void entitiesAreSortedByDepths() throws Exception {
        EntitySorter entitySorter = new EntitySorter();

        Entity leafEntity1 = new Entity("leafEntity1");
        Entity leafEntity2 = new Entity("leafEntity2");
        Entity leafEntity3 = new Entity("leafEntity3");
        Entity nonLeaf1 = new Entity("nonLeaf1", leafEntity1, leafEntity2);
        Entity nonLeaf2 = new Entity("nonLeaf2", leafEntity1, leafEntity3);
        Entity nonLeaf3 = new Entity("nonLeaf3", nonLeaf1);
        Entity superDependentNode = new Entity("superDependentNode", leafEntity1, nonLeaf1, nonLeaf3);

        List<RedGEntity> sortedEntities = entitySorter.sortEntities(Arrays.asList(
                superDependentNode,
                nonLeaf3,
                nonLeaf2,
                nonLeaf1,
                leafEntity1,
                leafEntity2,
                leafEntity3
        ));

        Assert.assertEquals(Arrays.asList(
                leafEntity1,
                leafEntity2,
                leafEntity3,
                nonLeaf2,
                nonLeaf1,
                nonLeaf3,
                superDependentNode
        ), sortedEntities);
    }

    @Test
    public void entitiesSelfReferenceTest() throws Exception {
        Entity root = new Entity("root");
        root.addDependency(root);
        Entity leaf = new Entity("leaf", root);
        Entity leafWithSelf = new Entity("leafWithSelf", root, leaf);
        leafWithSelf.addDependency(leafWithSelf);

        List<RedGEntity> sorted = EntitySorter.sortEntities(Arrays.asList(
                leaf,
                leafWithSelf,
                root));

        Assert.assertEquals(Arrays.asList(
                root,
                leaf,
                leafWithSelf
        ), sorted);
    }

    @Test
    public void existingEntitiesFirst() throws Exception {
        EntitySorter entitySorter = new EntitySorter();

        Entity existingEntity1 = new ExistingEntity("existingEntity1");
        Entity leafEntity1 = new Entity("leafEntity1");
        Entity existingEntity2 = new ExistingEntity("existingEntity2");
        Entity leafEntity2 = new Entity("leafEntity2");
        leafEntity2.setDependencies(null);
        Entity existingEntity3 = new ExistingEntity("existingEntity3");
        Entity nonLeaf1 = new Entity("nonLeaf1", leafEntity1, leafEntity2);
        Entity existingEntity4 = new ExistingEntity("existingEntity4");


        List<RedGEntity> sortedEntities = entitySorter.sortEntities(Arrays.asList(
                existingEntity1,
                nonLeaf1,
                existingEntity2,
                leafEntity1,
                existingEntity3,
                leafEntity2,
                existingEntity4
        ));

        Assert.assertEquals(Arrays.asList(
                existingEntity1,
                existingEntity2,
                existingEntity3,
                existingEntity4,
                leafEntity1,
                leafEntity2,
                nonLeaf1
        ), sortedEntities);
    }

    private static class Entity implements RedGEntity {
        private String identifier;
        private List<RedGEntity> dependencies;

        public Entity(String identifier, RedGEntity... dependencies) {
            this.identifier = identifier;
            this.dependencies = Arrays.asList(dependencies);
        }

        @Override
        public String getSQLString() {
            return null;
        }

        @Override
        public String getPreparedStatementString() {
            return null;
        }

        @Override
        public Object[] getPreparedStatementValues() {
            return new Object[0];
        }

        @Override
        public AttributeMetaInfo[] getPreparedStatementValuesMetaInfos() {
            return new AttributeMetaInfo[0];
        }

        @Override
        public List<RedGEntity> getDependencies() {
            return dependencies;
        }

        @Override
        public String toString() {
            return identifier;
        }

        public void addDependency(RedGEntity dep) {
            if (!(this.dependencies instanceof ArrayList)) {
                this.dependencies = new ArrayList<>(this.dependencies);
            }
            this.dependencies.add(dep);
        }

        public void setDependencies(List<RedGEntity> deps) {
            this.dependencies = deps;
        }
    }

    private static class ExistingEntity extends Entity {
        public ExistingEntity(String identifier, RedGEntity... dependencies) {
            super(identifier, dependencies);
        }
    }
}