package com.github.rmannibucau.cdi.akka.internal;

import akka.actor.ActorRef;
import com.github.rmannibucau.cdi.akka.Akka;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import java.lang.annotation.Annotation;
import java.util.Map;

final class ActorRefLifecycle implements ContextualLifecycle<ActorRef> {
    private final Map<Class<?>, ActorRef> refs;

    public ActorRefLifecycle(final Map<Class<?>, ActorRef> actorRefs) {
        refs = actorRefs;
    }

    @Override
    public ActorRef create(final Bean<ActorRef> bean, final CreationalContext<ActorRef> creationalContext) {
        for (final Annotation qualifier : bean.getQualifiers()) {
            if (Akka.class.equals(qualifier.annotationType())) {
                return refs.get(Akka.class.cast(qualifier).value());
            }
        }
        throw new IllegalArgumentException("Injection points using AkkaExtension should be decorated with @Akka(...)");
    }

    @Override
    public void destroy(final Bean<ActorRef> bean, final ActorRef instance,
                        final CreationalContext<ActorRef> creationalContext) {
        // no-op
    }
}
