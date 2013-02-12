package com.github.rmannibucau.cdi.akka.internal;

import akka.actor.Actor;
import akka.actor.UntypedActorFactory;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;

import javax.enterprise.inject.spi.BeanManager;

public class CdiCreator implements UntypedActorFactory {
    private final BeanManager bm;
    private final Class<? extends Actor> clazz;

    public CdiCreator(final BeanManager bm, final Class<? extends Actor> clazz) {
        this.bm = bm;
        this.clazz = clazz;
    }

    public CdiCreator(final Class<? extends Actor> clazz) {
        this.bm = BeanManagerProvider.getInstance().getBeanManager();
        this.clazz = clazz;
    }

    @Override
    public Actor create() throws Exception {
        return Cdis.newDependentInstance(bm, clazz);
    }
}
