package ssp.service;

import akka.actor.UntypedActor;

public class GreeterService extends UntypedActor {
    public static class Greet {}

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Greet) {
            System.out.print("Hello World!");
        } else {
            unhandled(message);
        }
    }
}
