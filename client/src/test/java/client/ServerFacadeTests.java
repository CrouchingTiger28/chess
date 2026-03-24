package client;

import dataaccess.*;
import dataaccess.DataAccessException;
import model.*;
import org.junit.jupiter.api.*;
import server.Server;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;
    private final static UserData TEST_USER = new UserData("TestUsername", "TestPassword", "Test@Email");
    private final static UserAccess USER_ACCESS = new UserAccess();
    private final static AuthAccess AUTH_ACCESS = new AuthAccess();
    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();


    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

        serverFacade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void setup() {
        serverFacade.clearDatabase();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    @Order(1)
    public void succeedRegisterTest() throws DataAccessException, SQLException {
        serverFacade.register(TEST_USER);

        UserData user = USER_ACCESS.getUser(TEST_USER.username());
        Assertions.assertEquals(TEST_USER.username(), user.username());
    }

    @Test
    @Order(2)
    public void failedRegisterTest() {
        serverFacade.register(TEST_USER);
        UserData user = new UserData("TestUsername", "NewPassword", "new@Email");
        System.setOut(new PrintStream(outContent));

        serverFacade.register(user);

        String output = outContent.toString();
        Assertions.assertEquals("Username already taken.\n" + System.lineSeparator(), output);
    }

    @Test
    @Order(3)
    public void succeedLogoutTest() throws DataAccessException, SQLException {
        String authToken = serverFacade.register(TEST_USER);
        serverFacade.logout(authToken);

        Assertions.assertNull(AUTH_ACCESS.getAuth(authToken));
    }

    @Test
    @Order(4)
    public void failedLogoutTest() {
        System.setOut(new PrintStream(outContent));

        serverFacade.logout("String");

        String output = outContent.toString();
        Assertions.assertEquals("Must be logged in to log out.\n" + System.lineSeparator(), output);
        }

    @Test
    @Order(5)
    public void succeedLoginTest() throws DataAccessException, SQLException {
        serverFacade.register(TEST_USER);

        UserData user = USER_ACCESS.getUser(TEST_USER.username());
        Assertions.assertEquals(TEST_USER.username(), user.username());
    }

    @Test
    @Order(6)
    public void failedLoginTest() {
        String authToken = serverFacade.register(TEST_USER);
        serverFacade.logout(authToken);
        UserData user = new UserData("TestUsername", "NewPassword", "new@Email");

        System.setOut(new PrintStream(outContent));

        serverFacade.login(user);

        String output = outContent.toString();
        Assertions.assertEquals("Invalid Username or Password. " +
                "If you don't have an account, register for a new one.\n" + System.lineSeparator(), output);

    }

    @Test
    @Order(7)
    public void succeedListGameTest() {
        String authToken = serverFacade.register(TEST_USER);
        GameList list = serverFacade.listGames(authToken);

        Assertions.assertEquals(new GameList(new ArrayList<>()), list);
    }

    @Test
    @Order(8)
    public void failedListGameTest() {
        serverFacade.register(TEST_USER);

        System.setOut(new PrintStream(outContent));

        serverFacade.listGames("authToken");

        String output = outContent.toString();

        Assertions.assertEquals("Please log in to see game list.\n", output);
    }

    @Test
    @Order(9)
    public void succeedCreateGameTest() {
        String authToken = serverFacade.register(TEST_USER);
        serverFacade.createGame(authToken, "TestGame");

        GameList games = serverFacade.listGames(authToken);
        Assertions.assertEquals("TestGame", games.games().getFirst().gameName());
    }

    @Test
    @Order(10)
    public void failedCreateGameTest() {
        System.setOut(new PrintStream(outContent));

        serverFacade.createGame("authToken", "Game");

        String output = outContent.toString();

        Assertions.assertEquals("Please log in to create a game.\n", output);
    }

    @Test
    @Order(11)
    public void succeedJoinGameTest() {
        String authToken = serverFacade.register(TEST_USER);
        serverFacade.createGame(authToken, "TestGame");

        serverFacade.joinGame(authToken, "WHITE", 1);
        GameList games = serverFacade.listGames(authToken);
        Assertions.assertEquals(TEST_USER.username(), games.games().getFirst().whiteUsername());
    }

    @Test
    @Order(12)
    public void failedJoinGameTest() {
        String authToken = serverFacade.register(TEST_USER);

        System.setOut(new PrintStream(outContent));
        serverFacade.joinGame(authToken, "WHITE", 0);
        String output = outContent.toString();

        Assertions.assertEquals("Game must exist to join it.\n", output);

    }
}
