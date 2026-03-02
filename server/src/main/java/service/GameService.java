package service;

public class GameService {
    dataaccess.GameAccess games = new dataaccess.GameAccess();
    public GameService() {

    }

    public void deleteGames() {
        games.deleteGameData();
    }
}
