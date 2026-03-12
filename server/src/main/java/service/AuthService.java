package service;

import dataaccess.DataAccessException;

import java.sql.SQLException;

public class AuthService {
    dataaccess.AuthAccess auths = new dataaccess.AuthAccess();
    public AuthService() {

    }

    public void deleteAuths() throws DataAccessException, SQLException {
        auths.deleteAuthData();
    }

    public void logout(String authToken) throws NotAuthorizedException, DataAccessException, SQLException {
        if (auths.getAuth(authToken) == null) {
            throw(new NotAuthorizedException("Invalid AuthToken"));
        }
        else {
            auths.deleteAuth(authToken);
        }
    }
}
