package client;

import chess.ChessGame;
import model.GameData;
import model.GameList;

import java.util.ArrayList;
import java.util.List;

public class ClientCommunicator {

    public ClientCommunicator() {

    }

    public GameList listGames() {
        return new GameList(new ArrayList<>(List.of(new GameData(0, null,
                "BirdCowbow", "Our Game", new ChessGame()))));
    }
}
