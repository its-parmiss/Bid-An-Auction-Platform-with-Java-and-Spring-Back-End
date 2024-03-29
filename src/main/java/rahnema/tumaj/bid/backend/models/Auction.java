package rahnema.tumaj.bid.backend.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="Auctions")
@Data
@EqualsAndHashCode(exclude={"users", "user", "category", "images", "bids"})
public class Auction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    @Column(nullable = false, name = "title")
    String title;
    @Column(name = "description")
    String description;
    @Column(name = "start_date")
    Date startDate;
    @Column(name = "last_bid")
    Long lastBid;
    @Column(name = "last_bidder")
    String lastBidder;
    @Column(name = "base_price",nullable = false)
    Long basePrice;
    @Column(name = "active_bidders_limit", columnDefinition = "int DEFAULT '0'")
    int activeBiddersLimit;
    @Column(name = "currently_active_bidders", columnDefinition = "int DEFAULT '0'")
    int currentlyActiveBidders;
    @Column(name = "finished"/*, columnDefinition = "boolean DEFAULT false"*/)
    boolean finished;
    @Column(name = "expire_date")
    Date expireDate;
    @Column(name = "createdAt")
//    @Temporal(TemporalType.DATE)
    Date createdAt;
    @ManyToOne
    @JsonManagedReference
    Category category;
    @ManyToOne
    @JsonBackReference
    User user;
    @OneToMany(fetch=FetchType.EAGER,mappedBy = "auction")
    @JsonManagedReference
    Set<Images> images = new HashSet<>();
    @JsonBackReference
    @OneToMany(fetch=FetchType.EAGER,mappedBy = "auction")
    Set<Bid> bids;
    @JsonBackReference
    @ManyToMany(fetch=FetchType.EAGER,mappedBy = "auctions",cascade=CascadeType.ALL) //bookmarks
    Set<User> users;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
        this.finished = false;
        this.lastBidder="";
    }

    public void addImage(Images image) {
        this.images.add(image);
    }
}