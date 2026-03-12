package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;
import passoff.model.*;
import passoff.server.TestServerFacade;
import server.Server;
import java.sql.SQLException;

public class AuthAccessTest {
    private static TestServerFacade serverFacade;

    private static Server server;
    private static AuthAccess authAccess;
    private final static AuthData TEST_AUTH = new AuthData("This-is-a-test-authtoken", "TestUsername");

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
        authAccess = new AuthAccess();
    }

    @BeforeEach
    public void setup() {
        serverFacade.clear();
    }

    @Test
    @Order(1)
    public void succeedCreateTest() throws DataAccessException, SQLException {
        authAccess.createAuth(TEST_AUTH);

        AuthData auth = authAccess.getAuth(TEST_AUTH.authToken());
        Assertions.assertEquals(TEST_AUTH, auth);
    }

    @Test
    @Order(2)
    public void failedCreateTest() throws DataAccessException, SQLException {
        authAccess.createAuth(TEST_AUTH);
        AuthData auth = new AuthData("This-is-a-test-authtoken", "NewUsername");

        Assertions.assertThrows(SQLException.class, () -> authAccess.createAuth(auth));
    }

    @Test
    @Order(3)
    public void succeedGetTest() throws DataAccessException, SQLException {
        authAccess.createAuth(TEST_AUTH);

        AuthData auth = authAccess.getAuth(TEST_AUTH.authToken());
        Assertions.assertEquals(TEST_AUTH, auth);
    }

    @Test
    @Order(4)
    public void failedGetTest() throws DataAccessException, SQLException {
        authAccess.createAuth(TEST_AUTH);

        Assertions.assertNull(authAccess.getAuth("This-is-just-fake"));
    }

    @Test
    @Order(5)
    public void succeedDeleteTest() throws DataAccessException, SQLException {
        authAccess.createAuth(TEST_AUTH);
        authAccess.deleteAuth(TEST_AUTH.authToken());

        Assertions.assertNull(authAccess.getAuth(TEST_AUTH.authToken()));
    }

    @Test
    @Order(6)
    public void succeedClearTest() throws DataAccessException, SQLException {
        authAccess.createAuth(TEST_AUTH);
        AuthData auth = new AuthData("This-is-a-new-test-authtoken", "NewUsername");
        authAccess.createAuth(auth);

        authAccess.deleteAuthData();
        Assertions.assertNull(authAccess.getAuth(TEST_AUTH.authToken()));
        Assertions.assertNull(authAccess.getAuth(auth.authToken()));
    }
}
