package org.pac4j.vertx.core;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.Token;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.Scope.Value.Requirement;
import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.pac4j.core.exception.TechnicalException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Default eventbus object converter</p>
 * <p>The serialization strategy is:</p>
 * <ul>
 * <li>For primitive types (String, Number and Boolean), return as is</li>
 * <li>For arrays, convert to JsonArray</li>
 * <li>Otherwise, convert to a JsonObject with the class name in the "class" attribute and the serialized form with Jackson in the "value" attribute.
 * The (de)serialization Jackson process can be customized using the <code>addMixIn(target, mixinSource)</code> method</li>
 * </ul>
 * 
 * @author Michael Remond
 * @since 1.1.0
 *
 */
public class DefaultJsonConverter implements JsonConverter {

    private final ObjectMapper mapper = new ObjectMapper();
    private static final DefaultJsonConverter INSTANCE = new DefaultJsonConverter();

    public static JsonConverter getInstance() {
        return INSTANCE;
    }

    public DefaultJsonConverter() {
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);

        mapper.addMixIn(OAuth1RequestToken.class, OAuth1RequestTokenMixin.class)
            .addMixIn(BearerAccessToken.class, BearerAccessTokenMixin.class)
            .addMixIn(Scope.Value.class, ValueMixin.class)
            .addMixIn(Token.class, TokenMixin.class);
    }

    @Override
    public Object encodeObject(Object value) {
        if (value == null) {
            return null;
        } else if (isPrimitiveType(value)) {
            return value;
        } else if (value instanceof Object[]) {
            Object[] src = ((Object[]) value);
            List<Object> list = new ArrayList<>(src.length);
            fillEncodedList(src, list);
            return new JsonArray(list);
        } else {
            try {
                return new JsonObject().put("class", value.getClass().getName()).put("value",
                        new JsonObject(encode(value)));
            } catch (Exception e) {
                throw new TechnicalException("Error while encoding object", e);
            }
        }
    }

    private void fillEncodedList(Object[] src, List<Object> list) {
        for (Object object : src) {
            list.add(encodeObject(object));
        }
    }

    @Override
    public Object decodeObject(Object value) {
        if (value == null) {
            return null;
        } else if (isPrimitiveType(value)) {
            return value;
        } else if (value instanceof JsonArray) {
            JsonArray src = (JsonArray) value;
            List<Object> list = new ArrayList<>(src.size());
            fillDecodedList(src, list);
            return list.toArray();
        } else if (value instanceof JsonObject) {
            JsonObject src = (JsonObject) value;
            return decode(src);
        }
        return null;
    }

    private Object decode(JsonObject src) {
        try {
            return decode(src.getJsonObject("value").encode(), Class.forName(src.getString("class")));
        } catch (Exception e) {
            throw new TechnicalException("Error while decoding object", e);
        }
    }

    private void fillDecodedList(JsonArray src, List<Object> list) {
        for (Object object : src) {
            list.add(decodeObject(object));
        }
    }

    private boolean isPrimitiveType(Object value) {
        return value instanceof String || value instanceof Number || value instanceof Boolean;
    }

    private String encode(Object value) throws JsonGenerationException, JsonMappingException, IOException {
        return mapper.writeValueAsString(value);
    }

    @SuppressWarnings("unchecked")
    private <T> T decode(String string, Class<?> clazz) throws JsonParseException, JsonMappingException, IOException {
        return (T) mapper.readValue(string, clazz);
    }

    public static class BearerAccessTokenMixin {
        @JsonIgnore
        private AccessTokenType type;

        @JsonCreator
        public BearerAccessTokenMixin(@JsonProperty("value") String value, @JsonProperty("lifetime") long lifetime,
                @JsonProperty("scope") Scope scope) {
        }
    }

    public static class ValueMixin {
        @JsonCreator
        public ValueMixin(@JsonProperty("value") String value, @JsonProperty("requirement") Requirement requirement) {
        }
    }

    public static class TokenMixin {
        @JsonCreator
        public TokenMixin(@JsonProperty("token") String token, @JsonProperty("secret") String secret,
                @JsonProperty("rawResponse") String rawResponse) {
        }
    }

    public static class OAuth1RequestTokenMixin {
        @JsonCreator
        public OAuth1RequestTokenMixin(@JsonProperty("token") String token,
                                       @JsonProperty("tokenSecret") String tokenSecret,
                                       @JsonProperty("oauthCallbackConfirmed") boolean oauthCallbackConfirmed,
                                       @JsonProperty("rawResponse") String RawResponse) {

        }
    }

}
