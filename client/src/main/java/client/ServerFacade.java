package client;

import model.*;


public class ServerFacade {
    private static final ClientCommunicator COMM = new ClientCommunicator();

    public void clearDatabase() {
        try {
            COMM.doDelete("/db", null);
        } catch (RuntimeException ex) {
            System.out.println("Internal server error. Please try again later.");
        }
    }

    public String register(UserData user) {
        try {
            AuthData newAuth = COMM.doPost("/user", null, user.username(), user.password(), user.email());
            if (newAuth != null) {
                return newAuth.authToken();
            } else {
                throw new RuntimeException("something went wrong :(");
            }
        } catch (RuntimeException ex) {
            switch (ex.getMessage()) {
                case "400":
                    System.out.println("Username and Password field need to be filled");
                    break;
                case "403":
                    System.out.println("Username already taken.\n");
                    break;
                default:
                    System.out.println("Internal server error. Please try again later.");
            }
            return null;
        }
    }

    public String login(UserData user) {
        try {
            AuthData newAuth = COMM.doPost("/session", null, user.username(), user.password());
            return newAuth.authToken();
        } catch (RuntimeException ex) {
            switch (ex.getMessage()) {
                case "400":
                    System.out.println("Username and Password field need to be filled.");
                    break;
                case "401":
                    System.out.println("Invalid Username or Password. If you don't have an account, register for a new one.\n");
                    break;
                default:
                    System.out.println("Internal server error. Please try again later.");
            }
            return null;
        }
    }

    public void logout(String authToken) {
        try {
            COMM.doDelete("/session", authToken);
        } catch (RuntimeException ex) {
            if (ex.getMessage().equals("401")) {
                System.out.println("Must be logged in to log out.\n");
            } else {
                System.out.println("Internal server error occurred. Please come back later.");
            }
        }
    }

    public GameList listGames(String authToken) {
        //return a GameList of all the games. Exactly what it says on the tin.
        try {
            return COMM.listGames("/game", authToken);
        } catch (RuntimeException ex) {
            if (ex.getMessage().equals("401")) {
                System.out.println("Please log in to see game list.\n");
            } else {
                System.out.println("Internal server error occurred. Please come back later.");
            }
            return null;
        }
    }

    public void createGame(String authToken, String gameName) {
        try {
            COMM.doPost("/game", authToken, gameName);
        } catch (RuntimeException ex) {
            switch (ex.getMessage()) {
                case "400":
                    System.out.println("Game name must exist to create game.\n");
                    break;
                case "401":
                    System.out.println("Please log in to create a game.\n");
                    break;
                default:
                    System.out.println("Internal server error occurred. Please come back later.");
            }
        }
    }

    public boolean joinGame(String authToken, String playerColor, int gameID) {
        try {
            COMM.joinGame(authToken, playerColor, gameID);
            return true;
        } catch (RuntimeException ex) {
            switch (ex.getMessage()) {
                case "400":
                    System.out.println("Game must exist to join it.\n");
                    break;
                case "401":
                    System.out.println("Please log in to join a game.\n");
                    break;
                case "403":
                    System.out.printf("Oops! There's already a %s player in that game!%n%n", playerColor.toLowerCase());
                    break;
                default:
                    System.out.println("Internal server error occurred. Please come back later.");
            }
            return false;
        }
    }

}
