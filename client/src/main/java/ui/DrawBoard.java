package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;

public class DrawBoard {

    private final PrintStream out = new PrintStream(System.out, true);
    public DrawBoard() {

    }

    public void draw(GameData game, String colorPerspective) {
        List<String> columns = (Objects.equals(colorPerspective, "White")) ?
                List.of("a", "b", "c", "d", "e", "f", "g", "h") : List.of("h", "g", "f", "e", "d", "c", "b", "a");
        borderText();

        headerAndFooter(columns);

        ChessBoard board = game.game().getBoard();
        boolean reverse = !Objects.equals(colorPerspective, "White");
        drawBoardBlock(board, reverse);

        headerAndFooter(columns);

        out.print(EscapeSequences.RESET_BG_COLOR);
        out.print(EscapeSequences.RESET_TEXT_COLOR);
    }

    public void drawBoardBlock(ChessBoard board, Boolean reverse) {
        for (int y = 8; y >= 1; y--) {
            borderText();
            int row = (reverse) ? 9-y : y;
            System.out.printf(" %d ", row);

            out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
            for (int x = 1; x <= 8; x++) {
                int column = (reverse) ? 9-x : x;

                if ((column + row) % 2 != 1) {
                    out.print(EscapeSequences.SET_BG_COLOR_DARK_BROWN);
                } else {
                    out.print(EscapeSequences.SET_BG_COLOR_PALE_BROWN);
                }
                ChessPiece piece = board.getPiece(new ChessPosition(row, column));
                System.out.print(getSymbol(piece));

            }
            borderText();
            System.out.printf(" %d ", row);

            out.print(EscapeSequences.RESET_BG_COLOR);
            System.out.println();
        }
    }

    public void drawWhite(GameData game) {
        draw(game, "White");
    }

    public void drawBlack(GameData game) {
        draw(game, "Black");
    }

    private void borderText() {
        out.print(EscapeSequences.SET_BG_COLOR_BORDER_BROWN);
        out.print(EscapeSequences.SET_TEXT_COLOR_WHITE);
    }

    private void headerAndFooter(List<String> values) {
        borderText();

        System.out.print("   ");

        for (String value : values) {
            out.print(EscapeSequences.SMALL_EMPTY);
            System.out.printf("%s ", value);
        }
        System.out.print("   ");

        out.print(EscapeSequences.RESET_BG_COLOR);
        System.out.print("\n");
    }

    private String getSymbol(ChessPiece piece) {
        if (piece == null) {
            return EscapeSequences.EMPTY;
        } else {
            chess.ChessGame.TeamColor color = piece.getTeamColor();
            ChessPiece.PieceType type = piece.getPieceType();

            if (color == ChessGame.TeamColor.WHITE) {
                return switch (type) {
                    case ChessPiece.PieceType.PAWN -> EscapeSequences.WHITE_PAWN;
                    case ChessPiece.PieceType.KNIGHT -> EscapeSequences.WHITE_KNIGHT;
                    case ChessPiece.PieceType.BISHOP -> EscapeSequences.WHITE_BISHOP;
                    case ChessPiece.PieceType.ROOK -> EscapeSequences.WHITE_ROOK;
                    case ChessPiece.PieceType.QUEEN -> EscapeSequences.WHITE_QUEEN;
                    case ChessPiece.PieceType.KING -> EscapeSequences.WHITE_KING;
                };
            } else if (color == ChessGame.TeamColor.BLACK){
                return switch (type) {
                    case ChessPiece.PieceType.PAWN -> EscapeSequences.BLACK_PAWN;
                    case ChessPiece.PieceType.KNIGHT -> EscapeSequences.BLACK_KNIGHT;
                    case ChessPiece.PieceType.BISHOP -> EscapeSequences.BLACK_BISHOP;
                    case ChessPiece.PieceType.ROOK -> EscapeSequences.BLACK_ROOK;
                    case ChessPiece.PieceType.QUEEN -> EscapeSequences.BLACK_QUEEN;
                    case ChessPiece.PieceType.KING -> EscapeSequences.BLACK_KING;
                };
            }
            return EscapeSequences.EMPTY;
        }
    }
}
