package service;

import Model.AuthData;
import Model.UserData;
import java.util.UUID;
import java.util.Objects;

public class UserService {
    dataaccess.UserAccess users = new dataaccess.UserAccess();
    dataaccess.AuthAccess auths = new dataaccess.AuthAccess();
    public UserService() {

    }

    public AuthData register(UserData registerRequest) throws AlreadyTakenException {
        UserData user = users.getUser(registerRequest.username());
        if (user != null) {
            throw new AlreadyTakenException("User already exists");
        } else {
            users.createUser(registerRequest);
            AuthData newToken = new AuthData(UUID.randomUUID().toString(), registerRequest.username());
            auths.createAuth(newToken);
            return newToken;
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

    public void deleteUsers() {
        users.deleteUserData();
    }
}
