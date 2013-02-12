package com.github.rmannibucau.cdi.akka.internal;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.github.rmannibucau.cdi.akka.Akka;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.AnnotationInstanceProvider;
import scala.concurrent.duration.Duration;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessProducerMethod;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.singletonMap;

public class AkkaExtension implements Extension, Deactivatable {
    private boolean activated;
    private boolean hasActorSystem = false;
    private final Collection<ProcessBean<? extends Actor>> actors = new ArrayList<ProcessBean<? extends Actor>>();

    private final AtomicReference<ActorSystem> system = new AtomicReference<ActorSystem>();
    private final Map<Class<?>, ActorRef> actorRefs = new ConcurrentHashMap<Class<?>, ActorRef>();

    void activate(final @Observes BeforeBeanDiscovery beforeBeanDiscovery) {
        activated = ClassDeactivationUtils.isActivated(AkkaExtension.class);
    }

    <T> void findActorRefs(final @Observes ProcessBean<T> processBean) {
        if (!activated) {
            return;
        }

        final Bean<T> bean = processBean.getBean();
        final Class<?> clazz = bean.getBeanClass();

        if (Actor.class.isAssignableFrom(clazz)) {
            actors.add(ProcessBean.class.cast(processBean));
        }
    }

    <A, B> void findActorSystem(final @Observes ProcessProducerMethod<A, B> processProducer) {
        if (!activated) {
            return;
        }

        final Class<?> returnType = processProducer.getAnnotatedProducerMethod().getJavaMember().getReturnType();
        if (ActorSystem.class.isAssignableFrom(returnType)) {
            if (hasActorSystem) {
                throw new IllegalStateException("Two actor systems available");
            }
            hasActorSystem = true;
        }
    }

    void addActorRef(final @Observes AfterBeanDiscovery afterBeanDiscovery, final BeanManager beanManager) {
        if (!activated) {
            return;
        }

        for (final ProcessBean<?> bean : actors) {
            final Class<?> beanClass = bean.getBean().getBeanClass();
            final Akka qualifier = AnnotationInstanceProvider.of(Akka.class, singletonMap("value", beanClass));
            afterBeanDiscovery.addBean(
                    new BeanBuilder<ActorRef>(beanManager)
                            .qualifiers(qualifier)
                            .beanClass(ActorRef.class)
                            .passivationCapable(true)
                            .id("Cdi_ActorRef#" + beanClass.getName())
                            .scope(Dependent.class) // not proxiable so simulate the @ApplicationScoped through the lifecycle
                            .name(beanClass.getSimpleName())
                            .beanLifecycle(new ActorRefLifecycle(actorRefs))
                            .create());
        }

        {
            final Akka qualifier = AnnotationInstanceProvider.of(Akka.class, singletonMap("value", ActorSystem.class));
            afterBeanDiscovery.addBean(
                new BeanBuilder<ActorSystem>(beanManager)
                        .qualifiers(qualifier)
                        .beanClass(ActorSystem.class)
                        .scope(Dependent.class)
                        .name("actorSystem")
                        .beanLifecycle(new ActorSystemLifecycle(system))
                        .create());
        }
    }

    void startAkka(final @Observes AfterDeploymentValidation afterDeploymentValidation, final BeanManager beanManager) {
        if (!activated) {
            return;
        }

        if (hasActorSystem) {
            system.set(Cdis.newDependentInstance(beanManager, ActorSystem.class));
        } else {
            system.set(ActorSystem.create("AkkaCdi"));
        }

        for (ProcessBean<? extends Actor> actor : actors) {
            addActorRef(beanManager, actor);
        }
        actors.clear();
    }

    private <T extends Actor> void addActorRef(final BeanManager beanManager, final ProcessBean<T> actor) {
        final Class<T> beanClass = Class.class.cast(actor.getBean().getBeanClass());
        final ActorRef actorRef = system.get().actorOf(new Props(beanClass).withCreator(new CdiCreator(beanManager, beanClass)), beanClass.getSimpleName());
        actorRefs.put(beanClass, actorRef);
    }

    void shutdown(final @Observes BeforeShutdown shutdown) {
        final ActorSystem actorSystem = system.get();
        if (!actorSystem.isTerminated()) {
            actorSystem.shutdown();
            actorSystem.awaitTermination(Duration.apply(5, TimeUnit.MINUTES));
        }
        actorRefs.clear();
    }
}
