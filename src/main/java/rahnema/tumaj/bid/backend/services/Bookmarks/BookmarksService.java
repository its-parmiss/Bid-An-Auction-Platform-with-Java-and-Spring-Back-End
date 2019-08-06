package rahnema.tumaj.bid.backend.services.Bookmarks;

import rahnema.tumaj.bid.backend.models.Auction;

import java.util.List;

public interface BookmarksService {

    void bookmarkAuction(Long auctionId, Long userId);
    List<Auction> getAll(Long id);
}