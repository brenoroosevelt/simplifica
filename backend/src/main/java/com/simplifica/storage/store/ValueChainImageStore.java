package com.simplifica.storage.store;

import com.simplifica.domain.entity.ValueChain;
import org.springframework.content.commons.repository.ContentStore;

import java.util.UUID;

/**
 * Spring Content store for Value Chain images.
 *
 * Spring Content automatically provides implementations for storing,
 * retrieving, and deleting content associated with ValueChain entities.
 *
 * Usage:
 * - setContent(valueChain, inputStream) - stores image content
 * - getContent(valueChain) - retrieves image content as InputStream
 * - unsetContent(valueChain) - deletes the image content
 */
public interface ValueChainImageStore extends ContentStore<ValueChain, UUID> {
    // Spring Content automatically generates the implementation
    // No methods need to be declared unless custom behavior is needed
}
