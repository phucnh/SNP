package ssp.service;

import akka.actor.UntypedActor;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.OpenRtb.BidRequest;
import com.google.openrtb.json.OpenRtbJsonFactory;

public class GreeterService extends UntypedActor {
    public static class Greet {}

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Greet) {
            System.out.print("Hello World!");
            OpenRtb.BidRequest request = BidRequest.newBuilder()
                    .setId("1")
                    .addImp(OpenRtb.BidRequest.Imp.newBuilder()
                            .setId("1")
                            .setBidfloor(4000)
                    ).build();

            OpenRtbJsonFactory openRtbJson = OpenRtbJsonFactory.create();
            String jsonReq = openRtbJson.newWriter().writeBidRequest(request);
            System.out.print(jsonReq);
        } else {
            unhandled(message);
        }
    }
}
