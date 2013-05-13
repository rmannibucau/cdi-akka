package com.github.rmannibucau.cdi.akka.internal;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import javax.enterprise.inject.spi.BeanManager;

public final class Actors {
    private Actors() {
        // no-op
    }

    public static <T extends Actor> ActorRef getActorRef(final ActorSystem system, final BeanManager beanManager, final Class<T> beanClass) {
        return system.actorOf(new Props(beanClass).withCreator(new CdiCreator(beanManager, beanClass)));
    }
}
