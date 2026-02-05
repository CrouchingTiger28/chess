package chess;

import jdk.jshell.spi.ExecutionControl;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor colorTurn;
    private ChessBoard board;
    public ChessGame() {
        colorTurn = TeamColor.WHITE;
        board = new ChessBoard();
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return colorTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        colorTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (board.getPiece(move.getStartPosition()).getTeamColor() != colorTurn) {
            throw new InvalidMoveException("Not player's turn");
        }
        if (validMoves(move.getStartPosition()).contains(move)) {
            ChessPiece piece = board.getPiece(move.getStartPosition());
            board.addPiece(move.getStartPosition(), null);
            board.addPiece(move.getEndPosition(), piece);
            colorTurn = (colorTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        } else {
            throw new InvalidMoveException("Move not valid");
        }
    }



    private ChessPosition findKing(TeamColor teamColor) {
        for (int x = 1; x <= 8; x++) {
            for (int y =1; y <= 8; y++) {
                ChessPosition position = new ChessPosition(y, x);
                ChessPiece piece = board.getPiece(position);
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                    return position;
                }
            }
        }
        return null;
    }


    private boolean checkCheck(TeamColor teamColor, ChessBoard myBoard) {
        ChessPosition kingPosition = findKing(teamColor);

        for (int x = 1; x <= 8; x++) {
            for (int y =1; y <= 8; y++) {
                ChessPosition piecePosition = new ChessPosition(y, x);
                ChessPiece myPiece = myBoard.getPiece(piecePosition);
                if (myPiece != null && myPiece.getTeamColor() != teamColor) {
                    Collection<ChessMove> moves = myPiece.pieceMoves(myBoard, piecePosition);
                    if (moves.contains(new ChessMove(piecePosition, kingPosition, null)) ||
                            moves.contains(new ChessMove(piecePosition, kingPosition, ChessPiece.PieceType.QUEEN))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return checkCheck(teamColor, board);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            ChessPosition kingPosition = findKing(teamColor);
            Collection<ChessMove> validKingMoves = new ArrayList<>();
            Collection<ChessMove> invalidKingMoves = new ArrayList<>();

            for (ChessMove move : board.getPiece(kingPosition).pieceMoves(board, kingPosition)) {
                ChessBoard boardCopy = board.deepCopy();
                boardCopy.addPiece(kingPosition, null);
                boardCopy.addPiece(move.getEndPosition(), new ChessPiece(teamColor, ChessPiece.PieceType.KING));
                if (!checkCheck(teamColor, boardCopy)) {
                    validKingMoves.add(move);
                } else {
                    invalidKingMoves.add(move);
                }
            }
            return (validKingMoves.isEmpty());
        } else {
            return false;
        }
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
