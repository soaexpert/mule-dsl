/*
 * $Id: 20811 2011-03-29 09:56:20Z porcelli $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.config.domain;

public interface Flow extends Lifecycle, Pipeline {

    void addInboundEndpoint(InboundEndpoint inbound);

    void addOutboundEndpoint(OutboundEndpoint outbound);

    void addRouter(Router router);

    void serExceptionHandler(ExceptionHandler handler);

    public interface FlowResponse extends Response {
        void addOutboundEndpoint(OutboundEndpoint outbound);

        void addRouter(Router router);
    }
}
