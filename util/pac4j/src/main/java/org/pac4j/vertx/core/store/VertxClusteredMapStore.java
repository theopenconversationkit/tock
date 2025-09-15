package org.pac4j.vertx.core.store;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.store.Store;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Pac4j shared store implementation based on vert.x clustered shared data.
 */
public class VertxClusteredMapStore<K, V> extends VertxMapStoreBase implements Store<K, V> {

    private final Vertx vertx;
    private final int blockingTimeoutSeconds;

    public VertxClusteredMapStore(final Vertx vertx) {
        this(vertx, 1);
    }

    public VertxClusteredMapStore(final Vertx vertx, final int timeoutSeconds) {
        this.vertx = vertx;
        this.blockingTimeoutSeconds = timeoutSeconds;
    }

    @Override
    public Optional<V> get(final K key) {
        final AsyncMap<K, V> map = await(vertx.sharedData().<K, V>getAsyncMap(PAC4J_SHARED_DATA_KEY));
        final V value = await(map.get(key));
        return Optional.ofNullable(value);
    }

    @Override
    public void set(final K key, final V value) {
        final AsyncMap<K, V> map = await(vertx.sharedData().<K, V>getAsyncMap(PAC4J_SHARED_DATA_KEY));
        await(map.put(key, value));
    }

    @Override
    public void remove(final K key) {
        final AsyncMap<K, V> map = await(vertx.sharedData().<K, V>getAsyncMap(PAC4J_SHARED_DATA_KEY));
        await(map.remove(key));
    }

    /** Helper: block on a Vert.x Future with a bounded timeout (Store API is sync). */
    private <T> T await(final Future<T> fut) {
        try {
            return fut.toCompletionStage()
                    .toCompletableFuture()
                    .get(blockingTimeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new TechnicalException(ie);
        } catch (ExecutionException | TimeoutException e) {
            throw new TechnicalException(e);
        }
    }
}
