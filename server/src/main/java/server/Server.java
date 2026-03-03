package server;

import Model.AuthData;
import Model.UserData;
import Model.GameData;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.*;
import io.javalin.http.BadRequestResponse;
import io.javalin.json.JavalinGson;
import service.AlreadyTakenException;
import service.InvalidLoginException;
import service.NotAuthorizedException;

public class Server {

    private final Javalin javalin;

    public Server() {
        Gson gson = new Gson();
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

        });

        javalin.delete("/session", ctx -> {
            String token = extractAuth(ctx.header("Authorization"));

            try {
                authService.logout(token);

                ctx.status(200);
                ctx.result("");
            }
            catch (NotAuthorizedException e) {
                ctx.status(401);
                ctx.result("{ \"message\": \"Error: unauthorized\" }");
            }
        });

        javalin.get("/game", ctx -> {
            String token = extractAuth(ctx.header("Authorization"));

            try {
                java.util.ArrayList<GameData> games = gameService.listGames(token);

                ctx.status(200);
                ctx.json(games);
            }
            catch (NotAuthorizedException e) {
                ctx.status(401);
                ctx.result("{ \"message\": \"Error: unauthorized\" }");
            }

        });

        javalin.post("/game", ctx -> {
            String token = extractAuth(ctx.header("Authorization"));

            try {
                GameData newGameRequest = ctx.bodyAsClass(GameData.class);
                int newGameResult = gameService.createGame(newGameRequest, token);

                ctx.status(200);
                ctx.json(gson.toJson(new Model.GameIDValue(newGameResult)));
            }
            catch (NotAuthorizedException e) {
                ctx.status(401);
                ctx.result("{ \"message\": \"Error: unauthorized\" }");
            }
            catch (BadRequestResponse e) {
                ctx.status(400);
                ctx.result("{ \"message\": \"Error: bad request\" }");
            }
        });

        javalin.put("/game", ctx -> {
            String token = extractAuth(ctx.header("Authorization"));

            try {
                Model.JoinRequest joinGameRequest = ctx.bodyAsClass(Model.JoinRequest.class);
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
        });

        javalin.delete("/db", ctx -> {
            userService.deleteUsers();
            gameService.deleteGames();
            authService.deleteAuths();

            ctx.status(200);
            ctx.json(java.util.Map.of());
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
}
