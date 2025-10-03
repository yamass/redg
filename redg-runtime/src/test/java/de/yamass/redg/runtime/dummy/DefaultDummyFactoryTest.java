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

import de.yamass.redg.runtime.AbstractRedG;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.spy;


class DefaultDummyFactoryTest {

	@Test
	void testGetDummy_createNew() throws Exception {
		AbstractRedG redG = spy(AbstractRedG.class);

		DefaultDummyFactory factory = new DefaultDummyFactory();
		Assertions.assertNotNull(factory);

		TestRedGEntity1 entity1 = factory.getDummy(redG, TestRedGEntity1.class);
		Assertions.assertNotNull(entity1);
		Assertions.assertTrue(factory.isDummy(entity1));

		//assure it was properly added
		Assertions.assertEquals(entity1, redG.findSingleEntity(TestRedGEntity1.class, e -> true));
	}

	@Test
	void testGetDummy_reuseCachedObject() throws Exception {
		AbstractRedG redG = spy(AbstractRedG.class);

		DefaultDummyFactory factory = new DefaultDummyFactory();
		Assertions.assertNotNull(factory);

		TestRedGEntity1 entity1 = factory.getDummy(redG, TestRedGEntity1.class);
		Assertions.assertNotNull(entity1);

		TestRedGEntity1 entity2 = factory.getDummy(redG, TestRedGEntity1.class);
		Assertions.assertNotNull(entity1);

		//assure it was properly added
		Assertions.assertEquals(entity1, entity2);
	}

	@Test
	void testGetDummy_transitiveDependencies() throws Exception {
		AbstractRedG redG = spy(AbstractRedG.class);

		DefaultDummyFactory factory = new DefaultDummyFactory();
		Assertions.assertNotNull(factory);

		TestRedGEntity2 entity2 = factory.getDummy(redG, TestRedGEntity2.class);
		Assertions.assertNotNull(entity2);

		Assertions.assertTrue(redG.findSingleEntity(TestRedGEntity1.class, e -> true) != null);
		Assertions.assertTrue(redG.findSingleEntity(TestRedGEntity2.class, e -> true) != null);
	}

	@Test
	void testGetDummy_NoFittingConstructor() throws Exception {
		AbstractRedG redG = spy(AbstractRedG.class);

		DefaultDummyFactory factory = new DefaultDummyFactory();
		Assertions.assertNotNull(factory);

		assertThatThrownBy(() -> factory.getDummy(redG, TestRedGEntity4.class))
				.isInstanceOf(DummyCreationException.class)
				.hasMessageContaining("Could not find a fitting constructor");
	}

	@Test
	void testGetDummy_NoFittingConstructor2() throws Exception {
		AbstractRedG redG = spy(AbstractRedG.class);

		DefaultDummyFactory factory = new DefaultDummyFactory();
		Assertions.assertNotNull(factory);

		assertThatThrownBy(() -> factory.getDummy(redG, TestRedGEntity6.class))
				.isInstanceOf(DummyCreationException.class)
				.hasMessageContaining("Could not find a fitting constructor");
	}

	@Test
	void testGetDummy_NoFittingConstructor3() throws Exception {
		AbstractRedG redG = spy(AbstractRedG.class);

		DefaultDummyFactory factory = new DefaultDummyFactory();
		Assertions.assertNotNull(factory);

		assertThatThrownBy(() -> factory.getDummy(redG, TestRedGEntity7.class))
				.isInstanceOf(DummyCreationException.class)
				.hasMessageContaining("Could not find a fitting constructor");
	}

	@Test
	void testGetDummy_InstantiationFails() throws Exception {
		AbstractRedG redG = spy(AbstractRedG.class);

		DefaultDummyFactory factory = new DefaultDummyFactory();
		Assertions.assertNotNull(factory);

		assertThatThrownBy(() -> factory.getDummy(redG, TestRedGEntity5.class))
				.isInstanceOf(DummyCreationException.class)
				.hasMessageContaining("Instantiation of the dummy failed");
	}

}
