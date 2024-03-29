package rahnema.tumaj.bid.backend.repositories;

import org.springframework.data.repository.CrudRepository;
import rahnema.tumaj.bid.backend.models.Category;
import rahnema.tumaj.bid.backend.models.User;

import java.util.Optional;

public interface CategoryRepository extends CrudRepository<Category, Long> {

    Optional<Category> findById(Long Id);
}
