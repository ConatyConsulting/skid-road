package io.ifar.skidroad.writing.file;

import java.io.IOException;

/**
 * Serializes items for output. Must be thread-safe.
 */
public interface Serializer<T> {
    public String serialize(T item) throws IOException;
}
