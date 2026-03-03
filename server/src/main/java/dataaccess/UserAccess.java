package dataaccess;

import java.util.ArrayList;
import java.util.Objects;

public class UserAccess {

    private static ArrayList<model.UserData> users = new ArrayList<>();
    public UserAccess() {

    }

    public model.UserData getUser(String username) {
        for (model.UserData currentUser : users) {
            if (Objects.equals(currentUser.username(), username)) {
                return currentUser;
            }
        }
        return null;
    }

    public void createUser(model.UserData data) {
        users.add(data);
    }

    public void deleteUserData() {
        users = new ArrayList<>();
    }
}
