package client;

import chess.*;
import model.GameData;
import ui.DrawBoard;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class ClientMain {

    private boolean loggedIn = false;
    private String authToken = null;
    private ui.DrawBoard boardPen = new ui.DrawBoard();
    private List<GameData> gameList = List.of();
    private static ServerFacade serverFacade = new ServerFacade();

    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        ClientMain self = new ClientMain();
        self.menu();
    }

    private void menu() {
        Scanner scanner = new Scanner(System.in);
        int input;
        boolean displayMenu = true;

        while (displayMenu) {
            if (!loggedIn) {
                printPreloginMenu();
            } else {
                printPostloginMenu();
            }

            try {
                input = scanner.nextInt();

                if (loggedIn) {
                    postloginMenuItem(input);
                } else {
                    displayMenu = preloginMenuItem(input);
                }
            } catch (InputMismatchException ex) {
                System.out.println("Invalid input. Please try again.\n");
                scanner.next();
                printHelp(loggedIn);
            }
        }
        scanner.close();
    }

    private void printPreloginMenu() {
        List<String> options = List.of("login", "register", "help", "quit");
        for (var i = 0; i < options.size(); i++) {
            System.out.printf("\t%d. %s%n", i + 1, options.get(i));
        }
        System.out.print("\n");
    }

    private void printPostloginMenu() {
        List<String> options = List.of("help", "logout", "create game", "list games", "play game", "observe game");
        for (var i = 0; i < options.size(); i++) {
            System.out.printf("\t%d. %s%n", i + 1, options.get(i));
        }
        System.out.print("\n");
    }

    private void printHelp(Boolean loggedIn) {
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
                printHelp(false);
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
                printHelp(true);
                break;
            case 2:
                loggedIn = false;
                break;
            case 3:
                //create game
                break;
            case 4:
                //list games
                break;
            case 5:
                //play game
                joinGame();
                break;
            case 6:
                //observe game
                break;
            default:
                System.out.printf("%d %s", option, "is not a valid input. Select help (1) for additional assistance.\n");
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
            return gameList.get(gameNumber).gameID();
        } catch (IndexOutOfBoundsException ex) {
            return 0;
        }
    }
}