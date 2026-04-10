package server.websocket;

import com.google.gson.Gson;
import service.GameService;
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
import websocket.messages.*;
import dataaccess.*;
import model.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    private final ConnectionManager connections = new ConnectionManager();
    private final AuthAccess authAccess = new AuthAccess();
    private final GameAccess gameAccess = new GameAccess();
    private final GameService gameService = new GameService();

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
                case CommandType.RESIGN -> resign(action.getGameID(), action.getAuthToken(), ctx.session);
            }
        } catch (IOException | DataAccessException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void enter(int gameID, String authToken, Session session) throws IOException {
        try {
            AuthData authData = authAccess.getAuth(authToken);
            GameData gameData = gameAccess.getGame(gameID);

            if (gameData != null && authData != null) {
                connections.add(gameID, session);
                String username = authData.username();
                String color = (Objects.equals(gameData.whiteUsername(), username)) ? "white" : "black";

                String message = String.format("%s has joined the game as %s", username, color);
                connections.broadcast(session, new NotificationMessage(message), gameID);
                connections.homeBoard(session, new LoadGameMessage(gameData));
            } else {
                throw new DataAccessException("Websocket failed :/");
            }
        } catch (DataAccessException | SQLException ex) {
            connections.homeBoard(session, new ErrorMessage("An error has occurred :("));
        }
    }

    private void exit(int gameID, String authToken, Session session) throws IOException, DataAccessException, SQLException  {
        try {
            AuthData authData = authAccess.getAuth(authToken);

            if (authData != null) {
                String username = authData.username();

                var message = String.format("%s has left the game", username);
                var notificationMessage = new NotificationMessage(message);
                connections.broadcast(session, notificationMessage, gameID);
                connections.remove(gameID, session);
            } else {
                throw new DataAccessException("Websocket failed :/");
            }
        } catch (DataAccessException | SQLException ex) {
            connections.homeBoard(session, new ErrorMessage("An error has occurred :("));
        }
    }

    private void resign(int gameID, String authToken, Session session) throws IOException, DataAccessException, SQLException  {
        try {
            AuthData authData = authAccess.getAuth(authToken);
            GameData gameData = gameAccess.getGame(gameID);

            if (authData != null && gameData != null) {
                if (!authData.username().equals(gameData.whiteUsername()) && !authData.username().equals(gameData.blackUsername())) {
                    connections.homeBoard(session, new ErrorMessage("Error: you cannot concede this game. You are an observer."));
                } else if (gameData.game().isGameOver()) {
                    connections.homeBoard(session, new ErrorMessage("Error: you cannot concede this game. It is already over."));
                } else {
                    gameAccess.endGame(gameID);

                    String username = authData.username();
                    String winningColor = (Objects.equals(gameData.whiteUsername(), username)) ? "black" : "white";

                    var message = String.format("%s has resigned the game, and %s has won.", username, winningColor);
                    var notificationMessage = new NotificationMessage(message);
                    connections.broadcast(session, notificationMessage, gameID);
                    connections.remove(gameID, session);
                    connections.homeBoard(session, new NotificationMessage("You have conceded the game."));
                }
            } else {
                throw new DataAccessException("Websocket failed :/");
            }
        } catch (DataAccessException | SQLException ex) {
            connections.homeBoard(session, new ErrorMessage("An error has occurred :("));
        }
    }
}
