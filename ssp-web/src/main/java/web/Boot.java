package ssp.web;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import ssp.service.GreeterService;

public class Boot {
    public static void main(String[] args) throws Exception {
        final ActorSystem system = ActorSystem.create("snp-ssp");
        final ActorRef greeter = system.actorOf(Props.create(GreeterService.class), "greater");
        
        try {
            greeter.tell(new GreeterService.Greet(), null);
        } catch (Exception e) {
            System.err.println("Failed getting result: " + e.getMessage());
            throw e;
        } finally {
            system.shutdown();
            system.awaitTermination();
        }
    }
}
