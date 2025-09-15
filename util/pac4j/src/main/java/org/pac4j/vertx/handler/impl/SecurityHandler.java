package org.pac4j.vertx.handler.impl;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.impl.UserContextInternal;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.engine.SecurityGrantedAccessAdapter;
import org.pac4j.core.engine.SecurityLogic;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.vertx.VertxFrameworkParameters;
import org.pac4j.vertx.VertxWebContext;
import org.pac4j.vertx.auth.Pac4jUser;
import org.pac4j.vertx.context.session.VertxSessionStore;

import java.util.Objects;

/**
 * @author Jeremy Prime
 * @since 2.0.0
 */
public class SecurityHandler implements AuthenticationHandler {

    private final Vertx vertx;
    private final VertxSessionStore sessionStore;
    private final Config config;
    private final SecurityHandlerOptions options;

    public SecurityHandler(final Vertx vertx,
                           final VertxSessionStore sessionStore,
                           final Config config,
                           final SecurityHandlerOptions options) {
        this.vertx = Objects.requireNonNull(vertx, "vertx");
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.config = Objects.requireNonNull(config, "config");
        this.options = Objects.requireNonNull(options, "options");
    }

    @Override
    public void handle(final RoutingContext ctx) {
         final WebContext webCtx = new VertxWebContext(ctx, sessionStore);

        final SecurityLogic securityLogic = config.getSecurityLogic();
        final HttpActionAdapter httpActionAdapter = config.getHttpActionAdapter();

        final SecurityGrantedAccessAdapter granted = (context, store, profiles) -> {
            final Pac4jUser user = new Pac4jUser(profiles);

           ((UserContextInternal) ctx.user()).setUser(user);

            ctx.next();
            return null;
        };

        // Delegate to pac4j logic (clients/authorizers/matchers come from options)
        securityLogic.perform(
                config,
                granted,
                options.getClients(),
                options.getAuthorizers(),
                options.getMatchers(),
                new VertxFrameworkParameters(ctx)
        );
    }
}
