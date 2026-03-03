package dataaccess;

import java.util.ArrayList;
import java.util.Objects;

public class AuthAccess {

    private static ArrayList<model.AuthData> auths = new ArrayList<>();
    public AuthAccess() {

    }

    public void createAuth(model.AuthData data) {
        auths.add(data);
    }

    public model.AuthData getAuth(String authToken) {
        for (model.AuthData currentAuth : auths) {
            if (Objects.equals(currentAuth.authToken(), authToken)) {
                return currentAuth;
            }
        }
        return null;
    }

    public void deleteAuth(String authToken) {
        auths.removeIf(currentAuth -> Objects.equals(currentAuth.authToken(), authToken));
    }

    public void deleteAuthData() {
        auths = new ArrayList<>();
    }
}
