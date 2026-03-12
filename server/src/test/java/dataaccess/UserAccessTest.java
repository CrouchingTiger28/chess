package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;
import passoff.model.*;
import passoff.server.TestServerFacade;
import server.Server;
import java.sql.SQLException;

public class UserAccessTest {
    private static TestServerFacade serverFacade;

    private static Server server;
    private static UserAccess userAccess;
    private final static UserData TEST_USER = new UserData("TestUsername", "TestPassword", "Test@Email");

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
        userAccess = new UserAccess();
    }

    @BeforeEach
    public void setup() {
        serverFacade.clear();
    }

    @Test
    @Order(3)
    public void succeedCreateTest() throws DataAccessException, SQLException {
        userAccess.createUser(TEST_USER);

        UserData user = userAccess.getUser(TEST_USER.username());
        Assertions.assertEquals(TEST_USER, user);
    }

    @Test
    @Order(4)
    public void failedCreateTest() throws DataAccessException, SQLException {
        userAccess.createUser(TEST_USER);
        UserData user = new UserData("TestUsername", "NewPassword", "new@Email");

        Assertions.assertThrows(SQLException.class, () -> userAccess.createUser(user));
    }

    @Test
    @Order(1)
    public void succeedGetTest() throws DataAccessException, SQLException {
        userAccess.createUser(TEST_USER);

        UserData user = userAccess.getUser(TEST_USER.username());
        Assertions.assertEquals(TEST_USER, user);
    }

    @Test
    @Order(2)
    public void failedGetTest() throws DataAccessException, SQLException {
        userAccess.createUser(TEST_USER);

        Assertions.assertNull(userAccess.getUser("Banana"));
    }

    @Test
    @Order(5)
    public void succeedClearTest() throws DataAccessException, SQLException {
        userAccess.createUser(TEST_USER);
        UserData user = new UserData("NewUsername", "NewPassword", "New@Email");
        userAccess.createUser(user);

        userAccess.deleteUserData();
        Assertions.assertNull(userAccess.getUser("TestUsername"));
        Assertions.assertNull(userAccess.getUser("NewUsername"));
    }

}
