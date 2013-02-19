package com.github.rmannibucau.cdi.akka.internal;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.util.Set;

final class Cdis {
    private Cdis() {
        // no-op
    }

    public static <T> T newDependentInstance(final BeanManager bm, final Class<T> clazz) {
        final Set<Bean<?>> beans = bm.getBeans(clazz);
        if (beans == null || beans.isEmpty()) {
            throw new IllegalStateException("Could not find actor " + clazz);
        }

        final Bean<?> bean = bm.resolve(beans); // this line is quite complicated in pure scala and often = beans.iterator().next()
        final CreationalContext<?> creationalContext = bm.createCreationalContext(bean);
        final T instance = clazz.cast(bm.getReference(bean, clazz, creationalContext));
        final ReleasableBean<T> rb = new ReleasableBean<T>(creationalContext, instance);
        Releasables.add(creationalContext);
        return rb.getInstance();
    }
}
