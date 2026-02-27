package dataaccess;

import java.util.ArrayList;
import java.util.Objects;

public class UserAccess {

    private ArrayList<Model.UserData> users = new ArrayList<>();
    public UserAccess() {

    }

    public Model.UserData getUser(String username) {
        for (Model.UserData currentUser : users) {
            if (Objects.equals(currentUser.username(), username)) {
                return currentUser;
            }
        }
        return null;
    }

    public void createUser(Model.UserData data) {
        users.add(data);
    }

    public void deleteUserData() {
        users = new ArrayList<>();
    }
}
