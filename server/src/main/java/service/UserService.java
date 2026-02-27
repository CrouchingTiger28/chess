package service;

import Model.AuthData;
import Model.UserData;

import java.util.Objects;

public class UserService {
    dataaccess.UserAccess users = new dataaccess.UserAccess();
    public UserService() {

    }

    public UserData register(UserData registerRequest) throws AlreadyTakenException {
        UserData user = users.getUser(registerRequest.username());
        if (user != null) {
            throw new AlreadyTakenException("User already exists");
        } else {
            users.createUser(registerRequest);
            return registerRequest;
        }
    }

    public UserData login(UserData loginRequest) {
        UserData user = users.getUser(loginRequest.username());
        if (user == null || !Objects.equals(user.password(), loginRequest.password())) {
            throw new InvalidLoginException("Incorrect Username or Password");
        } else {
            return user;
        }
    }

}
