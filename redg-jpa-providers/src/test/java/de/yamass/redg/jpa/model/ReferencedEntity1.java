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

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class ReferencedEntity1 {

	@Id
	private long id;

	@ManyToOne
	private ReferencedEntity2 refEntity2;

	@ManyToOne
	@JoinColumns(value = {
			@JoinColumn(name = "REF_2_ID1", referencedColumnName = "ID1"),
			@JoinColumn(name = "REF_2_ID2", referencedColumnName = "ID_2")
	})
	private ReferencedEntity2 referencedEntity2WithExpliciteJoinColumns;

	@ManyToOne
	private SubEntityJoined1 manyToOneToSubEntityJoined1;

	@OneToMany
	private List<SubEntityJoined1> oneToManyToSubEntityJoined1;

	@ManyToMany
	private List<SubEntityJoined1> manyToManyToSubEntityJoined1;

	@ManyToOne
	private SubEntitySingleTable1 manyToOneToSubEntitySingleTable1;

	@OneToMany
	private List<SubEntitySingleTable1> oneToManyToSubEntitySingleTable1;

	@ManyToMany
	private List<SubEntitySingleTable1> manyToManyToSubEntitySingleTable1;

	@ManyToOne
	private SubEntityTablePerClass1 manyToOneToSubEntityTablePerClass1;

	@OneToMany
	private List<SubEntityTablePerClass1> oneToManyToSubEntityTablePerClass1;

	@ManyToMany
	private List<SubEntityTablePerClass1> manyToManyToSubEntityTablePerClass1;

	@ManyToOne
	private SubEntity subEntity;

	@Embedded
	private TestEmbeddable testEmbeddable;


}
