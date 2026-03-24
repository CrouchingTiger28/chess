package client;

import chess.ChessGame;
import model.*;

import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

public class ClientCommunicator {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverURL = "http://localhost:8080";
    private final Gson gson = new Gson();

    public ClientCommunicator() {

    }

    public AuthData doPost(String path, String Authtoken, String... args) {
        String reqBody;
        switch (path) {
            case "/session":
                reqBody = String.format("{ \"username\":\"%s\", \"password\":\"%s\" }", args[0], args[1]);
                break;
            case "/user":
                reqBody = String.format("\t{ \"username\":\"%s\", \"password\":\"%s\", \"email\":\"%s\" }", args[0], args[1], args[2]);
                break;
            default:
                reqBody = null;
        }
        return makeRequest("POST", path, reqBody, null, AuthData.class);
    }

    public void doDelete(String path, String authToken) {
        makeRequest("DELETE", path, null, authToken, null);
    }

    public GameList listGames() {
        return new GameList(new ArrayList<>(List.of(new GameData(37, null,
                "BirdCowboy", "Our Game", new ChessGame()))));
    }

    private <T> T makeRequest(String method, String path, String request, String authtoken, Class<T> responseClass) throws RuntimeException {
        try {
            URI uri = (new URI(serverURL + path));
            HttpRequest.BodyPublisher bodyPublisher;
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
                bodyPublisher = HttpRequest.BodyPublishers.ofString(request != null ? request : "");
            } else {
                bodyPublisher = HttpRequest.BodyPublishers.noBody();
            }

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json");

            if (authtoken != null ) {
                builder.header("Authorization", "Bearer " + authtoken);
            }
            HttpRequest httpRequest = builder.method(method, bodyPublisher).build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();

            if (status / 100 != 2) {
                throw new RuntimeException("HTTP " + status + " : " + response.body());
            }

            if (responseClass == null) {
                return null;
            }

            return gson.fromJson(response.body(), responseClass);

        } catch (URISyntaxException | MalformedURLException ex) {
            throw new RuntimeException("URL issue");
        } catch (java.io.IOException | java.lang.InterruptedException ex) {
            throw new RuntimeException("server error");
        }
    }
}
