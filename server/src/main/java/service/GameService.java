package service;

import model.GameData;
import chess.ChessGame;
import dataaccess.DataAccessException;
import io.javalin.http.BadRequestResponse;

public class GameService {
    dataaccess.GameAccess games = new dataaccess.GameAccess();
    dataaccess.AuthAccess auths = new dataaccess.AuthAccess();
    public GameService() {

    }

    public void deleteGames() throws DataAccessException{
        games.deleteGameData();
    }

    public model.GameList listGames(String authToken) throws DataAccessException{
        checkAuth(authToken);

        return new model.GameList(games.listGames());
    }

    public int createGame(GameData newGame, String authToken) throws NotAuthorizedException, DataAccessException{
        if (newGame.gameName() == null) {
            throw new BadRequestResponse("No game name given");
        }
        checkAuth(authToken);

        return games.createGame(new GameData(0, null, null, newGame.gameName(), new ChessGame()));
    }

    public void joinGame(model.JoinRequest request, String authToken) throws AlreadyTakenException, DataAccessException {
        checkAuth(authToken);

        GameData game = games.getGame(request.gameID());
        if (game == null) {
            throw new BadRequestResponse("Game does not exist");
        } else {
            if (request.playerColor() == ChessGame.TeamColor.WHITE) {
                if (game.whiteUsername() == null) {
                    games.updateGame(request.gameID(), "white", auths.getAuth(authToken).username());
                } else {
                    throw new AlreadyTakenException("White user already filled");
                }
            } else if (request.playerColor() == ChessGame.TeamColor.BLACK) {
                if (game.blackUsername() == null) {
                    games.updateGame(request.gameID(), "black", auths.getAuth(authToken).username());
                } else {
                    throw new AlreadyTakenException("Black user already filled");
                }
            } else {
                throw new BadRequestResponse("Invalid player color");
            }
        }
    }

    private void checkAuth(String authToken) throws DataAccessException, NotAuthorizedException{
        if (auths.getAuth(authToken) == null) {
            throw(new NotAuthorizedException("Invalid AuthToken"));
        }
    }
}
