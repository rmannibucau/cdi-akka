package com.github.rmannibucau.cdi.akka.internal;

import javax.enterprise.context.spi.CreationalContext;
import java.util.ArrayList;
import java.util.Collection;

public final class Releasables {
    private static final Collection<CreationalContext<?>> releasableBeans = new ArrayList<CreationalContext<?>>();

    private Releasables() {
        // no-op
    }

    public static void add(final CreationalContext<?> rb) {
        releasableBeans.add(rb);
    }

    public static void release() {
        for (final CreationalContext<?> cc : releasableBeans) {
            cc.release();
        }
        releasableBeans.clear();
    }
}
