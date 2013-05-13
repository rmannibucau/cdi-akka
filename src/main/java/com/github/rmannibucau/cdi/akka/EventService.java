package com.github.rmannibucau.cdi.akka;

import akka.actor.Actor;
import akka.actor.ActorSystem;
import com.github.rmannibucau.cdi.akka.internal.Actors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 * Just a CDI class making the link between Akka and CDI more Java like
 */
@ApplicationScoped
public class EventService {
    @Inject
    @Akka(ActorSystem.class)
    private ActorSystem actorSystem;

    @Inject
    private BeanManager bm;

    public <T> void send(final Class<? extends Actor> actor, final T message) {
        Actors.getActorRef(actorSystem, bm, actor).tell(message);
    }
}
