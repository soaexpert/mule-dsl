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
import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.NamedObject;
import org.mule.api.agent.Agent;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.context.MuleContextAware;
import org.mule.api.transformer.Transformer;
import org.mule.api.transport.Connector;
import org.mule.config.dsl.ConfigurationException;
import org.mule.routing.MessageFilter;
import org.mule.transport.AbstractConnector;

import java.util.Map;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.mule.config.dsl.internal.GuiceRegistry.GUICE_INJECTOR_REF;
import static org.mule.config.dsl.internal.util.Preconditions.checkNotNull;
import static org.mule.config.dsl.internal.util.PrivateAccessorHack.executeHiddenMethod;
import static org.mule.config.dsl.internal.util.PrivateAccessorHack.setPrivateFieldValue;

/**
 * Utility class that, based on given {@link org.mule.config.dsl.Catalog} and
 * {@link Injector} setups a given {@link MuleContext}.
 *
 * @author porcelli
 */
public class MuleContextConfig {

    private final DefaultCatalogImpl catalog;
    private final Injector injector;
    private final MuleContext muleContext;

    /**
     * @param catalog     the dsl catalog
     * @param injector    the guice injector, null is allowed
     * @param muleContext the muleContext to be configured
     * @throws NullPointerException if {@code catalog} or {@code muleContext} param are null
     */
    public MuleContextConfig(final DefaultCatalogImpl catalog, final Injector injector, final MuleContext muleContext) throws NullPointerException {
        this.catalog = checkNotNull(catalog, "catalog");
        this.muleContext = checkNotNull(muleContext, "muleContext");
        this.injector = injector;
    }

    /**
     * Configures the {@link org.mule.api.MuleContext} with dsl {@link org.mule.config.dsl.Catalog} information.
     *
     * @return the mule context configured
     * @throws org.mule.config.dsl.ConfigurationException
     *          if fails to register any configuration from catalog
     */
    public MuleContext config() throws ConfigurationException {
        try {
            if (injector != null) {
                muleContext.getRegistry().registerObject(GUICE_INJECTOR_REF, injector);
                final GuiceRegistry registry = new GuiceRegistry(muleContext, injector);
                muleContext.addRegistry(registry);
            }

            configureGlobalComponents();

            configureGlobalTransformers();

            configureGlobalFilters();

            configureFlows();

            return muleContext;
        } catch (final Exception e) {
            throw new ConfigurationException("Failed to configure mule context.", e);
        }
    }

    private void configureGlobalComponents() {
        for (final Map.Entry<String, Object> entry : catalog.getComponents().entrySet()) {
            try {
                if (entry.getValue() instanceof NamedObject && isEmpty(((NamedObject) entry.getValue()).getName())) {
                    ((NamedObject) entry.getValue()).setName(entry.getKey());
                }

                if (entry.getValue() instanceof MuleContextAware) {
                    ((MuleContextAware) entry.getValue()).setMuleContext(muleContext);
                }

                if (entry.getValue() instanceof Connector) {
                    setPrivateFieldValue(AbstractConnector.class, entry.getValue(), "muleContext", muleContext);
                    executeHiddenMethod(AbstractConnector.class, entry.getValue(), "updateCachedNotificationHandler");
                    muleContext.getRegistry().registerConnector((Connector) entry.getValue());
                } else if (entry.getValue() instanceof Agent) {
                    muleContext.getRegistry().registerAgent((Agent) entry.getValue());
                } else {
                    muleContext.getRegistry().registerObject(entry.getKey(), entry.getValue());
                }
            } catch (MuleException e) {
                throw new ConfigurationException("Failed to configure a Component.", e);
            }
        }
    }

    private void configureGlobalTransformers() {
        for (final TransformerBuilderImpl transformerBuilder : catalog.getGlobalTransformers().values()) {
            final Transformer transformer = transformerBuilder.build(muleContext, catalog.getPropertyPlaceholder());
            if (transformer != null) {
                try {
                    transformer.setName(transformerBuilder.getName());
                    muleContext.getRegistry().registerTransformer(transformer);
                } catch (final MuleException e) {
                    throw new ConfigurationException("Failed to configure a Global Transformer.", e);
                }
            }
        }
    }

    private void configureGlobalFilters() {
        for (final FilterBuilderImpl filterBuilder : catalog.getGlobalFilters().values()) {
            final MessageFilter filter = filterBuilder.build(muleContext, catalog.getPropertyPlaceholder());
            if (filter != null) {
                try {
                    muleContext.getRegistry().registerObject(filterBuilder.getName(), filter);
                } catch (final MuleException e) {
                    throw new ConfigurationException("Failed to configure a Global Filter.", e);
                }
            }
        }
    }

    private void configureFlows() {
        for (final FlowBuilderImpl activeFlowBuilder : catalog.getFlows().values()) {
            final FlowConstruct flow = activeFlowBuilder.build(muleContext, catalog.getPropertyPlaceholder());
            if (flow != null) {
                try {
                    muleContext.getRegistry().registerFlowConstruct(flow);
                } catch (final MuleException e) {
                    throw new ConfigurationException("Failed to configure a Flows.", e);
                }
            }
        }
    }

}
