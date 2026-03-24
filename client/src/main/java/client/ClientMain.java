package client;

import chess.*;
import model.GameData;
import model.GameList;
import model.UserData;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class ClientMain {

    private boolean loggedIn = false;
    private String authToken = null;
    private final ui.DrawBoard boardPen = new ui.DrawBoard();
    private GameList gameList = null;
    private static final ServerFacade SERVER_FACADE = new ServerFacade();
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);
        ClientMain self = new ClientMain();
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
                System.out.printf("Invalid input. Please try again.%nInput must be a whole number between 1 and %d.%n", numOfOptions);
                scanner.next();
            }
        }

    }

    private void menu() {
        int firstChoice;
        int secondChoice;

        while (true) {

            if (!loggedIn) {
                firstChoice = repl(List.of("login", "register", "help", "quit", "clear"), 5);
                if (!preloginMenuItem(firstChoice)) {
                    break;
                }
            } else {
                secondChoice = repl(List.of("help", "logout", "create game", "list games", "join game", "observe game"), 6);
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

    private void clear() {
        scanner.nextLine();
        String response;
        List<String> affirmative = List.of("yes", "YES", "Yes", "y", "Y");

        System.out.print("Are you sure you want to clear the database? (yes/no)\n");
        response = scanner.next();

        if (affirmative.contains(response)) {
            SERVER_FACADE.clearDatabase();
            System.out.println("Clearing database...");
            authToken = null;
        }
    }

    private boolean register() {
        scanner.nextLine();
        String username;
        String password1 = null;
        String password2 = "other null";
        String email;


        System.out.print("Please create a username: \n");
        username = scanner.nextLine();

        while (!Objects.equals(password1, password2)) {
            System.out.print("Please create a password: \n");
            password1 = scanner.nextLine();

            System.out.print("Please repeat your password: \n");
            password2 = scanner.nextLine();

            if (!Objects.equals(password1, password2)) {
                System.out.println("Passwords don't match!");
            }
        }

        System.out.print("Please input email: \n");
        email = scanner.nextLine();

        authToken = SERVER_FACADE.register(new UserData(username, password1, email));

        if (authToken != null) {
            System.out.println("Registering you...\n");
            return true;
        } else {
            return false;
        }

    }

    private boolean login() {
        scanner.nextLine();
        String username;
        String password;


        System.out.print("Please input username: \n");
        username = scanner.nextLine();

        System.out.print("Please input password: \n");
        password = scanner.nextLine();

        authToken = SERVER_FACADE.login(new UserData(username, password, null));

        if (authToken != null) {
            System.out.println("Logging you in...\n");
            return true;

        } else {
            return false;
        }
    }

    private boolean logout() {
        scanner.nextLine();
        String response;
        List<String> affirmative = List.of("yes", "YES", "Yes", "y", "Y");
        List<String> negative = List.of("no", "NO", "No", "n", "N");

        while (authToken != null) {
            System.out.print("Are you sure you want to log out? (yes/no)\n");
            response = scanner.next();

            if (affirmative.contains(response)) {
                SERVER_FACADE.logout(authToken);
                authToken = null;
            } else if (negative.contains(response)) {
                return false;
            }
        }
        System.out.println("Logging you out...\n");
        return true;
    }

    private void listGames() {
        updateGameList();
        if (gameList == null || gameList.games().isEmpty()) {
            System.out.println("There aren't any games right now. Create a new one by selecting 'create game' (3).");
        } else {
            String whitePlayer;
            String blackPlayer;
            for (int i = 0; i < gameList.games().size(); i++) {
                GameData game = gameList.games().get(i);
                whitePlayer = (game.whiteUsername() == null) ? "none" : game.whiteUsername();
                blackPlayer = (game.blackUsername() == null) ? "none" : game.blackUsername();
                System.out.printf("%d: %s. %s: %s, %s: %s%n", i + 1, game.gameName(), "White Player", whitePlayer, "Black Player", blackPlayer);
            }
            System.out.println();
        }
    }

    private void createGame() {
        scanner.nextLine();
        String gameName;
        GameData myGame = null;


        System.out.print("Game Name: \n");
        gameName = scanner.nextLine();

        SERVER_FACADE.createGame(authToken, gameName);

        updateGameList();

        for (GameData game : gameList.games()) {
            if (gameName.equals(game.gameName())) {
                myGame = game;
                break;
            }
        }

        if (myGame != null) {
            int index = gameList.games().indexOf(myGame);
            System.out.println("Creating game...");
            System.out.printf("Your game, %s, is number %d on the list!%n%n", gameName, index+1);
        }

    }

    private void joinGame() {
        updateGameList();
        if (gameList.games().isEmpty()) {
            System.out.println("There aren't any games yet!\nCreate a game to play.");
            return;
        }
        System.out.println("What game would you like to join?");
        listGames();
        int gameNumber = repl(List.of(), gameList.games().size());

        System.out.println("What color do you want to join as?");
        int color = repl(List.of("White", "Black"), 2);

        String colorName = (color == 1) ? "WHITE" : "BLACK";
        GameData game = getGame(gameNumber);

        int gameID = (game != null)? game.gameID() : null;

        if (SERVER_FACADE.joinGame(authToken, colorName, gameID)) {
            System.out.printf("Alright, joining game %d as %s...%n", gameNumber, colorName.toLowerCase());
            drawBoard(colorName, game);
        }
    }

    private void observeGame() {
        updateGameList();
        if (gameList.games().isEmpty()) {
            System.out.println("There aren't any games yet!\nCreate a game to play.");
            return;
        }

        System.out.println("What game would you like to observe?");
        listGames();
        int gameNumber = repl(List.of(), gameList.games().size());

        GameData game = getGame(gameNumber);

        System.out.printf("Alright, observing game %d from white's perspective...%n", gameNumber);
        drawBoard("White", game);
    }

    private boolean preloginMenuItem(int option) {
        return switch (option) {
            case 1 -> {
                loggedIn = login();
                yield true;
            }
            case 2 -> {
                //register
                loggedIn = register();
                yield true;
            }
            case 3 -> {
                printHelp();
                yield true;
            }
            case 4 ->
                //quit
                    false;
            case 5 -> {
                //clear
                clear();
                yield true;
            }
            default -> {
                System.out.printf("%d %s", option, "is not a valid input. Select help (3) for additional assistance.\n");
                yield true;
            }
        };
    }

    private void postloginMenuItem(int option) {
        switch (option) {
            case 1:
                printHelp();
                break;
            case 2:
                //logout
                loggedIn = !logout();
                break;
            case 3:
                //create game
                createGame();
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
                observeGame();
                break;
            default:
                System.out.printf("%d %s", option, "is not a valid input. Select help (1) for additional assistance.\n");
        }
    }

    private void updateGameList() {
        gameList = SERVER_FACADE.listGames(authToken);
    }

    private GameData getGame(int gameNumber) {
        try {
            return gameList.games().get(gameNumber - 1);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }

    private void drawBoard(String color, GameData game) {
        if (color.equalsIgnoreCase("WHITE")) {
            boardPen.drawWhite(game);
        } else {
            boardPen.drawBlack(game);
        }
    }
}