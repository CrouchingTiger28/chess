package service;

import Model.GameData;
import chess.ChessGame;
import dataaccess.DataAccessException;
import io.javalin.http.BadRequestResponse;

import java.util.ArrayList;
import java.util.Objects;

public class GameService {
    dataaccess.GameAccess games = new dataaccess.GameAccess();
    dataaccess.AuthAccess auths = new dataaccess.AuthAccess();
    public GameService() {

    }

    public void deleteGames() {
        games.deleteGameData();
    }

    public ArrayList<GameData> listGames(String authToken) {
        checkAuth(authToken);

        return games.listGames();
    }

    public int createGame(GameData newGame, String authToken) throws NotAuthorizedException{
        checkAuth(authToken);

        int id = games.getCurrentID();
        games.createGame(new GameData(id, null, null, newGame.gameName(), new ChessGame()));
        return id;
    }

    public void joinGame(Model.JoinRequest request, String authToken) throws AlreadyTakenException, DataAccessException {
        checkAuth(authToken);

        GameData game = games.getGame(request.gameID());
        if (game == null) {
            throw new BadRequestResponse("Game does not exist");
        } else {
            if (Objects.equals(request.playerColor(), "white")) {
                if (game.whiteUsername() != null) {
                    games.updateGame(request.gameID(), "white", auths.getAuth(authToken).username());
                } else {
                    throw new AlreadyTakenException("White user already filled");
                }
            } else {
                if (game.whiteUsername() != null) {
                    games.updateGame(request.gameID(), "black", auths.getAuth(authToken).username());
                } else {
                    throw new AlreadyTakenException("Black user already filled");
                }
            }
        }
    }

    private void checkAuth(String authToken) {
        if (auths.getAuth(authToken) == null) {
            throw(new NotAuthorizedException("Invalid AuthToken"));
        }
    }
}
