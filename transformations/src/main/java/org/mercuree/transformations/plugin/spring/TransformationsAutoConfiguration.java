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

import org.mercuree.transformations.core.TransformationsMaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.List;

/**
 * TODO: javadoc
 *
 * @author Alexander Valyugin
 */
@Configuration
@ConditionalOnBean(DataSource.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(TransformationsConfigurationProperties.class)
public class TransformationsAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(TransformationsAutoConfiguration.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TransformationsConfigurationProperties properties;

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        logger.info("Datasource: {}", properties.getDataSource());
//        DataSource dataSource = applicationContext.getBean(properties.getDataSource(), DataSource.class);
        logger.info("DataSource found {}", dataSource);
        TransformationsMaster master = new TransformationsMaster(dataSource);
        logger.info("Starting database transformations");
        master.run();
        logger.info("Database transformations is finished");
    }

}
