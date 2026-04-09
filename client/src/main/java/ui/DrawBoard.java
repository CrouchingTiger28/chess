package ui;

import chess.*;
import model.GameData;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class DrawBoard {

    private final PrintStream out = new PrintStream(System.out, true);
    public DrawBoard() {

    }

    public void draw(GameData game, String colorPerspective, ArrayList<ChessPosition> squares, ChessPosition origin) {
        List<String> columns = (colorPerspective.equalsIgnoreCase("white")) ?
                List.of("a", "b", "c", "d", "e", "f", "g", "h") : List.of("h", "g", "f", "e", "d", "c", "b", "a");
        borderText();

        headerAndFooter(columns);

        ChessBoard board = game.game().getBoard();
        boolean reverse = !colorPerspective.equalsIgnoreCase("white");
        drawBoardBlock(board, reverse, squares, origin);

        headerAndFooter(columns);

        out.print(EscapeSequences.RESET_BG_COLOR);
        out.print(EscapeSequences.RESET_TEXT_COLOR);
    }

    public void drawBoardBlock(ChessBoard board, Boolean reverse, ArrayList<ChessPosition> squares, ChessPosition origin) {

        for (int y = 8; y >= 1; y--) {
            borderText();
            int row = (reverse) ? 9-y : y;
            System.out.printf(" %d ", row);

            out.print(EscapeSequences.SET_TEXT_COLOR_BLACK);
            for (int x = 1; x <= 8; x++) {
                int column = (reverse) ? 9-x : x;

                if ((column + row) % 2 != 1) {
                    out.print(EscapeSequences.SET_BG_COLOR_DARK_BROWN);
                    if (squares != null && squares.contains(new ChessPosition(row, column))) {
                        out.print(EscapeSequences.SET_BG_COLOR_DARK_GREEN);
                    } else if (origin != null && origin.equals(new ChessPosition(row, column))) {
                        out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY);
                    }
                } else {
                    out.print(EscapeSequences.SET_BG_COLOR_PALE_BROWN);
                    if (squares != null && squares.contains(new ChessPosition(row, column))) {
                        out.print(EscapeSequences.SET_BG_COLOR_GREEN);
                    }   else if (origin != null && origin.equals(new ChessPosition(row, column))) {
                        out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
                    }
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
