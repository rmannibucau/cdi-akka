cdi-akka: Basic idea
====================

Some basic CDI integration for Akka.

Akka(CDI)Extension
==================

The com.github.rmannibucau.cdi.akka.internal.AkkaExtension is simply a CDI extension
to allow to import in Akka actors world some CDI features and the opposite.

The first feature is to expose to CDI world akka.actor.ActorRef.

In Java it looks like:

    @Inject @Akka(MyActor.class)
    private ActorRef ref;

In Scala it will probably be:

    @Inject @Akka(classOf(MyActor))
    private var ref: ActorRef = _

So then you can send messages to Akka as simple as usually:

    ref ! Foo

If you want some more advanced usage you can inject using the same kind of trick the ActorSystem:

    @Inject @Akka(ActorSystem)
    private var system: ActorSystem = _

Here a Java sample:

    @ApplicationScoped
    public class SomeBusiness {
        @Inject @Akka(PongActor.class)
        private ActorRef pongActor;

        @Inject @Akka(PingActor.class)
        private ActorRef pingActor;

        @Inject @Akka(ActorSystem.class)
        private ActorSystem system;

        public void sendPong() {
            System.out.println("PingActor! from " + system.name());
            answer();
        }

        private void answer() {
            pongActor.tell(Pong$.MODULE$, pingActor); // Pong is a scala case class
        }
    }

Note that in this extension actors are @Dependent beans so you can inject any CDI bean you want in it:

    class PingActor extends Actor {
      @Inject
      private var business: SomeBusiness = _

      def receive = {
        case Ping =>
          business.sendPong()
      }
    }


Using CdiCreator without the extension
======================================

To get actor managed by cdi you can use the com.github.rmannibucau.cdi.akka.internal.CdiCreator even
if you don't use the extension.

In Java it will look like:

    system.actorOf(new Props(beanClass).withCreator(new CdiCreator(beanManager, actorType)), "myActor);
    // or (will use deltaspike BeanManagerProvider)
    system.actorOf(new Props(beanClass).withCreator(new CdiCreator(actorType)), "myActor);


(De)Activation
==============

The module is activated by default but if you want to use only some part of it (will be explained later)
you can deactivate the cdi extension using deltaspike deactivable mecanism
on the extension com.github.rmannibucau.cdi.akka.internal.AkkaExtension.

Note: The easier is probable to implement org.apache.deltaspike.core.spi.activation.ClassDeactivator.
