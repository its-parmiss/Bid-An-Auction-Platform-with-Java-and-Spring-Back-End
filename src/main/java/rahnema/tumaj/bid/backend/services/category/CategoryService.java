package rahnema.tumaj.bid.backend.services.category;

import rahnema.tumaj.bid.backend.domains.Category.CategoryInputDTO;
import rahnema.tumaj.bid.backend.domains.Category.CategoryOutputDTO;
import rahnema.tumaj.bid.backend.domains.user.UserInputDTO;
import rahnema.tumaj.bid.backend.models.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> getAll();
    CategoryOutputDTO addOne(CategoryInputDTO category);
    Category findById(Long Id);
}
