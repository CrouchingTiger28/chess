package ui;

import model.GameData;
import model.GameList;

public class DrawBoard {

    public DrawBoard() {

    }

    public void drawWhite(GameData game, GameList gameList) {
        System.out.println("Drawing board from white perspective.");

        System.out.println(game.game().getBoard());
    }

    public void drawBlack(GameData game, GameList gameList) {
        System.out.println("Drawing board from black perspective.");

        System.out.println(game.game().getBoard());
    }
}
