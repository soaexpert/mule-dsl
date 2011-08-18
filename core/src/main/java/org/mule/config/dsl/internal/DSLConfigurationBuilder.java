/*
 * ---------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.config.dsl.internal;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.mule.api.MuleContext;
import org.mule.api.config.ConfigurationBuilder;
import org.mule.api.config.ConfigurationException;
import org.mule.config.dsl.Module;

import java.util.ArrayList;
import java.util.List;

import static org.mule.config.dsl.internal.util.Preconditions.checkContentsNotNull;

public class DSLConfigurationBuilder implements ConfigurationBuilder {

    boolean isConfigured = false;
    final Module[] modules;

    public DSLConfigurationBuilder(Module... modules) {
        checkContentsNotNull(modules, "modules");
        if (modules.length < 1) {
            throw new IllegalArgumentException("At least one module should be provided.");
        }

        this.modules = modules;
    }

    @Override
    public synchronized void configure(MuleContext muleContext) throws ConfigurationException {
        if (isConfigured) {
            return;
        }

        final DefaultCatalogImpl myCatalog = new DefaultCatalogImpl();
        final List<com.google.inject.Module> guiceModules = new ArrayList<com.google.inject.Module>();

        for (final Module module : modules) {
            module.configure(myCatalog);
            if (module instanceof com.google.inject.Module) {
                guiceModules.add((com.google.inject.Module) module);
            }
        }

        final MuleContextConfig muleContextConfig;
        if (guiceModules.size() > 0) {
            final Injector injector = Guice.createInjector(guiceModules);
            muleContextConfig = new MuleContextConfig(myCatalog, injector, muleContext);
        } else {
            muleContextConfig = new MuleContextConfig(myCatalog, null, muleContext);
        }

        muleContextConfig.config();

        isConfigured = true;
    }

    @Override
    public boolean isConfigured() {
        return isConfigured;
    }
}
