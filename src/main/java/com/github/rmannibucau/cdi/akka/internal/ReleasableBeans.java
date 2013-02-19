package com.github.rmannibucau.cdi.akka.internal;

import java.util.ArrayList;
import java.util.Collection;

public final class ReleasableBeans {
    private static final Collection<ReleasableBean<?>> releasableBeans = new ArrayList<ReleasableBean<?>>();

    private ReleasableBeans() {
        // no-op
    }

    public static void add(final ReleasableBean<?> rb) {
        releasableBeans.add(rb);
    }

    public static void release() {
        for (final ReleasableBean<?> rb : releasableBeans) {
            rb.getCreationalContext().release();
        }
        releasableBeans.clear();
    }
}
