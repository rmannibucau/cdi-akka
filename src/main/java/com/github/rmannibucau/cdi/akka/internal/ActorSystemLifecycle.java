package com.github.rmannibucau.cdi.akka.internal;

import akka.actor.ActorSystem;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import java.util.concurrent.atomic.AtomicReference;

final class ActorSystemLifecycle implements ContextualLifecycle<ActorSystem> {
    private final AtomicReference<ActorSystem> system;

    public ActorSystemLifecycle(final AtomicReference<ActorSystem> as) {
        system = as;
    }

    @Override
    public ActorSystem create(final Bean<ActorSystem> bean, final CreationalContext<ActorSystem> creationalContext) {
        return system.get();
    }

    @Override
    public void destroy(final Bean<ActorSystem> bean, final ActorSystem instance,
                        final CreationalContext<ActorSystem> creationalContext) {
        // no-op
    }
}
