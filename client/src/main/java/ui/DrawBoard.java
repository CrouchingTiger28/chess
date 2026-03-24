package ui;

import model.GameList;

public class DrawBoard {

    public DrawBoard() {

    }

    public void drawWhite(int gameID, GameList gameList) {
        System.out.println("Drawing board from white perspective.");
    }

    public void drawBlack(int gameID, GameList gameList) {
        System.out.println("Drawing board from black perspective.");
    }
}
