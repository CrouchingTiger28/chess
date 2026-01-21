package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PieceMovesCalculator {

    private final ChessBoard board;
    private final ChessPosition position;
    private final ChessPiece.PieceType type;
    private final List<ChessMove> moves;

    public PieceMovesCalculator(ChessBoard board, ChessPosition position) {
        this.board = board;
        this.position = position;
        this.type = board.getPiece(position).getPieceType();
        moves = new ArrayList<>();
    }

    public Collection<ChessMove> pieceMoves() {
        if (type == ChessPiece.PieceType.BISHOP) {
            bishopMoves();
        } else if (type == ChessPiece.PieceType.KING) {
            kingMoves();
        } else if (type == ChessPiece.PieceType.KNIGHT) {
            knightMoves();
        } else if (type == ChessPiece.PieceType.PAWN) {
            pawnMoves();
        } else if (type == ChessPiece.PieceType.QUEEN) {
            rookMoves();
            bishopMoves();
        } else if (type == ChessPiece.PieceType.ROOK) {
            rookMoves();
        }
        return moves;
    }

    private Boolean continueSearch(int y, int x) {
        if (y < 1 || y > 8 || x < 1 || x > 8) {
            return false;
        }
        ChessPosition target = new ChessPosition(y, x);
        if (board.getPiece(target) != null) {
            if (board.getPiece(position).getTeamColor() != board.getPiece(target).getTeamColor()) {
                moves.add(new ChessMove(position, target, null));
            }
            return false;
        } else {
            moves.add(new ChessMove(position, target, null));
            return true;
        }
    }

    private void addPromotionTargets(ChessPosition target) {
        moves.add(new ChessMove(position, target, ChessPiece.PieceType.KNIGHT));
        moves.add(new ChessMove(position, target, ChessPiece.PieceType.ROOK));
        moves.add(new ChessMove(position, target, ChessPiece.PieceType.BISHOP));
        moves.add(new ChessMove(position, target, ChessPiece.PieceType.QUEEN));
    }

    private void bishopMoves() {
        boolean keepSearching;
        int i;
        int x;
        int y;

        for (i = 1, keepSearching = true; i < 7 && keepSearching; i++) {
            y = position.getRow() + i;
            x = position.getColumn() + i;
            keepSearching = continueSearch(y, x);
        }

        for (i = 1, keepSearching = true; i < 7 && keepSearching; i++) {
            y = position.getRow() - i;
            x = position.getColumn() + i;
            keepSearching = continueSearch(y, x);
        }

        for (i = 1, keepSearching = true; i < 7 && keepSearching; i++) {
            y = position.getRow() + i;
            x = position.getColumn() - i;
            keepSearching = continueSearch(y, x);
        }

        for (i = 1, keepSearching = true; i < 7 && keepSearching; i++) {
            y = position.getRow() - i;
            x = position.getColumn() - i;
            keepSearching = continueSearch(y, x);
        }
    }

    private void kingMoves() {
        for (int x = position.getColumn() - 1; x <= position.getColumn() + 1; x++) {
            for (int y = position.getRow() - 1; y <= position.getRow() + 1; y ++) {
                ChessPosition target = new ChessPosition(y, x);
                if (!target.equals(position) && (1 <= x && x <= 8) && (1 <= y && y <= 8) &&
                        (board.getPiece(target) == null || board.getPiece(target).getTeamColor() != board.getPiece(position).getTeamColor())) {
                    moves.add(new ChessMove(position, new ChessPosition(y, x), null));
                }
            }
        }
    }

    private void knightMoves() {
        int x = position.getColumn();
        int y = position.getRow();

        continueSearch(y + 2, x + 1);
        continueSearch(y + 2, x - 1);

        continueSearch(y - 2, x + 1);
        continueSearch(y - 2, x - 1);

        continueSearch(y + 1, x + 2);
        continueSearch(y - 1, x + 2);

        continueSearch(y + 1, x - 2);
        continueSearch(y - 1, x - 2);
    }

    private void pawnMoves() {
        int x = position.getColumn();
        int y = position.getRow();

        if (board.getPiece(position).getTeamColor() == ChessGame.TeamColor.WHITE) {
            ChessPosition front = new ChessPosition(y+1, x);
            ChessPosition left = new ChessPosition(y+1, x-1);
            ChessPosition right = new ChessPosition(y+1, x+1);

            if (y+1 == 8) {

                if (board.getPiece(front) == null) {
                    addPromotionTargets(front);
                } if (x-1 >= 1 && board.getPiece(left) != null && board.getPiece(left).getTeamColor() != ChessGame.TeamColor.WHITE) {
                    addPromotionTargets(left);
                } if (x+1 <= 8 && board.getPiece(right) != null && board.getPiece(right).getTeamColor() != ChessGame.TeamColor.WHITE) {
                    addPromotionTargets(right);
                }
            } else {
                if (y == 2 && board.getPiece(front) == null && board.getPiece(new ChessPosition(y+2, x)) == null) {
                    moves.add(new ChessMove(position, new ChessPosition(y+2, x), null));
                } if (board.getPiece(front) == null) {
                    moves.add(new ChessMove(position, front, null));
                } if (x-1 >= 1 && board.getPiece(left) != null && board.getPiece(left).getTeamColor() != ChessGame.TeamColor.WHITE) {
                    moves.add(new ChessMove(position, left, null));
                } if (x+1 <= 8 && board.getPiece(right) != null && board.getPiece(right).getTeamColor() != ChessGame.TeamColor.WHITE) {
                    moves.add(new ChessMove(position, right, null));
                }
            }

        }
        else {
            ChessPosition front = new ChessPosition(y-1, x);
            ChessPosition left = new ChessPosition(y-1, x-1);
            ChessPosition right = new ChessPosition(y-1, x+1);

            if (y-1 == 1) {

                if (board.getPiece(front) == null) {
                    addPromotionTargets(front);
                } if (x-1 >= 1 && board.getPiece(left) != null && board.getPiece(left).getTeamColor() != ChessGame.TeamColor.BLACK) {
                    addPromotionTargets(left);
                } if (x+1 <= 8 && board.getPiece(right) != null && board.getPiece(right).getTeamColor() != ChessGame.TeamColor.BLACK) {
                    addPromotionTargets(right);
                }
            } else {
                if (y == 7 && board.getPiece(front) == null && board.getPiece(new ChessPosition(y-2, x)) == null) {
                    moves.add(new ChessMove(position, new ChessPosition(y-2, x), null));
                } if (board.getPiece(front) == null) {
                    moves.add(new ChessMove(position, front, null));
                } if (x-1 >= 1 && board.getPiece(left) != null && board.getPiece(left).getTeamColor() != ChessGame.TeamColor.BLACK) {
                    moves.add(new ChessMove(position, left, null));
                } if (x+1 <= 8 && board.getPiece(right) != null && board.getPiece(right).getTeamColor() != ChessGame.TeamColor.BLACK) {
                    moves.add(new ChessMove(position, right, null));
                }
            }

        }
    }

    private void rookMoves() {
        int x = position.getColumn();
        int y;
        boolean keepSearching;

        for (y = position.getRow() - 1, keepSearching = true; y >= 1 && keepSearching; y --) {
            keepSearching = continueSearch(y, x);
        }

        for (y = position.getRow() + 1, keepSearching = true; y <= 8 && keepSearching; y++) {
            keepSearching = continueSearch(y, x);
        }

        y = position.getRow();
        for (x = position.getColumn() - 1, keepSearching = true; x >= 1 && keepSearching; x--) {
            keepSearching = continueSearch(y, x);
        }

        for (x = position.getColumn() + 1, keepSearching = true; x <= 8 && keepSearching; x ++) {
            keepSearching = continueSearch(y, x);
        }
    }
}
