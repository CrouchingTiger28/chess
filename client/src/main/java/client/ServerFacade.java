package client;

import model.GameList;

public class ServerFacade {
    private static ClientCommunicator comm = new ClientCommunicator();


    public GameList listGames() {
        //return a GameList of all the games. Exactly what it says on the tin.
        GameList list = comm.listGames();

        return list;
    }
}
