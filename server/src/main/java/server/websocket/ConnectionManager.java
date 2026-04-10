package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConnectionManager {
    public final HashMap<Integer, List<Session>> connections = new HashMap<>();

    public void add(int gameID, Session session) {
        if (!connections.containsKey(gameID)) {
            connections.put(gameID, new ArrayList<>());
        }
        connections.get(gameID).add(session);
    }

    public void remove(int gameID, Session session) {
        if (connections.containsKey(gameID)) {
            connections.get(gameID).remove(session);
        }
    }

    public void broadcast(Session excludeSession, NotificationMessage message, int gameID) throws IOException {
        List<Session> values = connections.get(gameID);
        for (Session c : values)
            if (c.isOpen()) {
                if (!c.equals(excludeSession)) {
                    c.getRemote().sendString(new Gson().toJson(message));
                }
            }
    }

    public void homeBoard(Session homeSession, ServerMessage message) throws IOException{
        homeSession.getRemote().sendString(new Gson().toJson(message));
    }

}