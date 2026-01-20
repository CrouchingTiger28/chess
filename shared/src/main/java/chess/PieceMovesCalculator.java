package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PieceMovesCalculator {

    private final ChessBoard board;
    private final ChessPosition position;
    private final ChessPiece.PieceType type;

    public PieceMovesCalculator(ChessBoard board, ChessPosition position) {
        this.board = board;
        this.position = position;
        this.type = board.getPiece(position).getPieceType();
    }

    public Collection<ChessMove> pieceMoves() {
        List<ChessMove> moves = new ArrayList<>();
        if (type == ChessPiece.PieceType.BISHOP) {
            return bishopMoves(moves);
        } else if (type == ChessPiece.PieceType.KING) {
            return kingMoves(moves);
//        } else if (type == ChessPiece.PieceType.KNIGHT) {
//            return knightMoves(board, position, moves);
//        } else if (type == ChessPiece.PieceType.PAWN) {
//            return pawnMoves(board, position, moves);
//        } else if (type == ChessPiece.PieceType.QUEEN) {
//            moves = rookMoves(board, position, moves);
//            return bishopMoves(board, position, moves);
        } else if (type == ChessPiece.PieceType.ROOK) {
            return rookMoves(moves);
        }
        return List.of();
    }

    private Collection<ChessMove> bishopMoves(List<ChessMove> moves) {
        int x;
        for (int y = 1; y <= position.getRow(); y++) {
            int shift = position.getRow() - y;
            x = position.getColumn() - shift;
            moves.add(new ChessMove(position, new ChessPosition(x, y), null));
        }
        return moves;
    }

    private Collection<ChessMove> kingMoves(List<ChessMove> moves) {
        for (int x = position.getColumn() - 1; x <= position.getColumn() + 1; x++) {
            for (int y = position.getRow() - 1; y <= position.getRow() + 1; y ++) {
                ChessPosition target = new ChessPosition(y, x);
                if (!target.equals(position) && (1 <= x && x <= 8) && (1 <= y && y <= 8) &&
                        (board.getPiece(target) == null || board.getPiece(target).getTeamColor() != board.getPiece(position).getTeamColor())) {
                    moves.add(new ChessMove(position, new ChessPosition(y, x), null));
                }
            }
        }
        return moves;
    }

    private Collection<ChessMove> rookMoves(List<ChessMove> moves) {
        int x = position.getColumn();
        int y;
        ChessPosition target;
        for (y = position.getRow() - 1; y >= 1; y --) {
            target = new ChessPosition(y, x);
            if (board.getPiece(target) != null) {
                if (board.getPiece(position).getTeamColor() != board.getPiece(target).getTeamColor()) {
                    moves.add(new ChessMove(position, new ChessPosition(y, x), null));
                }
                break;
            } else {
                moves.add(new ChessMove(position, new ChessPosition(y, x), null));
            }
        }

        y = position.getRow();
        for (x = position.getColumn() - 1; x >= 1; x--) {
            target = new ChessPosition(y, x);
            if (board.getPiece(target) != null) {
                if (board.getPiece(position).getTeamColor() != board.getPiece(target).getTeamColor()) {
                    moves.add(new ChessMove(position, new ChessPosition(y, x), null));
                }
                break;
            } else {
                moves.add(new ChessMove(position, new ChessPosition(y, x), null));
            }
        }

        x = position.getColumn();
        for (y = position.getRow() + 1; y <= 8; y++) {
            target = new ChessPosition(y, x);
            if (board.getPiece(target) != null) {
                if (board.getPiece(position).getTeamColor() != board.getPiece(target).getTeamColor()) {
                    moves.add(new ChessMove(position, new ChessPosition(y, x), null));
                }
                break;
            } else {
                moves.add(new ChessMove(position, new ChessPosition(y, x), null));
            }
        }

        y = position.getRow();
        for (x = position.getColumn() + 1; x <= 8; x ++) {
            target = new ChessPosition(y, x);
            if (board.getPiece(target) != null) {
                if (board.getPiece(position).getTeamColor() != board.getPiece(target).getTeamColor()) {
                    moves.add(new ChessMove(position, new ChessPosition(y, x), null));
                }
                break;
            } else {
                moves.add(new ChessMove(position, new ChessPosition(y, x), null));
            }
        }

        return moves;
    }
}
