package org.pac4j.vertx;

import io.vertx.ext.web.RoutingContext;
import org.pac4j.core.context.FrameworkParameters;

/** Minimal FrameworkParameters carrier for Vert.x. */
public final class VertxFrameworkParameters implements FrameworkParameters {
    private final RoutingContext routingContext;
    public VertxFrameworkParameters(final RoutingContext routingContext) {
        this.routingContext = routingContext;
    }
    public RoutingContext routingContext() { return routingContext; }
}
