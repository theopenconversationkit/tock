package org.pac4j.vertx.core;

/**
 * Interface for encoding and decoding objects for the event bus. This is required for
 * session attributes and the user profile.
 *
 * @author Michael Remond
 * @since 1.1.0
 *
 */
public interface JsonConverter {

  /**
   * Encode the given object in a compatible form for the event bus.
   *
   * @param value the value to encode
   * @return the encoded object
   */
  Object encodeObject(Object value);

  /**
   * Decode the given object encoded with the encodeObject method.
   *
   * @param value the value to decode
   * @return the decoded object
   */
  Object decodeObject(Object value);
}