package client;

import model.*;

public class ServerFacade {
    private static ClientCommunicator comm = new ClientCommunicator();

    public String register(UserData user) {
        AuthData newAuth = comm.doPost("/user", null, user.username(), user.password(), user.email());
        if (newAuth != null) {
            return newAuth.authToken();
        } else {
            throw new RuntimeException("something went wrong :(");
        }
    }

    public String login(UserData user) {
        AuthData newAuth = comm.doPost("/session", null, user.username(), user.password());
        if (newAuth != null) {
            return newAuth.authToken();
        } else {
            throw new RuntimeException("something went wrong :(");
        }
    }

    public void logout(String authToken) {
        comm.doDelete("/session", authToken);
    }

    public GameList listGames() {
        //return a GameList of all the games. Exactly what it says on the tin.
        GameList list = comm.listGames();
        return list;
    }

}
