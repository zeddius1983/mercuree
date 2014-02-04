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

package org.mercuree.evolution.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

/**
 * TODO: javadoc
 *
 * @author Alexander Valyugin
 */
@Configuration
@ConditionalOnBean(DataSource.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(EvolutionConfigurationProperties.class)
public class EvolutionAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(EvolutionAutoConfiguration.class);

    @Autowired
    private EvolutionConfigurationProperties properties;

    @Autowired
    private List<DataSource> dataSources;

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    @PostConstruct
    public void init() {
        // @DependsOn, @Estimated, @NeverChanges, @Update, @Rollback
        if (!properties.isEnabled()) {
            logger.info("Database evolution is currently disabled");
            return;
        }
        System.out.println(resourcePatternResolver);
        System.out.println(dataSources);
        System.out.println(properties.getDataSources());
        logger.info("Starting database evolution");
        logger.info("Database evolution is finished");
    }

}
