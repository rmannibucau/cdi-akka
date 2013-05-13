package com.github.rmannibucau.cdi.test.akka;

import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import com.github.rmannibucau.cdi.akka.Akka;
import com.github.rmannibucau.cdi.akka.EventService;
import com.github.rmannibucau.cdi.akka.internal.Actors;
import com.github.rmannibucau.cdi.akka.internal.AkkaExtension;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class EventServiceTest {
    @Deployment
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "cdi-akka-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClass(MyActorCalledFromService.class)
                .addAsLibraries(ShrinkWrap.create(JavaArchive.class, "cdi-akka.jar")
                        .addAsServiceProvider(Extension.class, AkkaExtension.class)
                        .addPackages(true, Akka.class.getPackage()));
    }

    @Inject
    private EventService service;

    @Inject
    @Akka(ActorSystem.class)
    private ActorSystem system;

    @Inject
    private BeanManager bm;

    @Test
    public void actor() {
        assertNotNull(service);

        service.send(MyActorCalledFromService.class, "service");
        try {
            MyActorCalledFromService.LATCH.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            // no-op
        }

        assertEquals("service", MyActorCalledFromService.message);

        Actors.getActorRef(system, bm, MyActorCalledFromService.class).tell("just to check we don't have naming issue on actors");
    }

    public static class MyActorCalledFromService extends UntypedActor {
        public static final CountDownLatch LATCH = new CountDownLatch(1);
        public static String message;

        @Override
        public void onReceive(final Object msg) throws Exception {
            message = msg.toString();
            LATCH.countDown();
        }
    }
}
