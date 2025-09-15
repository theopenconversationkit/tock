package org.pac4j.vertx.http;

import io.vertx.ext.web.RoutingContext;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.exception.http.WithContentAction;
import org.pac4j.core.exception.http.WithLocationAction;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.vertx.VertxWebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeremy Prime
 * @since 2.0.0
 */
public class VertxHttpActionAdapter implements HttpActionAdapter {

    public static final VertxHttpActionAdapter INSTANCE = new VertxHttpActionAdapter();

    private static final Logger LOG = LoggerFactory.getLogger(VertxHttpActionAdapter.class);

    @Override
    public Object adapt(final HttpAction action, final WebContext context) {
        if (action != null) {
            final RoutingContext routingContext = ((VertxWebContext)context).getVertxRoutingContext();
            int code = action.getCode();
            LOG.debug("Adapting action: {}", code);

            if (code < 400) {
                routingContext.response().setStatusCode(code);
            } else {
                routingContext.fail(code);
            }

            if (action instanceof WithLocationAction) {
                final WithLocationAction withLocationAction = (WithLocationAction) action;
                context.setResponseHeader(HttpConstants.LOCATION_HEADER, withLocationAction.getLocation());
                routingContext.response().end();

            } else if (action instanceof WithContentAction) {
                final WithContentAction withContentAction = (WithContentAction) action;
                final String content = withContentAction.getContent();

                if (content != null) {
                    routingContext.response().setChunked(true);
                    routingContext.response().write(content);
                    routingContext.response().end();
                }
            }
            return null;
        }

        throw new TechnicalException("No action provided");
    }
}
