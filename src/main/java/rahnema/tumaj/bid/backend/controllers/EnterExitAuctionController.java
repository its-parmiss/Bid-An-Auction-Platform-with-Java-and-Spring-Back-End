package rahnema.tumaj.bid.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import rahnema.tumaj.bid.backend.domains.Messages.AuctionEndedMessage;
import rahnema.tumaj.bid.backend.domains.Messages.AuctionInputMessage;
import rahnema.tumaj.bid.backend.domains.Messages.AuctionOutputMessage;
import rahnema.tumaj.bid.backend.models.Auction;
import rahnema.tumaj.bid.backend.models.User;
import rahnema.tumaj.bid.backend.services.auction.AuctionService;
import rahnema.tumaj.bid.backend.services.user.UserService;
import rahnema.tumaj.bid.backend.utils.exceptions.FullAuctionException;
import rahnema.tumaj.bid.backend.utils.exceptions.NotAllowedToLeaveAuctionException;
import rahnema.tumaj.bid.backend.utils.exceptions.NotFoundExceptions.AuctionNotFoundException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class EnterExitAuctionController {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    private final AuctionService service;
    private final UserService userService;
    ConcurrentMap<Long, Auction> auctionsData = new ConcurrentHashMap<>();

    public EnterExitAuctionController(AuctionService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    @MessageMapping("/enter")
    public synchronized void sendMessage(AuctionInputMessage inputMessage, /*("Authorization")*/ @Headers Map headers) {

        UsernamePasswordAuthenticationToken user = (UsernamePasswordAuthenticationToken) headers.get("simpUser");
        System.out.println("user.getName() = " + user.getName());
        Long longId = Long.valueOf(inputMessage.getAuctionId());
        Auction currentAuction = getAuction(longId);
        if (currentAuction.isFinished()) {
            return;
        }
        System.out.println("currentlyActiveBidders = " + currentAuction.getCurrentlyActiveBidders());
        if (currentAuction.getActiveBiddersLimit() > auctionsData.get(longId).getCurrentlyActiveBidders()) {
            currentAuction.setCurrentlyActiveBidders(currentAuction.getCurrentlyActiveBidders() + 1);
            auctionsData.put(longId, currentAuction);
            AuctionOutputMessage message = new AuctionOutputMessage();
            message.setCurrentlyActiveBiddersNumber(auctionsData.get(longId).getCurrentlyActiveBidders());
            this.simpMessagingTemplate.convertAndSend("/auction/" + inputMessage.getAuctionId(), message);
        } else {
            throw new FullAuctionException();
        }
    }


    @MessageMapping("/exit")
    public synchronized void exit(AuctionInputMessage auctionInputMessage, @Headers Map headers) {

        UsernamePasswordAuthenticationToken user = (UsernamePasswordAuthenticationToken) headers.get("simpUser");
        System.out.println("user.getName() = " + user.getName());
        Long auctionId = Long.valueOf(auctionInputMessage.getAuctionId());
        Auction currentAuction = getAuction(auctionId);
        if (currentAuction.isFinished()) {
            return;
        }
        System.out.println("currentlyActiveBidders = " + currentAuction.getCurrentlyActiveBidders());
        if (!currentAuction.getLastBidder().equals(user.getName())) {
            currentAuction.setCurrentlyActiveBidders(currentAuction.getCurrentlyActiveBidders() - 1);
            auctionsData.put(auctionId, currentAuction);
            AuctionOutputMessage message = new AuctionOutputMessage();
            message.setCurrentlyActiveBiddersNumber(auctionsData.get(auctionId).getCurrentlyActiveBidders());
            this.simpMessagingTemplate.convertAndSend("/auction/" + auctionInputMessage.getAuctionId(), message);
        } else {
            throw new NotAllowedToLeaveAuctionException();
        }
    }

    @MessageMapping("/endOfAuction")
    public synchronized void end(AuctionInputMessage auctionInputMessage) {
        Long auctionId = Long.valueOf(auctionInputMessage.getAuctionId());
        Auction currentAuction = getAuction(auctionId);
        currentAuction.setFinished(true);
        auctionsData.put(auctionId, currentAuction);
        service.saveAuction(currentAuction);
        AuctionEndedMessage message = new AuctionEndedMessage();
        message.setWinner(auctionsData.get(auctionId).getLastBidder());
        message.setWinningPrice(auctionsData.get(auctionId).getLastBid());
        this.simpMessagingTemplate.convertAndSend("/auction/" + auctionInputMessage.getAuctionId(), message);
    }
    private synchronized Auction getAuction(Long auctionId) {
        Auction currentAuction;
        if (auctionsData.get(auctionId) != null) {
            currentAuction = auctionsData.get(auctionId);
        } else {
            currentAuction = service.getOne(auctionId).orElseThrow(() -> new AuctionNotFoundException(auctionId));
            auctionsData.put(auctionId, currentAuction);
        }
        return currentAuction;
    }
}
