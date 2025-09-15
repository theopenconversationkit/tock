package org.pac4j.vertx;

import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.vertx.auth.Pac4jUser;
import org.pac4j.vertx.context.session.VertxSessionStore;

import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * @author Jeremy Prime
 * @since 2.0.0
 */
public class VertxProfileManager extends ProfileManager {

    private final VertxWebContext vertxWebContext;

    public VertxProfileManager(final VertxWebContext context, final VertxSessionStore sessionStore) {
        super(context, sessionStore);
        this.vertxWebContext = context;
    }

    @Override
    protected void saveAll(final LinkedHashMap<String, UserProfile> profiles, final boolean saveInSession) {
        super.saveAll(profiles, saveInSession);

        final Collection<UserProfile> values = (profiles != null) ? profiles.values() : java.util.List.of();
        final Pac4jUser vertxUser = new Pac4jUser(values);
        vertxWebContext.setVertxUser(vertxUser);
    }

    @Override
    public void removeOrRenewExpiredProfiles(final LinkedHashMap<String, UserProfile> profiles,
                                             final boolean readFromSession) {
        super.removeOrRenewExpiredProfiles(profiles, readFromSession);

       if (profiles == null || profiles.isEmpty()) {
            vertxWebContext.removeVertxUser();
        } else {
            final Pac4jUser refreshed = new Pac4jUser(profiles.values());
            vertxWebContext.setVertxUser(refreshed);
        }
    }
}
