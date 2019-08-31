package rahnema.tumaj.bid.backend.utils.assemblers;

import org.quartz.Trigger;
import org.springframework.stereotype.Component;
import rahnema.tumaj.bid.backend.domains.Messages.AuctionOutputMessage;
import rahnema.tumaj.bid.backend.domains.Messages.MessageContents;
import rahnema.tumaj.bid.backend.domains.Messages.MessageTypes;
import rahnema.tumaj.bid.backend.models.Auction;

import java.util.Date;
import java.util.concurrent.ConcurrentMap;

@Component
public class MessageAssembler {

    public AuctionOutputMessage getFullMessage() {
        AuctionOutputMessage message = new AuctionOutputMessage();
        message.setDescription(MessageContents.FORBIDDEN_ENTER_FULL);
        message.setMessageType(MessageTypes.AUCTION_FULL);
        return message;
    }



    public AuctionOutputMessage getAlreadyInMessage() {
        AuctionOutputMessage message = new AuctionOutputMessage();
        message.setDescription(MessageContents.FORBIDDEN_ENTER_DUPLICATE);
        message.setMessageType(MessageTypes.ALREADY_IN);
        return message;
    }

    public AuctionOutputMessage getUpdateMessage(ConcurrentMap<Long, Auction> auctionsData, Auction currentAuction,
                                                ConcurrentMap<Long, Trigger> triggers ) {
        AuctionOutputMessage message = new AuctionOutputMessage();
        message.setAuctionId(String.valueOf(currentAuction.getId()));
        setMessageLastBid(currentAuction, message);
        message.setActiveBidders(auctionsData.get(currentAuction.getId()).getCurrentlyActiveBidders());
        message.setMessageType(MessageTypes.UPDATE_BIDDERS);
        message.setRemainingTime(calculateRemainingTime(currentAuction.getId(), triggers));
        return message;
    }

    private void setMessageLastBid(Auction currentAuction, AuctionOutputMessage message) {
        if (currentAuction.getLastBid() != null)
            message.setBidPrice(String.valueOf(currentAuction.getLastBid()));
        else
            message.setBidPrice(String.valueOf(currentAuction.getBasePrice()));
    }


    public AuctionOutputMessage getFinishedMessage(Auction currentAuction) {
        AuctionOutputMessage message = new AuctionOutputMessage();
        message.setIsFinished(currentAuction.isFinished());
        message.setLastBid(currentAuction.getLastBid());
        message.setDescription(MessageContents.FORBIDDEN_ENTER_CLOSED);
        message.setMessageType(MessageTypes.AUCTION_FINISHED);
        return message;
    }


    public AuctionOutputMessage getLastBidderMessage() {
        AuctionOutputMessage message = new AuctionOutputMessage();
        message.setDescription(MessageContents.FORBIDDEN_EXIT_LAST_BIDDER);
        message.setMessageType(MessageTypes.EXIT_FORBIDDEN);
        return message;
    }

    public AuctionOutputMessage getUpdateOnExitMessage(ConcurrentMap<Long, Auction> auctionsData, Long auctionId) {
        AuctionOutputMessage message = new AuctionOutputMessage();
        message.setActiveBidders(auctionsData.get(auctionId).getCurrentlyActiveBidders());
        message.setMessageType(MessageTypes.UPDATE_BIDDERS);
        return message;
    }

    public AuctionOutputMessage getNotInMessage() {
        AuctionOutputMessage message = new AuctionOutputMessage();
        message.setDescription(MessageContents.FORBIDDEN_EXIT_NOT_IN);
        message.setMessageType(MessageTypes.EXIT_FORBIDDEN);
        return message;
    }


    public long calculateRemainingTime(Long auctionId, ConcurrentMap<Long, Trigger> triggers) {
        if (triggers.get(auctionId) != null) {
            return (  triggers.get(auctionId).getStartTime().getTime() - new Date().getTime() ) / 1000 ;
        }
        else return -1;
    }




}