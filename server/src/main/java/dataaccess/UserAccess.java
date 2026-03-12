package dataaccess;

import model.*;

import java.sql.*;

public class UserAccess {

    public UserAccess() {
    }

    public model.UserData getUser(String username) throws DataAccessException, SQLException{
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password, email FROM users WHERE username=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readUser(rs);
                    }
                }
            }
        }
        return null;
    }

    public void createUser(model.UserData data) throws DataAccessException, SQLException{
        var statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        ExecuteUpdate.execute(statement, data.username(), data.password(), data.email());
    }

    public void deleteUserData() throws DataAccessException, SQLException{
        var statement = "TRUNCATE users";
        ExecuteUpdate.execute(statement);
    }

    private UserData readUser(ResultSet rs) throws SQLException {
        var username = rs.getString("username");
        var password = rs.getString("password");
        var email = rs.getString("email");
        return new UserData(username, password, email);
    }
}
