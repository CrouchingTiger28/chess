package server.websocket;

import com.google.gson.Gson;
import websocket.ResponseException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;
import websocket.commands.UserGameCommand.CommandType;
import websocket.messages.Notification;
import dataaccess.*;
import model.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private final AuthAccess authAccess = new AuthAccess();
    private final GameAccess gameAccess = new GameAccess();

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            UserGameCommand action = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (action.getCommandType()) {
                case CommandType.CONNECT -> enter(action.getGameID(), action.getAuthToken(), ctx.session);
                case CommandType.LEAVE -> exit(action.getGameID(), action.getAuthToken(), ctx.session);
            }
        } catch (IOException | DataAccessException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void enter(int gameID, String authToken, Session session) throws IOException, DataAccessException, SQLException {
        connections.add(gameID, session);
        AuthData authData = authAccess.getAuth(authToken);
        GameData gameData = gameAccess.getGame(gameID);

        String username = authData.username();
        String color = (Objects.equals(gameData.whiteUsername(), username)) ? "white" : "black";

        var message = String.format("%s has joined the game as %s", username, color);
        var notification = new Notification(Notification.Type.ARRIVAL, message);
        connections.broadcast(session, notification, gameID);
    }

    private void exit(int gameID, String authToken, Session session) throws IOException, DataAccessException, SQLException  {
        AuthData authData = authAccess.getAuth(authToken);
        GameData gameData = gameAccess.getGame(gameID);

        String username = authData.username();

        var message = String.format("%s has left the game", username);
        var notification = new Notification(Notification.Type.DEPARTURE, message);
        connections.broadcast(session, notification, gameID);
        connections.remove(gameID, session);
    }
}
