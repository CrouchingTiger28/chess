package client;

import model.*;

import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.*;

public class ClientCommunicator {
    private final HttpClient client = HttpClient.newHttpClient();
    private static String serverURL = "http://localhost:";
    private final Gson gson = new Gson();

    public ClientCommunicator(int port) {
        serverURL += port;
    }

    public void joinGame(String authToken, String playerColor, int id) {
        String reqBody = String.format("{ \"playerColor\":\"%s\", \"gameID\": %d }", playerColor, id);
        makeRequest("PUT", "/game", reqBody, authToken, null);
    }

    public AuthData doPost(String path, String authToken, String... args) {
        String reqBody = switch (path) {
            case "/session" -> String.format("{ \"username\":\"%s\", \"password\":\"%s\" }", args[0], args[1]);
            case "/user" ->
                    String.format("\t{ \"username\":\"%s\", \"password\":\"%s\", \"email\":\"%s\" }", args[0], args[1], args[2]);
            case "/game" -> String.format("{ \"gameName\":\"%s\" }", args[0]);
            default -> null;
        };
        return makeRequest("POST", path, reqBody, authToken, AuthData.class);
    }

    public void doDelete(String path, String authToken) {
        makeRequest("DELETE", path, null, authToken, null);
    }

    public GameList listGames(String path, String authToken) {
        return makeRequest("GET", path, null, authToken, GameList.class);
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
                throw new RuntimeException(Integer.toString(status));
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
