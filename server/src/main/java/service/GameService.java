package service;

import Model.GameData;
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
}
