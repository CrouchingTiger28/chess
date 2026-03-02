package service;

import Model.GameData;
import chess.ChessGame;

import java.util.ArrayList;

public class GameService {
    dataaccess.GameAccess games = new dataaccess.GameAccess();
    dataaccess.AuthAccess auths = new dataaccess.AuthAccess();
    public GameService() {

    }

    public void deleteGames() {
        games.deleteGameData();
    }

    public ArrayList<GameData> listGames(String authToken) {
        if (auths.getAuth(authToken) == null) {
            throw(new NotAuthorizedException("Invalid AuthToken"));
        } else {
            return games.listGames();
        }
    }

    public int createGame(GameData newGame, String authToken) throws NotAuthorizedException{
        if (auths.getAuth(authToken) == null) {
            throw(new NotAuthorizedException("Invalid AuthToken"));
        } else {
            int id = games.getCurrentID();
            games.createGame(new GameData(id, null, null, newGame.gameName(), new ChessGame()));
            return id;
        }
    }
}
