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

package org.mercuree.transformations.plugin.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO: javadoc
 * <p/>
 *
 * @author Alexander Valyugin
 */
@ConfigurationProperties("mercuree.transformations")
public class TransformationsConfigurationProperties {

    private boolean enabled = true;

    private String controlTableName = "EVOLUTION_INFO";

    private String dataSource;

    private String transformationsPath;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getControlTableName() {
        return controlTableName;
    }

    public void setControlTableName(String controlTableName) {
        this.controlTableName = controlTableName;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getTransformationsPath() {
        return transformationsPath;
    }

    public void setTransformationsPath(String transformationsPath) {
        this.transformationsPath = transformationsPath;
    }
}
