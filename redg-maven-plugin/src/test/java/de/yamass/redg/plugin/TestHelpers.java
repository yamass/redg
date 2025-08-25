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

import org.codehaus.plexus.util.xml.Xpp3Dom;


public class TestHelpers {

    public static Xpp3Dom getArrayParameters(String name, String... params) {
        Xpp3Dom dom = new Xpp3Dom(name);
        for (String param : params) {
            Xpp3Dom paramNode = new Xpp3Dom("param");
            paramNode.setValue(param);
            dom.addChild(paramNode);
        }
        return dom;
    }

    public static Xpp3Dom getParameters(String name, String value) {
        Xpp3Dom dom = new Xpp3Dom(name);
        dom.setValue(value);
        return dom;
    }
}
