package org.pac4j.vertx.handler.impl;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.CallbackLogic;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.vertx.VertxFrameworkParameters;
import org.pac4j.vertx.VertxWebContext;
import org.pac4j.vertx.http.VertxHttpActionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallbackHandler implements Handler<RoutingContext> {

    private static final Logger LOG = LoggerFactory.getLogger(CallbackHandler.class);

    private final Vertx vertx;
    private final SessionStore sessionStore;
    private final Config config;

    // Config elements which are all optional
    private final String defaultUrl;
    private final Boolean renewSession;
    private final String  defaultClient;

    public CallbackHandler(final Vertx vertx,
                           final SessionStore sessionStore,
                           final Config config,
                           final CallbackHandlerOptions options) {
        this.vertx = vertx;
        this.sessionStore = sessionStore;
        this.config = config;
        this.defaultUrl    = options.getDefaultUrl();
        this.renewSession  = options.getRenewSession();
        this.defaultClient = options.getDefaultClient();
    }

    @Override
    public void handle(final RoutingContext rc) {
        final CallbackLogic callbackLogic =
                (config.getCallbackLogic() !=null) ? config.getCallbackLogic() : DefaultCallbackLogic.INSTANCE;
        final HttpActionAdapter httpAdapter =
                (config.getHttpActionAdapter() !=null) ? config.getHttpActionAdapter() : VertxHttpActionAdapter.INSTANCE;

        final VertxWebContext webCtx = new VertxWebContext(rc, sessionStore);

        vertx.<Void>executeBlocking(() -> {
            callbackLogic.perform(
                    config,
                    defaultUrl,
                    renewSession,
                    defaultClient,
                    new VertxFrameworkParameters(webCtx.getVertxRoutingContext())
            );
            return null;
        }).onComplete(ar -> {
            if (ar.failed()) {
                rc.fail(new TechnicalException(ar.cause()));
            } else {
                LOG.debug("Callback handled for {}", rc.request().path());
            }
        });
    }
}
