package chess;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.lang.StringBuilder;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    ChessPiece[][] squares = new ChessPiece[8][8];

    public ChessBoard() {

    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getColumn() - 1][position.getRow() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getColumn() - 1][position.getRow() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        squares = new ChessPiece[8][8];
        List<ChessPiece.PieceType> setupOrder;
        setupOrder = List.of(ChessPiece.PieceType.ROOK, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.ROOK);

        int x;
        for (x = 1; x <= 8; x++) {
            addPiece(new ChessPosition(1, x), new ChessPiece(ChessGame.TeamColor.WHITE, setupOrder.get(x - 1)));
            addPiece(new ChessPosition(2, x), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(7, x), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(8, x), new ChessPiece(ChessGame.TeamColor.BLACK, setupOrder.get(x - 1)));
        }
    }

    public ChessBoard deepCopy() {
        ChessBoard result = new ChessBoard();
        for (int x = 1; x<= 8; x++) {
            for (int y = 1; y <= 8; y++) {
                ChessPosition place = new ChessPosition(y, x);
                result.addPiece(place, this.getPiece(place));
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }

    public String toString() {
        StringBuilder rank;
        StringBuilder stack = new StringBuilder();

        for (int y = 1; y <= 8; y++) {
            rank = new StringBuilder();
            for (int x = 1; x <= 8; x++) {
                rank.append("|");
                if (this.getPiece(new ChessPosition(y, x)) != null) {
                    rank.append(this.getPiece(new ChessPosition(y, x)).toString());
                } else {
                    rank.append(" ");
                }
            }
            rank.append("|");

            stack.append(rank);
            stack.append("\n");
        }

        return stack.toString();
    }
}
