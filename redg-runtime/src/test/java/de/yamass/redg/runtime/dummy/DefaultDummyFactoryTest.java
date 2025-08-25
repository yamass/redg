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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;


public class DefaultDummyFactoryTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testGetDummy_createNew() throws Exception {
        AbstractRedG redG = spy(AbstractRedG.class);

        DefaultDummyFactory factory = new DefaultDummyFactory();
        assertNotNull(factory);

        TestRedGEntity1 entity1 = factory.getDummy(redG, TestRedGEntity1.class);
        assertNotNull(entity1);
        assertTrue(factory.isDummy(entity1));

        //assure it was properly added
        assertEquals(entity1, redG.findSingleEntity(TestRedGEntity1.class, e -> true));
    }

    @Test
    public void testGetDummy_reuseCachedObject() throws Exception {
        AbstractRedG redG = spy(AbstractRedG.class);

        DefaultDummyFactory factory = new DefaultDummyFactory();
        assertNotNull(factory);

        TestRedGEntity1 entity1 = factory.getDummy(redG, TestRedGEntity1.class);
        assertNotNull(entity1);

        TestRedGEntity1 entity2 = factory.getDummy(redG, TestRedGEntity1.class);
        assertNotNull(entity1);

        //assure it was properly added
        assertEquals(entity1, entity2);
    }

    @Test
    public void testGetDummy_transitiveDependencies() throws Exception {
        AbstractRedG redG = spy(AbstractRedG.class);

        DefaultDummyFactory factory = new DefaultDummyFactory();
        assertNotNull(factory);

        TestRedGEntity2 entity2 = factory.getDummy(redG, TestRedGEntity2.class);
        assertNotNull(entity2);

        assertTrue(redG.findSingleEntity(TestRedGEntity1.class, e -> true) != null);
        assertTrue(redG.findSingleEntity(TestRedGEntity2.class, e -> true) != null);
    }

    @Test
    public void testGetDummy_NoFittingConstructor() throws Exception {
        thrown.expect(DummyCreationException.class);
        thrown.expectMessage("Could not find a fitting constructor");
        AbstractRedG redG = spy(AbstractRedG.class);

        DefaultDummyFactory factory = new DefaultDummyFactory();
        assertNotNull(factory);

        factory.getDummy(redG, TestRedGEntity4.class);
    }

    @Test
    public void testGetDummy_NoFittingConstructor2() throws Exception {
        thrown.expect(DummyCreationException.class);
        thrown.expectMessage("Could not find a fitting constructor");
        AbstractRedG redG = spy(AbstractRedG.class);

        DefaultDummyFactory factory = new DefaultDummyFactory();
        assertNotNull(factory);

        factory.getDummy(redG, TestRedGEntity6.class);
    }

    @Test
    public void testGetDummy_NoFittingConstructor3() throws Exception {
        thrown.expect(DummyCreationException.class);
        thrown.expectMessage("Could not find a fitting constructor");
        AbstractRedG redG = spy(AbstractRedG.class);

        DefaultDummyFactory factory = new DefaultDummyFactory();
        assertNotNull(factory);

        factory.getDummy(redG, TestRedGEntity7.class);
    }

    @Test
    public void testGetDummy_InstantiationFails() throws Exception {
        thrown.expect(DummyCreationException.class);
        thrown.expectMessage("Instantiation of the dummy failed");
        AbstractRedG redG = spy(AbstractRedG.class);

        DefaultDummyFactory factory = new DefaultDummyFactory();
        assertNotNull(factory);

        factory.getDummy(redG, TestRedGEntity5.class);
    }

}
