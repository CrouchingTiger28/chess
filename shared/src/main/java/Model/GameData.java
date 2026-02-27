package Model;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, chess.ChessGame game) {
}
