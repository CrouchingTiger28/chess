package dataaccess;

import model.*;

import java.sql.*;

public class AuthAccess {

    public AuthAccess() {
    }

    public void createAuth(model.AuthData data) throws DataAccessException{
        var statement = "INSERT INTO auths (authToken, username) VALUES (?, ?)";
        ExecuteUpdate.execute(statement, data.authToken(), data.username());
    }

    public model.AuthData getAuth(String authToken) throws DataAccessException{
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken FROM auths WHERE authToken=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readAuth(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to read data.");
        }
        return null;
    }

    public void deleteAuth(String authToken) throws DataAccessException{
        var statement = "DELETE FROM auths WHERE authToken+?";
        ExecuteUpdate.execute(statement, authToken);
    }

    public void deleteAuthData() throws DataAccessException{
        var statement = "TRUNCATE auths";
        ExecuteUpdate.execute(statement);
    }

    private model.AuthData readAuth(ResultSet rs) throws SQLException {
        var authToken = rs.getString("authToken");
        var username = rs.getString("username");
        return new AuthData(authToken, username);
    }
}
