package client;

import chess.*;
import model.GameData;
import model.GameList;
import ui.DrawBoard;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class ClientMain {

    private boolean loggedIn = false;
    private String authToken = null;
    private ui.DrawBoard boardPen = new ui.DrawBoard();
    private GameList gameList = null;
    private static ServerFacade serverFacade = new ServerFacade();
    private String menuLayer = "first";
    private Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        ClientMain self = new ClientMain();
        self.updateGameList();
        self.menu();
    }

    private int repl(List<String> options, int numOfOptions) {
        int input;

        while (true) {
            for (var i = 0; i < options.size(); i++) {
                System.out.printf("\t%d. %s%n", i + 1, options.get(i));
            }
            System.out.print("\n");

            try {
                input = scanner.nextInt();

                if (0 < input && input <= numOfOptions) {
                    return input;
                }
            } catch (InputMismatchException ex) {
                System.out.println("Invalid input. Please try again.\n Select Help for more information.\n");
                scanner.next();
            }
        }

    }

    private void menu() {
        int firstChoice;
        int secondChoice;

        while (true) {

            if (!loggedIn) {
                firstChoice = repl(List.of("login", "register", "help", "quit"), 4);
                if (!preloginMenuItem(firstChoice)) {
                    break;
                }
            } else {
                secondChoice = repl(List.of("help", "logout", "create game", "list games", "play game", "observe game"), 6);
                postloginMenuItem(secondChoice);
            }

        }
        scanner.close();

    }

    private void printHelp() {
        String preLoginHelp = "This is the help string before logging in.";
        String postLoginHelp = "This is the help string after logging in.";

        if (!loggedIn) {
            System.out.println(preLoginHelp);
        } else {
            System.out.println(postLoginHelp);
        }
    }

    private boolean preloginMenuItem(int option) {
        switch (option) {
            case 1:
                //login
                loggedIn = true;
                return true;
            case 2:
                //register
                loggedIn = true;
                return true;
            case 3:
                printHelp();
                return true;
            case 4:
                //quit
                return false;
            default:
                System.out.printf("%d %s", option, "is not a valid input. Select help (3) for additional assistance.\n");
                return true;
        }
    }

    private void postloginMenuItem(int option) {
        switch (option) {
            case 1:
                printHelp();
                break;
            case 2:
                loggedIn = false;
                break;
            case 3:
                //create game
                break;
            case 4:
                //list games
                listGames();
                break;
            case 5:
                //play game
                joinGame();
                break;
            case 6:
                //observe game
                watchGame();
                break;
            default:
                System.out.printf("%d %s", option, "is not a valid input. Select help (1) for additional assistance.\n");
        }
    }

    private void listGames() {
        updateGameList();
        String whitePlayer;
        String blackPlayer;
        for (int i = 0; i < gameList.games().size(); i ++) {
            GameData game = gameList.games().get(i);
            whitePlayer = (game.whiteUsername() == null) ? "none" : game.whiteUsername();
            blackPlayer = (game.blackUsername() == null) ? "none" : game.blackUsername();
            System.out.printf("%d: %s. %s: %s, %s: %s%n%n", i+1, game.gameName(), "White Player", game.whiteUsername(), "Black Player", game.blackUsername());
        }
    }

    private boolean watchGame() {
        Scanner scanner = new Scanner(System.in);
        int input = 0;
        boolean tryJoin = true;
        int gameID = 0;

        while (tryJoin) {
            try {
                input = scanner.nextInt();

                if (input == 0) {
                    tryJoin = false;
                }

                gameID = getGameID(input);
                if (gameID > 0) {
                    tryJoin = false;
                }
            } catch (InputMismatchException ex) {
                System.out.println("Invalid input. Please try again.\n Return to previous menu by pressing 0.\n");
                scanner.next();
            }
        }
        if (input == 0) {
            return false;
        } else {
            boardPen.drawWhite();
            return true;
        }
    }

    private boolean joinGame() {
        Scanner scanner = new Scanner(System.in);
        int input;
        boolean joinID = true;
        boolean joinColor = true;
        int gameID = 0;

        while (joinColor) {
            try {
                input = scanner.nextInt();

                if (joinID) {
                    gameID = getGameID(input);
                    if (gameID > 0) {
                        joinID = false;
                    }
                }

                if (input == 1) {
                    //white
                    joinColor = false;
                    boardPen.drawWhite();
                } else if (input == 2) {
                    //black
                    joinColor = false;
                    boardPen.drawBlack();
                } else {
                    System.out.printf("%d %s", input, "is not a valid input.\n");
                }
            } catch (InputMismatchException ex) {
                System.out.println("Invalid input. Please try again.\n");
                scanner.next();
            }
        }
        return true;
    }

    private void updateGameList() {
        gameList = serverFacade.listGames();
    }

    private int getGameID(int gameNumber) {
        try {
            return gameList.games().get(gameNumber - 1).gameID();
        } catch (IndexOutOfBoundsException ex) {
            return 0;
        }
    }
}