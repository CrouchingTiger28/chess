package dataaccess;

import Model.GameData;
import java.util.ArrayList;
import java.util.Objects;

public class GameAccess {

    private ArrayList<GameData> games = new ArrayList<>();
    public GameAccess() {

    }

    public void createGame(GameData data) {
        games.add(data);
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

    public GameData getGame(int gameID) {
        for (GameData currentGame : games) {
            if (currentGame.gameID() == gameID) {
                return currentGame;
            }
        }
        return null;
    }

    public ArrayList<GameData> listGames() {
        return games;
    }

    public Boolean deleteGame(int gameID) {
        return games.removeIf(currentGame -> currentGame.gameID() == gameID);
    }

    public void deleteGames() {
        games = new ArrayList<>();
    }
}
