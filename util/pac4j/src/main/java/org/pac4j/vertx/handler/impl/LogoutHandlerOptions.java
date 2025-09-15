/*
  Copyright 2015 - 2015 pac4j organization

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.pac4j.vertx.handler.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Class with fluent API to wrap options which can be supplied to LogoutHandler. This approach is consistent
 * with existing vert.x configuration mechanisms for optional configuration.
 * @since 2.1.0
 */
@Accessors(chain = true)
public class LogoutHandlerOptions {

    @Getter @Setter
    private String defaultUrl = null;

    @Getter @Setter
    private String logoutUrlPattern = null;

    /**
     * True if the pac4j profiles are to be removed from the session on logoug, false otherwise.
     * By default we remove these profiles from the session as we assume this is what is implied by a logout.
     */
    @Getter @Setter
    private boolean localLogout = true;

    /**
     * True if following logout the entire web session should be cleared. By default we do not clear the entire session,
     * as for example the contents of a shopping cart should not generally be thrown away on logoug but remain within
     * the web session.
     */
    @Getter @Setter
    private boolean destroySession = false;

    /**
     * True if on logout a centralised logout should also occur to log out of some SSO provider, for example. By default
     * we assume existing pac4j behaviour which is not to do this.
     */
    @Getter @Setter
    private boolean centralLogout= false;
}
