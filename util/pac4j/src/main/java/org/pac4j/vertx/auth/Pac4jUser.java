package org.pac4j.vertx.auth;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import org.pac4j.core.profile.UserProfile;

import java.util.Collection;
import java.util.List;

/**
 * @author Jeremy Prime
 * @since 2.0.0
 */
public final class Pac4jUser implements User {

    private final List<UserProfile> profiles;
    private final JsonObject principal;
    private final JsonObject attributes;

    public Pac4jUser(final Collection<UserProfile> profiles) {
        this.profiles = (profiles == null) ? List.of() : List.copyOf(profiles);

        this.principal = new JsonObject().put("profilesCount", this.profiles.size());
        this.attributes = new JsonObject();
    }

    public List<UserProfile> getProfiles() {
        return profiles;
    }

    // ----- io.vertx.ext.auth.User -----

    @Override
    public JsonObject principal() {
        return principal;
    }

    @Override
    public JsonObject attributes() {
        return attributes;
    }

    @Override
    public User merge(User other) {
        if (other == null) return this;

        JsonObject otherPrinc = other.principal();
        if (otherPrinc != null) {
            for (String k : otherPrinc.fieldNames()) {
                this.principal.put(k, otherPrinc.getValue(k));
            }
        }

        JsonObject otherAttr = other.attributes();
        if (otherAttr != null) {
            for (String k : otherAttr.fieldNames()) {
                this.attributes.put(k, otherAttr.getValue(k));
            }
        }
        return this;
    }
}
