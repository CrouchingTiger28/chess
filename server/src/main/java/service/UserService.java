package service;

import Model.AuthData;
import Model.UserData;
import io.javalin.http.BadRequestResponse;

import java.util.UUID;
import java.util.Objects;

public class UserService {
    dataaccess.UserAccess users = new dataaccess.UserAccess();
    dataaccess.AuthAccess auths = new dataaccess.AuthAccess();
    public UserService() {

    }

    public AuthData register(UserData registerRequest) throws AlreadyTakenException, BadRequestResponse {
        if (registerRequest.username() == null || registerRequest.password() == null) {
            throw new BadRequestResponse("Username or Password not supplied");
        }
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

    public AuthData login(UserData loginRequest) {
        if (loginRequest.password() == null || loginRequest.username() == null) {
            throw new BadRequestResponse("Username or password fields blank");
        }
        UserData user = users.getUser(loginRequest.username());
        if (user == null || !Objects.equals(user.password(), loginRequest.password())) {
            throw new InvalidLoginException("Incorrect Username or Password");
        } else {
            AuthData newToken = new AuthData(UUID.randomUUID().toString(), loginRequest.username());
            auths.createAuth(newToken);
            return newToken;
        }
    }

    public void deleteUsers() {
        users.deleteUserData();
    }
}
