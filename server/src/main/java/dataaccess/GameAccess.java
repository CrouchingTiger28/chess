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

    public void updateGame(int gameID, String playercolor, String username) {
        GameData game = getGame(gameID);
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

    public void deleteGame(int gameID) {
        games.removeIf(currentGame -> currentGame.gameID() == gameID);
    }

    public void deleteGames() {
        games = new ArrayList<>();
    }
}
