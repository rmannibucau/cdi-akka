package com.github.rmannibucau.cdi.test.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import com.github.rmannibucau.cdi.akka.Akka;
import com.github.rmannibucau.cdi.akka.internal.AkkaExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class AkkaCdiTest {
    @Deployment
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "cdi-akka-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClass(MyActor.class)
                .addAsLibraries(ShrinkWrap.create(JavaArchive.class, "cdi-akka.jar")
                        .addAsServiceProvider(Extension.class, AkkaExtension.class)
                        .addPackages(true, Akka.class.getPackage()));
    }

    @Inject
    @Akka(MyActor.class)
    private ActorRef ref;

    @Inject
    @Akka(ActorSystem.class)
    private ActorSystem system;

    @Test
    public void actor() {
        assertNotNull(ref);

        ref.tell("akka", ref);
        try {
            MyActor.LATCH.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            // no-op
        }

        assertEquals("akka", MyActor.message);
    }

    @Test
    public void system() {
        assertNotNull(system);
        assertEquals("AkkaCdi", system.name());
    }

    public static class MyActor extends UntypedActor {
        public static final CountDownLatch LATCH = new CountDownLatch(1);
        public static String message;

        @Override
        public void onReceive(final Object msg) throws Exception {
            message = msg.toString();
            LATCH.countDown();
        }
    }
}
