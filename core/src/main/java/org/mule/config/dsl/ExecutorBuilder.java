/*
 * ---------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.config.dsl;

import java.lang.annotation.Annotation;

public interface ExecutorBuilder<P extends PipelineBuilder<P>> extends PipelineBuilder<P> {

    InnerArgsExecutorBuilder<P> methodAnnotatedWith(Class<? extends Annotation> annotationType);

    InnerArgsExecutorBuilder<P> methodAnnotatedWith(Annotation annotation);

    P withoutArgs();

    public static interface InnerArgsExecutorBuilder<P extends PipelineBuilder<P>> extends PipelineBuilder<InnerArgsExecutorBuilder<P>> {
        P withoutArgs();

        <E extends ExpressionEvaluatorBuilder> P args(E... args);
    }
}

