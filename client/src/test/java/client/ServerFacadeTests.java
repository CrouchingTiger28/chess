package client;

import dataaccess.AuthAccess;
import dataaccess.DataAccessException;
import dataaccess.UserAccess;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;
    private final static UserData TEST_USER = new UserData("TestUsername", "TestPassword", "Test@Email");
    private final static UserAccess userAccess = new UserAccess();
    private final static AuthAccess authAccess = new AuthAccess();
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

        UserData user = userAccess.getUser(TEST_USER.username());
        Assertions.assertEquals(TEST_USER.username(), user.username());
    }

    @Test
    @Order(2)
    public void failedRegisterTest() throws DataAccessException, SQLException {
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

        Assertions.assertNull(authAccess.getAuth(authToken));
    }

    @Test
    @Order(4)
    public void failedLogoutTest() throws DataAccessException, SQLException {
        System.setOut(new PrintStream(outContent));

        serverFacade.logout("String");

        String output = outContent.toString();
        Assertions.assertEquals("Must be logged in to log out.\n" + System.lineSeparator(), output);
        }

    @Test
    @Order(5)
    public void succeedLoginTest() throws DataAccessException, SQLException {
        serverFacade.register(TEST_USER);

        UserData user = userAccess.getUser(TEST_USER.username());
        Assertions.assertEquals(TEST_USER.username(), user.username());
    }

    @Test
    @Order(6)
    public void failedLoginTest() throws DataAccessException, SQLException {
        String authToken = serverFacade.register(TEST_USER);
        serverFacade.logout(authToken);
        UserData user = new UserData("TestUsername", "NewPassword", "new@Email");

        System.setOut(new PrintStream(outContent));

        serverFacade.login(user);

        String output = outContent.toString();
        Assertions.assertEquals("Invalid Username or Password. If you don't have an account, register for a new one.\n" + System.lineSeparator(), output);

    }

    @Test
    @Order(7)
    public void succeedCreateGameTest() {

    }
}
