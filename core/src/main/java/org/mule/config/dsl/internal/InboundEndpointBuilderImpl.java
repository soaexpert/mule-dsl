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
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.config.dsl.ExchangePattern;
import org.mule.config.dsl.internal.util.PropertyPlaceholder;
import org.mule.endpoint.EndpointURIEndpointBuilder;
import org.mule.endpoint.URIBuilder;

import static org.mule.config.dsl.internal.util.ExchangePatternUtil.convert;
import static org.mule.config.dsl.internal.util.Preconditions.checkNotEmpty;

public class InboundEndpointBuilderImpl implements Builder<InboundEndpoint> {

    private final String uri;
    private ExchangePattern exchangePattern = null;

    public InboundEndpointBuilderImpl(String uri, ExchangePattern exchangePattern) {
        this.uri = checkNotEmpty(uri, "uri");
        this.exchangePattern = exchangePattern;
    }

    @Override
    public InboundEndpoint build(MuleContext muleContext, Injector injector, PropertyPlaceholder placeholder) {
        org.mule.api.endpoint.EndpointBuilder internalEndpointBuilder = new EndpointURIEndpointBuilder(new URIBuilder(placeholder.replace(uri), muleContext));
        if (exchangePattern != null) {
            internalEndpointBuilder.setExchangePattern(convert(exchangePattern));
        }

        try {
            return internalEndpointBuilder.buildInboundEndpoint();
        } catch (Exception e) {
            //TODO handle
            throw new RuntimeException(e);
        }
    }
}
