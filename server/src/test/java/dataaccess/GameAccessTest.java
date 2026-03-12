package dataaccess;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.GameAccess;
import model.GameData;
import org.junit.jupiter.api.*;
import passoff.model.*;
import passoff.server.TestServerFacade;
import server.Server;

import java.sql.SQLException;
import java.util.ArrayList;

public class GameAccessTest {

    private static TestServerFacade serverFacade;

    private static Server server;
    private static GameAccess gameAccess;
    private final static GameData TEST_GAME = new GameData(0, null, null, "Game1", new chess.ChessGame());

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeAll
    public static void startServer() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on port " + port);

        serverFacade = new TestServerFacade("localhost", Integer.toString(port));
        gameAccess = new GameAccess();
    }

    @BeforeEach
    public void setup() {
        serverFacade.clear();
    }

    @Test
    @Order(1)
    public void succeedCreationTest() throws DataAccessException, SQLException {
        int result = gameAccess.createGame(TEST_GAME);
        Assertions.assertTrue(result > 0, "creation did not return valid gameID");
    }

    @Test
    @Order(2)
    public void failedCreationTest() {
        GameData game = new GameData(0, null, null, null, new ChessGame());

        Assertions.assertThrows(SQLException.class,
                () -> gameAccess.createGame(game),
                "Task failed successfully");
    }

    @Test
    @Order(3)
    public void succeedUpdateTest() throws DataAccessException, SQLException {
        int gameID = gameAccess.createGame(TEST_GAME);
        gameAccess.updateGame(gameID, "white", "DummyUser");
        Assertions.assertEquals("DummyUser", gameAccess.getGame(gameID).whiteUsername());
    }

    @Test
    @Order(4)
    public void failedUpdateTest() throws DataAccessException, SQLException{
        int gameID = gameAccess.createGame(TEST_GAME);

        Assertions.assertThrows(DataAccessException.class,
                () -> gameAccess.updateGame(gameID, "Banana", "CrashTestUser"),
                "Task failed successfully");
    }

    @Test
    @Order(5)
    public void succeedGetTest() throws DataAccessException, SQLException {
        int gameID = gameAccess.createGame(TEST_GAME);
        GameData gotResult = gameAccess.getGame(gameID);
        Assertions.assertEquals("Game1", gotResult.gameName());
    }

    @Test
    @Order(6)
    public void failedGetTest() throws DataAccessException, SQLException{
        gameAccess.createGame(TEST_GAME);

        GameData gotResult = gameAccess.getGame(0);
        Assertions.assertNull(gotResult);

    }

    @Test
    @Order(7)
    public void succeedListTest() throws DataAccessException, SQLException {
        int gameID = gameAccess.createGame(TEST_GAME);
        GameData myGame = new GameData(gameID, TEST_GAME.whiteUsername(), TEST_GAME.blackUsername(), TEST_GAME.gameName(), TEST_GAME.game());
        ArrayList<GameData> games = gameAccess.listGames();


        Assertions.assertEquals(myGame, games.getFirst());
        Assertions.assertEquals(1, games.size());
    }

    @Test
    @Order(8)
    public void succeedDeleteTest() throws DataAccessException, SQLException {
        int gameID = gameAccess.createGame(TEST_GAME);
        gameAccess.deleteGame(gameID);
        ArrayList<GameData> games = gameAccess.listGames();
        GameData game = gameAccess.getGame(gameID);

        Assertions.assertEquals(0, games.size());
        Assertions.assertNull(game);
    }

    @Test
    @Order(9)
    public void succeedClearTest() throws DataAccessException, SQLException {
        int game1ID = gameAccess.createGame(TEST_GAME);
        int game2ID = gameAccess.createGame(new GameData(0, null, null, "Game2", new ChessGame()));
        gameAccess.deleteGameData();
        ArrayList<GameData> games = gameAccess.listGames();
        GameData game1 = gameAccess.getGame(game1ID);
        GameData game2 = gameAccess.getGame(game2ID);

        Assertions.assertEquals(0, games.size());
        Assertions.assertNull(game1);
        Assertions.assertNull(game2);
    }


//    List games, delete game, delete all games
}
