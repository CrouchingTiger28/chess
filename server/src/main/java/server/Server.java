package server;

import Model.AuthData;
import Model.UserData;
import com.google.gson.Gson;
import io.javalin.*;
import service.AlreadyTakenException;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        Gson serializer = new Gson();
        service.AuthService authService = new service.AuthService();
        service.GameService gameService = new service.GameService();
        service.UserService userService = new service.UserService();

        // Register your endpoints and exception handlers here.
        javalin.post("/user", context -> {
            try {
                UserData registerRequest = context.bodyAsClass(UserData.class);
                AuthData registerResult = userService.register(registerRequest);

                context.status(200);
                context.json(serializer.toJson(registerResult));
            }
                catch(AlreadyTakenException e){
                context.status(403);
                context.json(serializer.toJson("{ \"message\": \"Error: already taken\" }"));
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
}
