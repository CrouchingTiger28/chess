package service;

public class AuthService {
    dataaccess.AuthAccess auths = new dataaccess.AuthAccess();
    public AuthService() {

    }

    public void deleteAuths() {
        auths.deleteAuthData();
    }

    public void logout(String authToken) {
        auths.deleteAuth(authToken);
    }
}
