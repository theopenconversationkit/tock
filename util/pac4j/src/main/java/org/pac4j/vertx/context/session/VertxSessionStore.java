package org.pac4j.vertx.context.session;

import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.SessionStore;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.core.util.serializer.JavaSerializer;
import org.pac4j.vertx.VertxWebContext;

import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Vert.x implementation of pac4j SessionStore interface to access the existing vertx-web session.
 */
public class VertxSessionStore implements org.pac4j.core.context.session.SessionStore {

    private final SessionStore sessionStore;
    private static final JavaSerializer JAVA_SERIALIZER = new JavaSerializer();

    private final Session providedSession;

    public VertxSessionStore(final SessionStore sessionStore) {
        this(sessionStore, null);
    }

    public VertxSessionStore(final SessionStore sessionStore, final Session providedSession) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore");
        this.providedSession = providedSession;
    }

    protected Session getVertxSession(final WebContext context) {
        if (providedSession != null) {
            return providedSession;
        } else {
            return ((VertxWebContext) context).getVertxSession();
        }
    }

    @Override
    public Optional<String> getSessionId(final WebContext context, final boolean createSession) {
        final Session vertxSession = getVertxSession(context);
        if (vertxSession != null) {
            return Optional.of(vertxSession.id());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Object> get(final WebContext context, final String key) {
        final Session vertxSession = getVertxSession(context);
        if (vertxSession != null) {
            if (Pac4jConstants.USER_PROFILES.equals(key)) {
                final String value = vertxSession.get(key);
                if (value != null) {
                    byte[] inputBytes = Base64.getDecoder().decode(value);
                    return Optional.ofNullable(JAVA_SERIALIZER.deserializeFromBytes(inputBytes));
                }
            }
            return Optional.ofNullable(vertxSession.get(key));
        }
        return Optional.empty();
    }

    @Override
    public void set(final WebContext context, final String key, final Object value) {
        final Session vertxSession = getVertxSession(context);
        if (vertxSession != null) {
            if (value == null) {
                vertxSession.remove(key);
            } else {
                if (Pac4jConstants.USER_PROFILES.equals(key)) {
                    vertxSession.put(key, Base64.getEncoder().encodeToString(JAVA_SERIALIZER.serializeToBytes(value)));
                } else {
                    vertxSession.put(key, value);
                }
            }
        }
    }

    @Override
    public boolean destroySession(final WebContext context) {
        final Session vertxSession = getVertxSession(context);
        if (vertxSession != null) {
            vertxSession.destroy();
            return true;
        }
        return false;
    }

    @Override
    public Optional<Object> getTrackableSession(final WebContext context) {
        final Session vertxSession = getVertxSession(context);
        if (vertxSession != null) {
            return Optional.of(vertxSession.id());
        }
        return Optional.empty();
    }

    @Override
    public Optional<org.pac4j.core.context.session.SessionStore> buildFromTrackableSession(final WebContext context, final Object trackableSession) {
        if (trackableSession != null) {
            final CompletableFuture<Session> vertxSessionFuture = new CompletableFuture<>();

            sessionStore
                    .get((String) trackableSession)
                    .onComplete(ar -> {
                        if (ar.succeeded()) {
                            vertxSessionFuture.complete(ar.result());
                        } else {
                            vertxSessionFuture.completeExceptionally(ar.cause());
                        }
                    });

            final CompletableFuture<VertxSessionStore> pac4jSessionFuture = vertxSessionFuture.thenApply(sess -> {
                if (sess != null) {
                    return new VertxSessionStore(sessionStore, sess);
                } else {
                    return null;
                }
            });

            try {
                return Optional.ofNullable(pac4jSessionFuture.get());
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new TechnicalException(e);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean renewSession(final WebContext context) {
        final Session vertxSession = getVertxSession(context);
        if (vertxSession != null) {
            vertxSession.regenerateId();
            return true;
        }
        return false;
    }
}
