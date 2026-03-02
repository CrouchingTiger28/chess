package dataaccess;

import Model.GameData;
import java.util.ArrayList;
import java.util.Objects;

public class GameAccess {

    private static ArrayList<GameData> games = new ArrayList<>();
    private static int currentID = 0;
    public GameAccess() {

    }

    public void createGame(GameData game) {
        currentID ++;
        games.add(game);
    }

    public int getCurrentID() {
        return currentID;
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

    public void deleteGame(int gameID) {
        games.removeIf(currentGame -> currentGame.gameID() == gameID);
    }

    public void deleteGameData() {
        currentID = 0;
        games = new ArrayList<>();
    }
}
