package org.pac4j.vertx.handler.impl;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author Jeremy Prime
 * @since 2.0.0
 */
@Accessors(chain=true)
public class SecurityHandlerOptions {

    @Getter @Setter @NonNull
    private String clients = "";

    @Getter @Setter @NonNull
    private String authorizers = "";

    @Getter @Setter @NonNull
    private String matchers = "";

    @Getter @Setter
    private boolean multiProfile = false;

}
