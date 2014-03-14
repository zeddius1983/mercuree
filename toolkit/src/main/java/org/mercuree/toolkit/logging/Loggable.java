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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * TODO: javadoc
 * <p/>
 *
 * @author Alexander Valyugin
 */
public class Loggable<T> implements Callable<T> {

    private final Callable<T> body;

    private final Method method;

    private final Object[] args;

    private String beforeFormat, afterFormat;

    public Loggable(Callable<T> body, Method method, Object[] args) {
        this.body = body;
        this.method = method;
        this.args = args;

        TraceFormat methodTraceFormat = method.getAnnotation(TraceFormat.class);
        Class<?> declaringClass = method.getDeclaringClass();
        TraceFormat classTraceFormat = declaringClass.getAnnotation(TraceFormat.class);
        Package declaringClassPackage = declaringClass.getPackage();
        TraceFormat packageTraceFormat = declaringClassPackage.getAnnotation(TraceFormat.class);

        TraceFormat[] traceFormatChain = {methodTraceFormat, classTraceFormat, packageTraceFormat};
        for (TraceFormat tf : traceFormatChain) {
            if (tf == null) continue;
            String thatAfterFormat = tf.afterFormat();
            if (this.afterFormat == null && !"".equals(thatAfterFormat)) {
                this.afterFormat = tf.afterFormat();
            }
        }
    }

    private void resolveTraceFormat(TraceFormat[] traceFormats) {
        for (TraceFormat tf : traceFormats) {
            if (tf == null) continue;
            String thatAfterFormat = tf.afterFormat();
            if (this.afterFormat == null && !"".equals(thatAfterFormat)) {
                this.afterFormat = tf.afterFormat();
            }
        }
    }

    @Override
    public T call() throws Exception {
        final Logger logger = LoggerFactory.getLogger(method.getDeclaringClass());
        try {
            return body.call();
        } catch (Exception e) {
            throw e;
        } finally {

        }
    }
}
