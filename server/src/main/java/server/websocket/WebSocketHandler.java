package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import service.GameService;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.MakeMoveCommand;
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

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            JsonObject jo = JsonParser.parseString(ctx.message()).getAsJsonObject();
            String type = jo.get("commandType").getAsString();
            if (Objects.equals(type, "MAKE_MOVE")) {
                MakeMoveCommand action = new Gson().fromJson(ctx.message(), MakeMoveCommand.class);
                makeMove(action.getMove(), action.getGameID(), action.getAuthToken(), ctx.session);
            } else {
                UserGameCommand action = new Gson().fromJson(ctx.message(), UserGameCommand.class);
                switch (action.getCommandType()) {
                    case CommandType.CONNECT -> enter(action.getGameID(), action.getAuthToken(), ctx.session);
                    case CommandType.LEAVE -> exit(action.getGameID(), action.getAuthToken(), ctx.session);
                    case CommandType.RESIGN -> resign(action.getGameID(), action.getAuthToken(), ctx.session);
                }
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
                String message;
                if (Objects.equals(gameData.whiteUsername(), username) || Objects.equals(gameData.blackUsername(), username)) {
                    String color = (Objects.equals(gameData.whiteUsername(), username)) ? "white" : "black";
                    message = String.format("%s has joined the game as %s", username, color);
                } else {
                    message = String.format("%s is observing the game.", username);
                }

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
            GameData gameData = gameAccess.getGame(gameID);

            if (authData != null) {
                String username = authData.username();
                String changeName = (Objects.equals(gameData.whiteUsername(), username)) ? "white" : "black";

                gameAccess.updateGame(gameData.gameID(), changeName, null);

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
                if (!Objects.equals(authData.username(),(gameData.whiteUsername())) && !Objects.equals(authData.username(), (gameData.blackUsername()))) {
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
                    connections.homeBoard(session, new NotificationMessage("You have conceded the game."));
                }
            } else {
                throw new DataAccessException("Websocket failed :/");
            }
        } catch (DataAccessException | SQLException ex) {
            connections.homeBoard(session, new ErrorMessage("An error has occurred :("));
        }
    }

    private void makeMove(ChessMove move, int gameID, String authToken, Session session) throws IOException{
        try {
            AuthData authData = authAccess.getAuth(authToken);
            GameData gameData = gameAccess.getGame(gameID);

            if (authData != null && gameData != null) {
                ChessGame.TeamColor turnColor = gameData.game().getTeamTurn();
                ChessGame.TeamColor playerColor = (Objects.equals(authData.username(), gameData.blackUsername())) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
                ChessGame.TeamColor pieceColor = gameData.game().getBoard().getPiece(move.getStartPosition()).getTeamColor();
                if (!Objects.equals(authData.username(), gameData.whiteUsername()) && !Objects.equals(authData.username(), gameData.blackUsername())) {
                    connections.homeBoard(session, new ErrorMessage("Error: you cannot make moves. You are an observer."));
                } else if (gameData.game().isGameOver()) {
                    connections.homeBoard(session, new ErrorMessage("Error: you cannot make any moves. The game is already over."));
                } else if (playerColor != pieceColor) {
                    connections.homeBoard(session, new ErrorMessage(String.format("Error: right now you're playing %s; you can't move %s pieces", playerColor, pieceColor)));
                } else if (playerColor != turnColor) {
                    connections.homeBoard(session, new ErrorMessage("Error: you cannot make any moves. It's not your turn."));
                } else {
                    gameData = gameAccess.makeMove(gameID, move);

                    String username = authData.username();

                    var message = String.format("%s has moved %s.", username, move);
                    var notificationMessage = new NotificationMessage(message);
                    var loadGameMessage = new LoadGameMessage(gameData);
                    connections.broadcast(session, notificationMessage, gameID);
                    connections.broadcast(session, loadGameMessage, gameID);
                    connections.homeBoard(session, loadGameMessage);

                    checkGameOver(gameData, authData, session);
                }
            } else {
                throw new DataAccessException("Websocket failed :/");
            }
        } catch (DataAccessException | SQLException ex) {
            connections.homeBoard(session, new ErrorMessage("An error has occurred :("));
        } catch (InvalidMoveException ex) {
            connections.homeBoard(session, new ErrorMessage("That move is invalid, select '6' to highlight legal moves."));
        }
    }

    private void checkGameOver(GameData game, AuthData data, Session session) throws DataAccessException, SQLException, IOException {
        ChessGame.TeamColor nowThreatened = (Objects.equals(game.whiteUsername(), data.username())) ?
                ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

        if (game.game().isInCheckmate(nowThreatened)) {
            gameAccess.endGame(game.gameID());
            connections.homeBoard(session, new NotificationMessage(String.format("Checkmate!%nCongratulations, you have won the game!")));
            connections.broadcast(session, new NotificationMessage(String.format("Checkmate.%n%s has won the game.", data.username())), game.gameID());
        } else if (game.game().isInStalemate(nowThreatened)) {
            gameAccess.endGame(game.gameID());
            connections.broadcast(null, new NotificationMessage("Uh oh, looks like a stalemate. It's a draw!"), game.gameID());
        } else {
            checkCheck(game, data, session);
        }
    }

    private void checkCheck(GameData game, AuthData data, Session session) throws DataAccessException, SQLException, IOException {
        ChessGame.TeamColor nowThreatened = (Objects.equals(game.whiteUsername(), data.username())) ?
                ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

        if (game.game().isInCheck(nowThreatened)) {
            connections.homeBoard(session, new NotificationMessage(String.format("You have put %s in check.", nowThreatened)));
            connections.broadcast(session, new NotificationMessage(String.format("%s is now in check.", nowThreatened.toString().toLowerCase())), game.gameID());
        }
    }
}
