/*
 * ---------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.config.dsl.internal;

import org.mule.api.DefaultMuleException;
import org.mule.api.MuleContext;
import org.mule.api.component.Component;
import org.mule.api.lifecycle.Callable;
import org.mule.component.SimpleCallableJavaComponent;
import org.mule.config.dsl.ExecutorBuilder;
import org.mule.config.dsl.PipelineBuilder;

import static org.mule.config.dsl.internal.util.Preconditions.checkNotNull;

public class ExecutorBuilderImpl extends PipelineBuilderImpl implements ExecutorBuilder, Builder<Component> {

    private final Class<?> clazz;
    private final Object obj;

    ExecutorBuilderImpl(final PipelineBuilderImpl parentScope, MuleContext muleContext, Class<?> clazz) {
        super(muleContext, parentScope);
        this.clazz = clazz;
        this.obj = null;
    }

    ExecutorBuilderImpl(final PipelineBuilderImpl parentScope, MuleContext muleContext, Object obj) {
        super(muleContext, parentScope);
        this.obj = checkNotNull(obj, "obj");
        this.clazz = null;
    }

    @Override
    public Component build() {
        if (clazz != null) {
            if (Callable.class.isAssignableFrom(clazz) || java.util.concurrent.Callable.class.isAssignableFrom(clazz)) {
                try {
                    return new SimpleCallableJavaComponent(clazz);
                } catch (DefaultMuleException e) {
                    //TODO here
                    throw new RuntimeException(e);
                }
            } else {
                //ReflectionEntryPointResolver
                throw new RuntimeException("Not supported");
            }
        } else {
            if (obj instanceof Callable){
                return new SimpleCallableJavaComponent((Callable) obj);
            } else {
                throw new RuntimeException("Not supported");
            }
        }
    }

    @Override
    public PipelineBuilder asSingleton() {
        return null;
    }

    @Override
    public PipelineBuilder asPrototype() {
        return null;
    }
}