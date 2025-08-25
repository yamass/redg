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

package de.yamass.redg.plugin;

public class JpaProviderConfig {

    private String persistenceUnitName;
    private boolean useAsNameProvider;
    private boolean useAsDataTypeProvider;
    private String hibernateDialect;

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public void setPersistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    public boolean isUseAsNameProvider() {
        return useAsNameProvider;
    }

    public void setUseAsNameProvider(boolean useAsNameProvider) {
        this.useAsNameProvider = useAsNameProvider;
    }

    public boolean isUseAsDataTypeProvider() {
        return useAsDataTypeProvider;
    }

    public void setUseAsDataTypeProvider(boolean useAsDataTypeProvider) {
        this.useAsDataTypeProvider = useAsDataTypeProvider;
    }

    public String getHibernateDialect() {
        return hibernateDialect;
    }

    public void setHibernateDialect(String hibernateDialect) {
        this.hibernateDialect = hibernateDialect;
    }
}
