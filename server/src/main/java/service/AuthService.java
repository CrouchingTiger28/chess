package service;

import dataaccess.DataAccessException;

public class AuthService {
    dataaccess.AuthAccess auths = new dataaccess.AuthAccess();
    public AuthService() {

    }

    public void deleteAuths() throws DataAccessException{
        auths.deleteAuthData();
    }

    public void logout(String authToken) throws NotAuthorizedException, DataAccessException {
        if (auths.getAuth(authToken) == null) {
            throw(new NotAuthorizedException("Invalid AuthToken"));
        }
        else {
            auths.deleteAuth(authToken);
        }
    }
}
