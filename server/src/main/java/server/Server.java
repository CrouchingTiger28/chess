package server;

import Model.AuthData;
import Model.UserData;
import com.google.gson.Gson;
import io.javalin.*;
import io.javalin.json.JavalinGson;
import service.AlreadyTakenException;

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
                context.json(gson.toJson(registerResult));
            }
                catch(AlreadyTakenException e){
                context.status(403);
                context.json(gson.toJson("{ \"message\": \"Error: already taken\" }"));
            }
        });

        javalin.delete("/db", ctx -> {
            userService.deleteUsers();
            gameService.deleteGames();
            authService.deleteAuths();

            ctx.status(200);
            ctx.json(gson.toJson(""));
        });

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
