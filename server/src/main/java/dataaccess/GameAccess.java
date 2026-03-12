package dataaccess;

import chess.ChessGame;
import model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;
import com.google.gson.Gson;

public class GameAccess {
    Gson gson;
    public GameAccess() {
        this.gson = new Gson();
    }

    public int createGame(GameData game) throws DataAccessException{
        if (game.gameID() == 0) {
            var statement = "INSERT INTO games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";
            return ExecuteUpdate.execute(statement, game.whiteUsername(),
                    game.blackUsername(), game.gameName(), gson.toJson(game.game()));
        } else {
            var statement = "INSERT INTO games (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?, ?)";
            return ExecuteUpdate.execute(statement, game.gameID(), game.whiteUsername(),
                    game.blackUsername(), game.gameName(), gson.toJson(game.game()));
        }
    }

    public void updateGame(int gameID, String playercolor, String username) throws DataAccessException{
        GameData game = getGame(gameID);
        if (game == null) {
            throw new DataAccessException("game does not exist");
        }
        deleteGame(gameID);

            if (Objects.equals(playercolor, "white")) {
                createGame(new GameData(gameID, username, game.blackUsername(), game.gameName(), game.game()));
            } else {
                createGame(new GameData(gameID, game.whiteUsername(), username, game.gameName(), game.game()));
            }
    }

    public GameData getGame(int gameID) throws DataAccessException{
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM games WHERE gameID=?";
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
            var statement = "SELECT * FROM games";
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
        chess.ChessGame game = gson.fromJson(rs.getString("game"), ChessGame.class);
        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
    }
}
