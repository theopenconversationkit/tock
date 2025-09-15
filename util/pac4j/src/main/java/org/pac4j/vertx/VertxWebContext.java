package org.pac4j.vertx;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.impl.UserContextInternal;
import org.pac4j.core.context.Cookie;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.vertx.auth.Pac4jUser;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * WebContext implementation for Vert.x 3.
 *
 * @author Jeremy Prime
 * @since 2.0.0
 */
public class VertxWebContext implements WebContext {

    private final RoutingContext routingContext;
    private final String method;
    private final String serverName;
    private final int serverPort;
    private final String fullUrl;
    private final String scheme;
    private final String remoteAddress;
    private final JsonObject headers;
    private final JsonObject parameters;
    private final Map<String, String[]> mapParameters;
    private final SessionStore sessionStore;

    public VertxWebContext(final RoutingContext routingContext, final SessionStore sessionStore) {
        final HttpServerRequest request = routingContext.request();
        this.routingContext = routingContext;
        this.method = request.method().toString();
        this.sessionStore = sessionStore;

        this.fullUrl = request.absoluteURI();
        URI uri;
        try {
            uri = new URI(fullUrl);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new InvalidParameterException("Request to invalid URL " + fullUrl + " while constructing VertxWebContext");
        }
        this.scheme = uri.getScheme();
        this.serverName = uri.getHost();
        this.serverPort = (uri.getPort() != -1) ? uri.getPort() : scheme.equals("http") ? 80 : 443;
        this.remoteAddress = request.remoteAddress().toString();

        headers = new JsonObject();
        for (String name : request.headers().names()) {
            headers.put(name, request.headers().get(name));
        }

        parameters = new JsonObject();
        for (String name : request.params().names()) {
            parameters.put(name, new JsonArray(Arrays.asList(request.params().getAll(name).toArray())));
        }
        for (String name : request.formAttributes().names()) {
            parameters.put(name, new JsonArray(Arrays.asList(request.formAttributes().getAll(name).toArray())));
        }

        mapParameters = new HashMap<>();
        for (String name : parameters.fieldNames()) {
            JsonArray params = parameters.getJsonArray(name);
            String[] values = new String[params.size()];
            int i = 0;
            for (Object o : params) {
                values[i++] = (String) o;
            }
            mapParameters.put(name, values);
        }
    }

    @Override
    public Optional<String> getRequestParameter(String name) {
        JsonArray values = parameters.getJsonArray(name);
        if (values != null && values.size() > 0) {
            return Optional.ofNullable(values.getString(0));
        }
        return Optional.empty();
    }

    @Override
    public Map<String, String[]> getRequestParameters() {
        return mapParameters;
    }

    @Override
    public Optional<Object> getRequestAttribute(String s) {
        return Optional.ofNullable(routingContext.get(s));
    }

    @Override
    public void setRequestAttribute(String s, Object o) {
        routingContext.put(s, o);
    }

    @Override
    public Optional<String> getRequestHeader(String name) {
        return Optional.ofNullable(headers.getString(name));
    }

    @Override
    public String getRequestMethod() {
        return method;
    }

    @Override
    public String getRemoteAddr() {
        return remoteAddress;
    }

    @Override
    public void setResponseHeader(String name, String value) {
        routingContext.response().putHeader(name, value);
    }

    @Override
    public Optional<String> getResponseHeader(String s) {
        return Optional.ofNullable(routingContext.response().headers().get(s));
    }

    public Map<String, String> getResponseHeaders() {
        return routingContext.response().headers().entries().stream()
            .collect(Collectors.toMap(Map.Entry::getKey,
                    Map.Entry::getValue));
    }

    @Override
    public void setResponseContentType(String s) {
        routingContext.response().headers().add("Content-Type", s);
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public int getServerPort() {
        return serverPort;
    }

    @Override
    public String getScheme() {
        return scheme;
    }

    @Override
    public boolean isSecure() {
        return getScheme().equals("https");
    }

    @Override
    public String getFullRequestURL() {
        return fullUrl;
    }

    @Override
    public Collection<Cookie> getRequestCookies() {
        return routingContext.request().cookies().stream().map(cookie -> {
            io.vertx.core.http.Cookie cookie1 = cookie;
            final Cookie p4jCookie = new Cookie(cookie.getName(), cookie.getValue());
            p4jCookie.setDomain(cookie.getDomain());
            p4jCookie.setPath(cookie.getPath());
            return p4jCookie;
        }).collect(Collectors.toList());
    }

    @Override
    public void addResponseCookie(Cookie cookie) {
        io.vertx.core.http.Cookie vertxCookie =  io.vertx.core.http.Cookie.cookie(cookie.getName(), cookie.getValue())
                               .setHttpOnly(cookie.isHttpOnly())
                               .setSecure(cookie.isSecure())
                               .setDomain(cookie.getDomain())
                               .setPath(cookie.getPath());
        /*
        *  dont use the default of one of org.pac4j.core.context.Cookie which is -1 but allow the default one of
        *  io.vertx.ext.web.Cookie which uses netty Cookie internally
        * */
        if(cookie.getMaxAge()>0){
            vertxCookie.setMaxAge(cookie.getMaxAge());
        }

        routingContext.response().addCookie(vertxCookie);
    }

    @Override
    public String getPath() {
        return routingContext.request().path();
    }

    public Pac4jUser getVertxUser() {
        return (Pac4jUser) routingContext.user();
    }

    public void removeVertxUser() {
        UserContextInternal uc = (UserContextInternal) routingContext.user();
        if (uc != null) {
            uc.setUser(null);
        }
    }

    public void setVertxUser(final Pac4jUser pac4jUser) {
        UserContextInternal uc = (UserContextInternal) routingContext.user();
        if (uc != null) {
            uc.setUser(pac4jUser);
        }
    }

    public Session getVertxSession() {
        return routingContext.session();
    }

    public RoutingContext getVertxRoutingContext() {
        return routingContext;
    }
}
