/*
 * ---------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.config.dsl.internal;

import com.google.inject.Injector;
import org.mule.api.DefaultMuleException;
import org.mule.api.MuleContext;
import org.mule.api.component.Component;
import org.mule.api.lifecycle.Callable;
import org.mule.component.DefaultJavaComponent;
import org.mule.component.SimpleCallableJavaComponent;
import org.mule.config.dsl.ExecutorBuilder;
import org.mule.config.dsl.PipelineBuilder;
import org.mule.config.dsl.internal.util.InjectorUtil;
import org.mule.config.dsl.internal.util.PropertyPlaceholder;
import org.mule.object.PrototypeObjectFactory;
import org.mule.object.SingletonObjectFactory;

import static org.mule.config.dsl.internal.util.EntryPointResolverSetUtil.createDefaultResolverSet;
import static org.mule.config.dsl.internal.util.Preconditions.checkNotNull;

class ExecutorBuilderImpl<P extends PipelineBuilder<P>> extends PipelineBuilderImpl<P> implements ExecutorBuilder<P>, Builder<Component> {

    private Object obj;
    private final Class<?> clazz;
    private final Builder<?> builder;
    private InstanceType instanceType = InstanceType.PROTOTYPE;

    public ExecutorBuilderImpl(final P parentScope, Class<?> clazz) {
        super(parentScope);
        this.clazz = checkNotNull(clazz, "clazz");
        this.obj = null;
        this.builder = null;
    }

    public ExecutorBuilderImpl(final P parentScope, Object obj) {
        super(parentScope);
        this.obj = checkNotNull(obj, "obj");
        this.clazz = null;
        this.builder = null;
    }

    public ExecutorBuilderImpl(final P parentScope, Builder<?> builder) {
        super(parentScope);
        this.builder = checkNotNull(builder, "builder");
        this.clazz = null;
        this.obj = null;
    }

    @Override
    public P asSingleton() {
        this.instanceType = InstanceType.SINGLETON;
        return getThis();
    }

    @Override
    public P asPrototype() {
        this.instanceType = InstanceType.PROTOTYPE;
        return getThis();
    }

    private enum InstanceType {
        SINGLETON, PROTOTYPE
    }

    @Override
    public Component build(MuleContext muleContext, Injector injector, PropertyPlaceholder placeholder) {
        if (clazz != null) {
            if (Callable.class.isAssignableFrom(clazz)) {
                try {
                    return new SimpleCallableJavaComponent(clazz);
                } catch (DefaultMuleException e) {
                    //todo keep this root couse
                }
            } else {
                if (InjectorUtil.hasProvider(injector, clazz)) {
                    return new DefaultJavaComponent(new GuiceLookup(injector, clazz), createDefaultResolverSet(), null);
                } else {
                    if (instanceType.equals(ExecutorBuilderImpl.InstanceType.PROTOTYPE)) {
                        return new DefaultJavaComponent(new PrototypeObjectFactory(clazz), createDefaultResolverSet(), null);
                    } else if (instanceType.equals(ExecutorBuilderImpl.InstanceType.SINGLETON)) {
                        return new DefaultJavaComponent(new SingletonObjectFactory(clazz), createDefaultResolverSet(), null);
                    }
                }
            }
        } else {
            if (builder != null) {
                obj = builder.build(muleContext, injector, placeholder);
            }

            if (obj instanceof Callable) {
                return new SimpleCallableJavaComponent((Callable) obj);
            } else {
                return new DefaultJavaComponent(new SingletonObjectFactory(obj), createDefaultResolverSet(), null);
            }
        }
        throw new RuntimeException("Not supported");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected P getThis() {
        if (parentScope != null) {
            return (P) parentScope;
        }
        return (P) this;
    }
}