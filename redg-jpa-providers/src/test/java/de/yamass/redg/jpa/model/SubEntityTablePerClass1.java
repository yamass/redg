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

package de.yamass.redg.jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class SubEntityTablePerClass1 extends ManagedSuperClassTablePerClass {

	private Long subTablePerClassImpliciteNameColumn;

	@Column(name = "SUB_TABLE_PER_CLASS_EXPLICITE_NAME_COLUMN")
	private Long subTablePerClassExpliciteNameColumn;

	@ManyToOne
	private ReferencedEntity1 subTablePerClassManyToOne;

	@OneToMany
	private List<ReferencedEntity1> subTablePerClassOneToMany;

	@ManyToMany
	private List<ReferencedEntity1> subTablePerClassManyToMany;

}
