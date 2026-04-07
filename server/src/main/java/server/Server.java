package server;

import dataaccess.DatabaseManager;
import model.HandlerResponse;
import dataaccess.DataAccessException;
import io.javalin.*;
import io.javalin.json.JavalinGson;

import java.sql.Connection;
import java.sql.SQLException;

public class Server {

    private final Javalin javalin;

    public Server() {
        try {
            configureDatabase();
        } catch (DataAccessException ex) {
            System.out.println("No");
        }
        Handler handler = new Handler();



        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new JavalinGson());
            config.router.mount(router -> {
                router.ws("/ws", ws -> {
                    ws.onConnect(ctx -> {
                        ctx.enableAutomaticPings();
                        System.out.println("Websocket connected");
                    });
                    ws.onMessage(ctx -> ctx.send("WebSocket response:" + ctx.message()));
                    ws.onClose(_ -> System.out.println("Websocket closed"));
                });
            });
        });


        // Register your endpoints and exception handlers here.
        javalin.post("/user", context -> {
            HandlerResponse response = handler.register(context);
            context.status(response.status());
            context.result(response.result());
        });

        javalin.post("/session", ctx -> {
            HandlerResponse response = handler.login(ctx);
            ctx.status(response.status());
            ctx.result(response.result());
        });

        javalin.delete("/session", ctx -> {
            HandlerResponse response = handler.logout(ctx);
            ctx.status(response.status());
            ctx.result(response.result());
        });

        javalin.get("/game", ctx -> {
            HandlerResponse response = handler.listGames(ctx);
            ctx.status(response.status());
            ctx.result(response.result());
        });

        javalin.post("/game", ctx -> {
            HandlerResponse response = handler.createGame(ctx);
            ctx.status(response.status());
            ctx.result(response.result());
        });

        javalin.put("/game", ctx -> {
            HandlerResponse response = handler.joinGame(ctx);
            ctx.status(response.status());
            ctx.result(response.result());
        });

        javalin.delete("/db", ctx -> {
            HandlerResponse response = handler.clear();
            ctx.status(response.status());
            ctx.result(response.result());
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    public int port() {
        return javalin.port();
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
              `gameName` varchar(256) NOT NULL,
              `game` TEXT NOT NULL,
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
