package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import model.*;
import service.*;

import java.sql.SQLException;

public class Handler {
    private final service.AuthService authService = new AuthService();
    private final service.GameService gameService = new service.GameService();
    private final service.UserService userService = new service.UserService();
    private final Gson gson = new Gson();
    public Handler() {


    }

    public HandlerResponse register(Context context){
        try {
            UserData registerRequest = context.bodyAsClass(UserData.class);
            AuthData registerResult = userService.register(registerRequest);

            return new HandlerResponse(200, gson.toJson(registerResult));
        }
        catch(AlreadyTakenException e){
            return new HandlerResponse(403, "{ \"message\": \"Error: already taken\" }");
        }
        catch(BadRequestResponse e) {
            return new HandlerResponse(400, "{ \"message\": \"Error: bad request\" }");
        }
        catch (SQLException | DataAccessException e) {
            return new HandlerResponse(500, "{ \"message\": \"Error: server error\" }");
        }
    }

    public HandlerResponse login(Context context) {
        try {
            UserData loginRequest = context.bodyAsClass(UserData.class);
            AuthData loginResult = userService.login(loginRequest);

            return new HandlerResponse(200, gson.toJson(loginResult));
        }
        catch(BadRequestResponse e) {
            return new HandlerResponse(400, "{ \"message\": \"Error: bad request\" }");
        }
        catch(InvalidLoginException e) {
            return new HandlerResponse(401, "{ \"message\": \"Error: unauthorized\" }");
        }
        catch (SQLException | DataAccessException e) {
            return new HandlerResponse(500, "{ \"message\": \"Error: server error\" }");
        }
    }

    public HandlerResponse logout(Context context) {
        String token = extractAuth(context.header("Authorization"));

        try {
            authService.logout(token);

            return new HandlerResponse(200, "");
        }
        catch (NotAuthorizedException  e) {
            return new HandlerResponse(401, "{ \"message\": \"Error: unauthorized\" }");
        }
        catch (SQLException | DataAccessException e) {
            return new HandlerResponse(500, "{ \"message\": \"Error: server error\" }");
        }
    }

    public HandlerResponse listGames(Context context) {
        String token = extractAuth(context.header("Authorization"));

        try {
            model.GameList games = gameService.listGames(token);

            return new HandlerResponse(200, gson.toJson(games));
        }
        catch (NotAuthorizedException e) {
            return new HandlerResponse(401, "{ \"message\": \"Error: unauthorized\" }");
        }
        catch (DataAccessException | SQLException e) {
            return new HandlerResponse(500, "{ \"message\": \"Error: server error\" }");
        }
    }

    public HandlerResponse createGame(Context context) {
        String token = extractAuth(context.header("Authorization"));

        try {
            GameData newGameRequest = context.bodyAsClass(GameData.class);
            int newGameResult = gameService.createGame(newGameRequest, token);

            return new HandlerResponse(200, gson.toJson(new model.GameIDValue(newGameResult)));
        }
        catch (NotAuthorizedException e) {
            return new HandlerResponse(401, "{ \"message\": \"Error: unauthorized\" }");
        }
        catch (BadRequestResponse e) {
            return new HandlerResponse(400, "{ \"message\": \"Error: bad request\" }");
        }
        catch (SQLException | DataAccessException e) {
            return new HandlerResponse(500, "{ \"message\": \"Error: server error\" }");
        }
    }

    public HandlerResponse joinGame(Context context) {
        String token = extractAuth(context.header("Authorization"));

        try {
            model.JoinRequest joinGameRequest = context.bodyAsClass(model.JoinRequest.class);
            gameService.joinGame(joinGameRequest, token);

            return new HandlerResponse(200, "");
        }
        catch (NotAuthorizedException e) {
            return new HandlerResponse(401, "{ \"message\": \"Error: unauthorized\" }");
        }
        catch (AlreadyTakenException e) {
            return new HandlerResponse(403, "{ \"message\": \"Error: already taken\" }");
        }
        catch (BadRequestResponse e) {
            return new HandlerResponse(400, "{ \"message\": \"Error: bad request\" }");
        }
        catch (SQLException | DataAccessException e) {
            return new HandlerResponse(500, "{ \"message\": \"Error: server error\" }");
        }
    }

    public HandlerResponse clear() {

        try {
            userService.deleteUsers();
            gameService.deleteGames();
            authService.deleteAuths();

            return new HandlerResponse(200, gson.toJson(java.util.Map.of()));
        }
        catch (SQLException | DataAccessException e) {
            return new HandlerResponse(500, "{ \"message\": \"Error: server error\" }");
        }
    }

    private String extractAuth(String header) {
        String token;
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring("Bearer ".length());
        } else {
            token = header;
        }
        return token;
    }
}
