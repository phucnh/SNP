package snp.ssp.service;

import akka.actor.ActorRef;
import akka.actor.Scheduler;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.actor.UntypedActorPublisher;
import com.google.openrtb.OpenRtb.BidRequest;
import com.google.openrtb.OpenRtb.BidResponse;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Auctioneer extends UntypedActorPublisher<BidRequest> {
    private static Config config = ConfigFactory.load().getConfig("snp.ssp.snp.ssp.service.auctioneer");
    static final int BULK_HEAD_BUFFER_SIZE = config.getInt("bulkhead-buffer-size");
    static final int AUCTION_DEFAULT_TIMEOUT_IN_MILLIS = 100;

    final Materializer materializer = ActorMaterializer.create(getContext().system());

    private Scheduler scheduler = getContext().system().scheduler();

    private List<ActorRef> bidders;

    protected final AuctionContext auctionContext = new AuctionContext();

    final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public void preStart() {
        // TODO: create stream
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof BidRequest) {

            BidRequest bidRequest = (BidRequest) message;
            log.info("received {}", bidRequest);

        } else if (message instanceof Protocols.OpenAuction) {

            Protocols.OpenAuction openAuctionMsg = (Protocols.OpenAuction) message;
            if (totalDemand() > 0) {
                // TODO: openAuction
            } else if (openAuctionMsg.canRetry()) {
                log.warning("No demand from down stream. Retrying {},", openAuctionMsg.toSimpleLog());
            } else {
                log.warning("Ignoring message because of max retired: {}", openAuctionMsg);

            }
        } else if (message instanceof BidResponse) {
            // TODO: add bid response to buffer
        } else if (message instanceof Protocols.CloseAuction) {

            Protocols.CloseAuction closeMsg = (Protocols.CloseAuction) message;
            String bidRequsetId = closeMsg.getBidRequestId();

            if (auctionContext.isClosed(bidRequsetId)) {
                log.debug("Auction is already closed: {}", bidRequsetId);
            } else {
                // TODO: build bid response and return to sellers
                auctionContext.closeAuctionOf(bidRequsetId);
            }

        } else {

            log.error("Unhandled message: {}", message);
            unhandled(message);
            
        }
    }

}

class Protocols {
    static class OpenAuction {
        private final BidRequest bidRequest;
        private int remainingRetry = 10;

        public OpenAuction(BidRequest bidRequest, int remainingRetry) {
            this.bidRequest = bidRequest;
            this.remainingRetry = remainingRetry;
        }

        BidRequest getBidRequest() {
            return bidRequest;
        }

        int getRemainingRetry() {
            return remainingRetry;
        }

        void setRemainingRetry(int remainingRetry) {
            remainingRetry = remainingRetry;
        }

        void increaseRemainingRetry(int incrementCount) {
            remainingRetry += incrementCount;
        }

        boolean canRetry() {
            return remainingRetry > 0;
        }

        String toSimpleLog() {
            return String.format("OpenAuction(%s)", bidRequest.getId());

        }
    }

    static class CloseAuction {
        private final String bidRequestId;

        CloseAuction(String bidRequestId) {
            this.bidRequestId = bidRequestId;
        }

        String getBidRequestId() { return bidRequestId; }

        String toSimpleLog() {
            return String.format("CloseAuction(%s)", bidRequestId);
        }
    }
}

class AuctionContext {
    private HashMap<String, ActorRef> sellers;
    private HashMap<String, BidRequest> bidRequests;
    private HashMap<String, HashSet<BidResponse>> bidRespones;

    AuctionContext() {
        sellers = new HashMap<>();
        bidRequests = new HashMap<>();
        bidRespones = new HashMap<>();
    }

    HashMap<String, ActorRef> getSellers() { return sellers; }

    HashMap<String, BidRequest> getBidRequests() { return bidRequests; }

    boolean isClosed(String id) {
        return !bidRequests.containsKey(id);
    }

    void closeAuctionOf(String id) {
        bidRequests.remove(id);
    }
}