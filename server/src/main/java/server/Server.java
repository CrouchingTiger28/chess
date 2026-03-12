package server;

import dataaccess.DatabaseManager;
import model.AuthData;
import model.UserData;
import model.GameData;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.*;
import io.javalin.http.BadRequestResponse;
import io.javalin.json.JavalinGson;
import service.AlreadyTakenException;
import service.InvalidLoginException;
import service.NotAuthorizedException;

import java.sql.Connection;
import java.sql.SQLException;

public class Server {

    private final Javalin javalin;

    public Server() {
        Gson gson = new Gson();
        try {
            configureDatabase();
        } catch (DataAccessException ex) {
            System.out.println("No");
        }
        service.AuthService authService = new service.AuthService();
        service.GameService gameService = new service.GameService();
        service.UserService userService = new service.UserService();


        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new JavalinGson());
        });


        // Register your endpoints and exception handlers here.
        javalin.post("/user", context -> {
            try {
                UserData registerRequest = context.bodyAsClass(UserData.class);
                AuthData registerResult = userService.register(registerRequest);

                context.status(200);
                context.json(registerResult);
            }
            catch(AlreadyTakenException e){
                context.status(403);
                context.result("{ \"message\": \"Error: already taken\" }");
            }
            catch(BadRequestResponse e) {
                context.status(400);
                context.result("{ \"message\": \"Error: bad request\" }");
            }
            catch (SQLException e) {
                context.status(500);
                context.result("{ \"message\": \"Error: server error\" }");
            }
        });

        javalin.post("/session", ctx -> {
            try {
                UserData loginRequest = ctx.bodyAsClass(UserData.class);
                AuthData loginResult = userService.login(loginRequest);

                ctx.status(200);
                ctx.json(gson.toJson(loginResult));
            }
            catch(BadRequestResponse e) {
                ctx.status(400);
                ctx.result("{ \"message\": \"Error: bad request\" }");
            }
            catch(InvalidLoginException e) {
                ctx.status(401);
                ctx.result("{ \"message\": \"Error: unauthorized\" }");
            }
            catch (SQLException e) {
                ctx.status(500);
                ctx.result("{ \"message\": \"Error: server error\" }");
            }
        });

        javalin.delete("/session", ctx -> {
            String token = extractAuth(ctx.header("Authorization"));

            try {
                authService.logout(token);

                ctx.status(200);
                ctx.result("");
            }
            catch (NotAuthorizedException  e) {
                ctx.status(401);
                ctx.result("{ \"message\": \"Error: unauthorized\" }");
            }
            catch (SQLException | DataAccessException e) {
                ctx.status(500);
                ctx.result("{ \"message\": \"Error: server error\" }");
            }
        });

        javalin.get("/game", ctx -> {
            String token = extractAuth(ctx.header("Authorization"));

            try {
                model.GameList games = gameService.listGames(token);

                ctx.status(200);
                ctx.json(games);
            }
            catch (NotAuthorizedException e) {
                ctx.status(401);
                ctx.result("{ \"message\": \"Error: unauthorized\" }");
            }
            catch (DataAccessException e) {
                ctx.status(500);
                ctx.result("{\"message\": \"Error: server error\"}");
            }
            catch (SQLException e) {
                ctx.status(500);
                ctx.result("{ \"message\": \"Error: server error\" }");
            }
        });

        javalin.post("/game", ctx -> {
            String token = extractAuth(ctx.header("Authorization"));

            try {
                GameData newGameRequest = ctx.bodyAsClass(GameData.class);
                int newGameResult = gameService.createGame(newGameRequest, token);

                ctx.status(200);
                ctx.json(gson.toJson(new model.GameIDValue(newGameResult)));
            }
            catch (NotAuthorizedException e) {
                ctx.status(401);
                ctx.result("{ \"message\": \"Error: unauthorized\" }");
            }
            catch (BadRequestResponse e) {
                ctx.status(400);
                ctx.result("{ \"message\": \"Error: bad request\" }");
            }
            catch (SQLException | DataAccessException e) {
                ctx.status(500);
                ctx.result("{ \"message\": \"Error: server error\" }");
            }
        });

        javalin.put("/game", ctx -> {
            String token = extractAuth(ctx.header("Authorization"));

            try {
                model.JoinRequest joinGameRequest = ctx.bodyAsClass(model.JoinRequest.class);
                gameService.joinGame(joinGameRequest, token);

                ctx.status(200);
                ctx.result("");
            }
            catch (NotAuthorizedException e) {
                ctx.status(401);
                ctx.result("{ \"message\": \"Error: unauthorized\" }");
            }
            catch (AlreadyTakenException e) {
                ctx.status(403);
                ctx.result("{ \"message\": \"Error: already taken\" }");
            }
            catch (DataAccessException | BadRequestResponse e) {
                ctx.status(400);
                ctx.result("{ \"message\": \"Error: bad request\" }");
            }
            catch (SQLException e) {
                ctx.status(500);
                ctx.result("{ \"message\": \"Error: server error\" }");
            }
        });

        javalin.delete("/db", ctx -> {

            try {
                userService.deleteUsers();
                gameService.deleteGames();
                authService.deleteAuths();

                ctx.status(200);
                ctx.json(java.util.Map.of());
            }
            catch (DataAccessException e) {
                ctx.status(400);
                ctx.result("{ \"message\": \"Error: bad request\" }");
            }
            catch (SQLException e) {
                ctx.status(500);
                ctx.result("{ \"message\": \"Error: server error\" }");
            }
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
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

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  users (
              `username` varchar(256) NOT NULL,
              `password` varchar(256) NOT NULL,
              `email` varchar(256),
              PRIMARY KEY (`username`),
              INDEX(username)
              );
            """,
            """
            CREATE TABLE IF NOT EXISTS  auths (
            `authToken` varchar(256) NOT NULL,
            `username` varchar(256) NOT NULL,
            PRIMARY KEY (`authToken`)
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS  games (
              `gameID` int NOT NULL AUTO_INCREMENT,
              `whiteUsername` varchar(256),
              `blackUsername` varchar(256),
              `gameName` varchar(256),
              `game` varchar(256) NOT NULL,
              PRIMARY KEY (`gameID`)
              );
            """
    };

    private void configureDatabase() throws DataAccessException{
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to configure database");
        }
    }
}
