package rahnema.tumaj.bid.backend.utils.exceptions.NotFoundExceptions;


public class AuctionNotFoundException extends RuntimeException {
    public AuctionNotFoundException(Long id) {
        super("Auction with id " + id + " not found");
    }
}