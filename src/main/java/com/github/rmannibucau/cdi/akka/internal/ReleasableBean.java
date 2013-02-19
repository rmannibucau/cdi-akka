package com.github.rmannibucau.cdi.akka.internal;

import javax.enterprise.context.spi.CreationalContext;

public class ReleasableBean<T> {
    private final CreationalContext<?> creationalContext;
    private final T instance;

    public ReleasableBean(final CreationalContext<?> creationalContext, final T instance) {
        this.creationalContext = creationalContext;
        this.instance = instance;
    }

    public CreationalContext<?> getCreationalContext() {
        return creationalContext;
    }

    public T getInstance() {
        return instance;
    }
}
