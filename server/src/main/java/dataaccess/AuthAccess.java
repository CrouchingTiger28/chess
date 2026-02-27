package dataaccess;

import java.util.ArrayList;
import java.util.Objects;

public class AuthAccess {

    private ArrayList<Model.AuthData> auths = new ArrayList<>();
    public AuthAccess() {

    }

    public void createAuth(Model.AuthData data) {
        auths.add(data);
    }

    public Model.AuthData getAuth(String authToken) {
        for (Model.AuthData currentAuth : auths) {
            if (Objects.equals(currentAuth.authToken(), authToken)) {
                return currentAuth;
            }
        }
        return null;
    }

    public void deleteAuth(String authToken) {
        auths.removeIf(currentAuth -> Objects.equals(currentAuth.authToken(), authToken));
    }

    public void deleteAuths() {
        auths = new ArrayList<>();
    }
}
