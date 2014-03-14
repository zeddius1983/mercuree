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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;

/**
 * TODO: javadoc
 * <p/>
 *
 * @author Alexander Valyugin
 */
@Aspect
public class LoggingAspect {

    private final ThreadLocal<Stack> invocationStack = new ThreadLocal<Stack>();

    @Pointcut("execution(* *(..)) && " + "@annotation(log)")
    private void logAnnotatedMethod(Log log) {
    }

    @Around("logAnnotatedMethod(log)")
    public Object aroundInvoke(ProceedingJoinPoint pjp, Log log) throws Exception {
        Logger logger = getLoggerForClass(((MethodSignature)pjp.getSignature()).getMethod().getDeclaringClass());

        return null;
    }

    Logger getLoggerForClass(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

}
