/*
 * Copyright (c) 2014 the original author or authors.
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

package org.mercuree.toolkit.logging;

/**
 * TODO: javadoc
 * <p/>
 *
 * @author Alexander Valyugin
 */
public class LoggingConfiguration {

    private static final String formatIn = "[$flow][$elapsed] $methodName($arg1.name)";

    private static final String formatOut = "[$flow][$elapsed] $methodName($arg1.name) -> $returnValue";

    public static LoggingConfiguration setDefaultFormat(String format) {
        return null;
    }

}