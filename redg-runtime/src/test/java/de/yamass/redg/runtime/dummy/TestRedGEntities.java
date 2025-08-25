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

package de.yamass.redg.runtime.dummy;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;

import de.yamass.redg.runtime.AbstractRedG;
import de.yamass.redg.runtime.AttributeMetaInfo;
import de.yamass.redg.runtime.RedGEntity;

class TestRedGEntity1 implements RedGEntity {

    public TestRedGEntity1(AbstractRedG redG) {

    }

    public TestRedGEntity1() {

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
        return Collections.emptyList();
    }
}

class TestRedGEntity2 implements RedGEntity {


    public TestRedGEntity2(AbstractRedG redG, String explicitAttribute, TestRedGEntity1 entity1, TestRedGEntity1 entity2) {
        // public constructor
    }

    TestRedGEntity2(AbstractRedG redG, TestRedGEntity1 entity1, TestRedGEntity1 entity2) {
        // dummy constructor
    }

    public TestRedGEntity2(int whatever) {

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
        return Collections.emptyList();
    }
}

class TestRedGEntity3 implements RedGEntity {


    private TestRedGEntity3(AbstractRedG redG, TestRedGEntity1 entity1, TestRedGEntity1 entity2) {

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
        return Collections.emptyList();
    }
}

class TestRedGEntity4 implements RedGEntity {


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
        return Collections.emptyList();
    }
}

class TestRedGEntity5 implements RedGEntity {

    public TestRedGEntity5(AbstractRedG redG) throws IllegalAccessException {
        throw new IllegalAccessException("Test");
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
        return Collections.emptyList();
    }
}

class TestRedGEntity6 implements RedGEntity {

    public TestRedGEntity6(final SecureRandom definitelyWrongParameter) {

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
        return Collections.emptyList();
    }
}

class TestRedGEntity7 implements RedGEntity {

    public TestRedGEntity7(final AbstractRedG redg, final SecureRandom definitelyWrongParameter) {

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
        return Collections.emptyList();
    }
}