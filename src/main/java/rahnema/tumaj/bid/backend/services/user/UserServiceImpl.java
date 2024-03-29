package rahnema.tumaj.bid.backend.services.user;

import org.springframework.stereotype.Service;
import rahnema.tumaj.bid.backend.controllers.SecurityController;
import rahnema.tumaj.bid.backend.domains.user.UserInputDTO;
import rahnema.tumaj.bid.backend.domains.user.UserOutputDTO;
import rahnema.tumaj.bid.backend.models.User;
import rahnema.tumaj.bid.backend.repositories.UserRepository;
import rahnema.tumaj.bid.backend.utils.athentication.TokenUtil;
import rahnema.tumaj.bid.backend.utils.exceptions.AlreadyExistExceptions.EmailAlreadyExistsException;
import rahnema.tumaj.bid.backend.utils.exceptions.NotFoundExceptions.TokenNotFoundException;
import rahnema.tumaj.bid.backend.utils.exceptions.NotFoundExceptions.UserNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final SecurityController securityController;
    private final TokenUtil tokenUtil;

    public UserServiceImpl(UserRepository userRepository, SecurityController securityController, TokenUtil tokenUtil) {
        this.userRepository = userRepository;
        this.securityController = securityController;
        this.tokenUtil = tokenUtil;
    }

    @Override
    public Optional<User> getOne(Long id) {
        return this.userRepository.findById(id);
    }

    @Override
    public List<User> getAll() {
        Iterable<User> userIterable = this.userRepository.findAll();
        List<User> userList = new ArrayList<>();
        userIterable.forEach(userList::add);
        return userList;
    }

    @Override
    public User addOne(UserInputDTO user) {
        if (this.userRepository.existsByEmail(user.getEmail()))
            throw new EmailAlreadyExistsException(user.getEmail());
        User userModel = UserInputDTO.toModel(user);
        userModel.setPassword(
            this.securityController.bCryptPasswordEncoder
                    .encode(userModel.getPassword())
        );
        return userRepository.save(userModel);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getUserWithToken(String token) {
        System.out.println(token);
        String email = tokenUtil
                .getUsernameFromToken(token.split(" ")[1])
                .orElseThrow(TokenNotFoundException::new);
        return this.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    }
}
