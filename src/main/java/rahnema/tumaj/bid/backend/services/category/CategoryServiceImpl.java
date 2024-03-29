package rahnema.tumaj.bid.backend.services.category;

import org.springframework.stereotype.Service;
import rahnema.tumaj.bid.backend.domains.Category.CategoryInputDTO;
import rahnema.tumaj.bid.backend.domains.Category.CategoryOutputDTO;
import rahnema.tumaj.bid.backend.models.Category;
import rahnema.tumaj.bid.backend.repositories.CategoryRepository;
import rahnema.tumaj.bid.backend.utils.exceptions.NotFoundExceptions.CategoryNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repository;

    public CategoryServiceImpl(CategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Category> getAll() {
        Iterable<Category> userIterable = this.repository.findAll();
        List<Category> categories = new ArrayList<>();
        userIterable.forEach(categories::add);
        return categories;
    }
    @Override
    public CategoryOutputDTO addOne(CategoryInputDTO category) {
        Category categoryModel = CategoryInputDTO.toModel(category);
        return CategoryOutputDTO.fromModel(repository.save(categoryModel));
    }

    @Override
    public Category findById(Long Id) {
        Optional<Category> category=repository.findById(Id);
        return category.orElseThrow(() -> new CategoryNotFoundException(Id));
    }

}
