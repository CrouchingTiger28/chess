package dataaccess;

import model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

public class GameAccess {
    public GameAccess() {

    }

    public int createGame(GameData game) throws DataAccessException{
        var statement = "INSERT INTO whiteUsername, blackUsername, gameName, game (game) VALUES (?, ?, ?, ?)";
        return ExecuteUpdate.execute(statement, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
    }

    public void updateGame(int gameID, String playercolor, String username) throws DataAccessException{
        GameData game = getGame(gameID);
        if (game == null) {
            throw new DataAccessException("game does not exist");
        }
        deleteGame(gameID);

        if (Objects.equals(playercolor, "white")) {
            createGame(new GameData(0, username, game.blackUsername(), game.gameName(), game.game()));
        } else {
            createGame(new GameData(0, game.whiteUsername(), username, game.gameName(), game.game()));
        }
    }

    public GameData getGame(int gameID) throws DataAccessException{
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID FROM games WHERE gameID=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readGame(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to read data.");
        }
        return null;
    }

    public ArrayList<GameData> listGames() throws DataAccessException{
        ArrayList<GameData> result = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT id FROM games";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(readGame(rs));
                    }
                }
            }
        }
        catch (Exception e) {
            throw new DataAccessException("Unable to read data");
        }
        return result;
    }

    public void deleteGame(int gameID) throws DataAccessException{
        var statement = "DELETE FROM games WHERE gameID=?";
        ExecuteUpdate.execute(statement, gameID);
    }

    public void deleteGameData() throws DataAccessException {
        var statement = "TRUNCATE games";
        ExecuteUpdate.execute(statement);
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        var gameID = rs.getInt("gameID");
        var whiteUsername = rs.getString("whiteUsername");
        var blackUsername = rs.getString("blackUsername");
        var gameName = rs.getString("gameName");
        chess.ChessGame game = rs.getObject("game", chess.ChessGame.class);
        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
    }
}
