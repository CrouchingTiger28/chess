package chess;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final PieceType type;
    private final ChessGame.TeamColor color;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.type = type;
        this.color = pieceColor;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        PieceMovesCalculator calculator = new PieceMovesCalculator(board, myPosition);
        return calculator.pieceMoves();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return type == that.type && color == that.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, color);
    }

    public String toString() {
        if (color == ChessGame.TeamColor.WHITE) {
            return switch (type) {
                case PieceType.PAWN -> "P";
                case PieceType.KNIGHT -> "N";
                case PieceType.BISHOP -> "B";
                case PieceType.ROOK -> "R";
                case PieceType.QUEEN -> "Q";
                case PieceType.KING -> "K";
            };
        } else if (color == ChessGame.TeamColor.BLACK){
            return switch (type) {
                case PieceType.PAWN -> "p";
                case PieceType.KNIGHT -> "n";
                case PieceType.BISHOP -> "b";
                case PieceType.ROOK -> "r";
                case PieceType.QUEEN -> "q";
                case PieceType.KING -> "k";
            };
        }
        return " ";
    }
}
