package rahnema.tumaj.bid.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rahnema.tumaj.bid.backend.domains.authentication.AuthenticationResponse;
import rahnema.tumaj.bid.backend.models.User;
import rahnema.tumaj.bid.backend.services.user.UserService;
import rahnema.tumaj.bid.backend.storage.StorageService;
import rahnema.tumaj.bid.backend.utils.exceptions.IllegalInputExceptions.IllegalUserInputException;

import rahnema.tumaj.bid.backend.utils.athentication.TokenUtil;
import rahnema.tumaj.bid.backend.utils.validators.UserValidator;
import rahnema.tumaj.bid.backend.utils.validators.ValidatorConstants;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SettingsController {

    private final UserService userService;
    private final TokenUtil tokenUtil;
    private final StorageService storageService;
    private final UserValidator userValidator;
    private final PasswordController passwordController;
    private final SecurityController securityController;

    @Autowired
    public SettingsController(StorageService storageService,
                              UserService userService,
                              TokenUtil tokenUtil,
                              UserValidator userValidator,
                              PasswordController passwordController,
                              SecurityController securityController) {

        this.userService = userService;
        this.tokenUtil = tokenUtil;
        this.storageService = storageService;
        this.userValidator = userValidator;
        this.passwordController = passwordController;
        this.securityController = securityController;
    }

    @PostMapping("/user/settings")
    public AuthenticationResponse changeAccountSettings(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> params) {

        User user = userService.getUserWithToken(token);

        if (validateUserFieldsFromParams(params)) {
            changeUserFieldsFromParams(params, user);
            User savedUser = userService.saveUser(user);
            return tokenUtil.generateNewAuthorization(savedUser);
        } else {
            throw new IllegalUserInputException();
        }

    }

    private boolean validateUserFieldsFromParams(Map<String, String> params) {
        return
                        userValidator.isUserNameValid(params.get("firstName"),
                                params.get("lastName"),
                                ValidatorConstants.NAME);
    }

    @PostMapping("/user/settings/upload")
    public Map<String,String> handleFileUpload(
            @RequestHeader("Authorization") String token,
            @RequestBody MultipartFile file) {

        String name = storageService.store(file, "profilePicture");
        org.springframework.core.io.Resource tempFile = storageService.loadAsResource(name, "profilePicture");
        User user = userService.getUserWithToken(token);
        try {
            user.setProfilePicture(tempFile.getURL().toString());
            userService.saveUser(user);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String,String> jsonName=new HashMap<>();
        jsonName.put("name",name);
        return jsonName;
    }


    private void changeUserFieldsFromParams(Map<String, String> params, User user) {
        String newFirstName = params.get("firstName");
        String newLastName = params.get("lastName");

        setUpdatedUserFields(user, newFirstName, newLastName);
    }

    private void setUpdatedUserFields(User user, String newFirstName, String newLastName) {

        user.setFirstName(newFirstName);
        if (newLastName != null)
            user.setLastName(newLastName);
    }

    @PostMapping("/user/settings/change-password")
    private AuthenticationResponse changeAccountPassword(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> params) {

        User user = userService.getUserWithToken(token);

        String currentPassword = params.get("currentPassword");
        String newPassword = params.get("newPassword");

        if (securityController.bCryptPasswordEncoder
                .matches(currentPassword, user.getPassword()) &&
                userValidator.isUserPasswordValid(newPassword, ValidatorConstants.PASSWORD)) {
            changeUserPassword(newPassword, user);
            return tokenUtil.generateNewAuthorization(user);
        } else {
            throw new IllegalUserInputException();
        }
    }

    private void changeUserPassword(String password, User user) {
        user.setPassword(
                securityController.bCryptPasswordEncoder
                        .encode(password)
        );
        userService.saveUser(user);
    }

}