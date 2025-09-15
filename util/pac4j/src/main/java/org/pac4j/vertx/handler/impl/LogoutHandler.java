package org.pac4j.vertx.handler.impl;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.pac4j.core.engine.LogoutLogic;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.vertx.VertxFrameworkParameters;
import org.pac4j.vertx.VertxWebContext;
import org.pac4j.vertx.http.VertxHttpActionAdapter;

/**
 * Implementation of a handler for handling pac4j user logout
 * @author Jeremy Prime
 * @since 2.0.0
 */
public class LogoutHandler implements Handler<RoutingContext> {

    private final String defaultUrl;
    private final String logoutUrlPattern;
    private final Config config;

    private final Vertx vertx;
    private final SessionStore sessionStore;
    private final boolean localLogout;
    private final boolean destroySession;
    private final boolean centralLogout;

    /**
     * Construct based on the option values provided
     *
     * @param vertx the vertx API
     * @param sessionStore the session store
     * @param options - the options to configure this handler
     * @param config the pac4j configuration
     */
    public LogoutHandler(final Vertx vertx,
                         final SessionStore sessionStore,
                         final LogoutHandlerOptions options,
                         final Config config) {
        this.vertx = vertx;
        this.sessionStore = sessionStore;
        this.config = config;

        this.defaultUrl = options.getDefaultUrl();
        this.logoutUrlPattern = options.getLogoutUrlPattern();
        this.localLogout = options.isLocalLogout();
        this.destroySession = options.isDestroySession();
        this.centralLogout = options.isCentralLogout();
    }

    @Override
    public void handle(final RoutingContext rc) {
        final LogoutLogic logoutLogic =
                (config.getCallbackLogic() !=null) ? config.getLogoutLogic() : DefaultLogoutLogic.INSTANCE;
        final HttpActionAdapter httpAdapter =
                (config.getHttpActionAdapter() !=null) ? config.getHttpActionAdapter() : VertxHttpActionAdapter.INSTANCE;

        final VertxWebContext webCtx = new VertxWebContext(rc, sessionStore);

        vertx.<Void>executeBlocking(() -> {
            logoutLogic.perform(
                    config,
                    defaultUrl,
                    logoutUrlPattern,
                    localLogout,
                    destroySession,
                    centralLogout,
                    new VertxFrameworkParameters(webCtx.getVertxRoutingContext())
            );
            return null;
        }).onComplete(ar -> {
            if (ar.failed()) {
                rc.fail(new TechnicalException(ar.cause()));
            } else {
               // do nothing
            }
        });
    }
}
