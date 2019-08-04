package rahnema.tumaj.bid.backend.models;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;


public class Category {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    Long id;
    @Column(nullable = false,name="title",unique = true)
    String title;
    @Column(name = "created_at",columnDefinition="DATE DEFAULT CURRENT_DATE")
    Date created_at;
    @OneToMany(mappedBy = "category")
    Set<Auction> auctions;
}
