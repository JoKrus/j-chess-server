package net.jcom.jchess.server.logic;

import net.jcom.jchess.server.generated.MoveData;
import net.jcom.jchess.server.logic.pieces.*;
import org.apache.commons.lang3.tuple.Pair;

public class Parser {
    public static Pair<PieceType, Color> parsePiece(String s) {
        if (s.length() != 1) return null;
        else return parsePiece(s.charAt(0));
    }

    public static Pair<PieceType, Color> parsePiece(char c) {
        Pair<PieceType, Color> ret = null;
        switch (c) {
            case 'p':
                ret = Pair.of(PieceType.PAWN, Color.BLACK);
                break;
            case 'P':
                ret = Pair.of(PieceType.PAWN, Color.WHITE);
                break;
            case 'b':
                ret = Pair.of(PieceType.BISHOP, Color.BLACK);
                break;
            case 'B':
                ret = Pair.of(PieceType.BISHOP, Color.WHITE);
                break;
            case 'n':
                ret = Pair.of(PieceType.KNIGHT, Color.BLACK);
                break;
            case 'N':
                ret = Pair.of(PieceType.KNIGHT, Color.WHITE);
                break;
            case 'r':
                ret = Pair.of(PieceType.ROOK, Color.BLACK);
                break;
            case 'R':
                ret = Pair.of(PieceType.ROOK, Color.WHITE);
                break;
            case 'q':
                ret = Pair.of(PieceType.QUEEN, Color.BLACK);
                break;
            case 'Q':
                ret = Pair.of(PieceType.QUEEN, Color.WHITE);
                break;
            case 'k':
                ret = Pair.of(PieceType.KING, Color.BLACK);
                break;
            case 'K':
                ret = Pair.of(PieceType.KING, Color.WHITE);
                break;
        }
        return ret;
    }

    public static String parsePiece(Piece piece) {
        String ret = parsePieceType(piece);

        if (ret == null) return null;

        if (piece.getColor() == Color.WHITE) {
            ret = ret.toUpperCase();
        }

        return ret;
    }

    private static String parsePieceType(Piece piece) {
        switch (piece.getPieceType()) {
            case PAWN:
                return "p";
            case BISHOP:
                return "b";
            case KNIGHT:
                return "n";
            case ROOK:
                return "r";
            case QUEEN:
                return "q";
            case KING:
                return "k";
            default:
                return null;
        }
    }

    public static Piece getPiece(Pair<PieceType, Color> pair, Coordinate coordinate) {
        return getPiece(pair.getLeft(), pair.getRight(), coordinate);
    }

    private static Piece getPiece(PieceType pieceType, Color color, Coordinate coordinate) {
        Piece piece = null;
        switch (pieceType) {
            case PAWN:
                piece = new Pawn(coordinate, color);
                break;
            case BISHOP:
                piece = new Bishop(coordinate, color);
                break;
            case KNIGHT:
                piece = new Knight(coordinate, color);
                break;
            case ROOK:
                piece = new Rook(coordinate, color);
                break;
            case QUEEN:
                piece = new Queen(coordinate, color);
                break;
            case KING:
                piece = new King(coordinate, color);
                break;
        }
        return piece;
    }

    public static Piece parsePromotionUnit(String promotionString, Color color) {
        Piece piece;

        if (promotionString == null) {
            promotionString = "";
        }

        promotionString = promotionString.toLowerCase();

        switch (promotionString) {
            case "n":
                piece = new Knight(null, color);
                break;
            case "b":
                piece = new Bishop(null, color);
                break;
            case "r":
                piece = new Rook(null, color);
                break;
            case "q":
            default:
                piece = new Queen(null, color);
                break;
        }

        return piece;
    }

    public static String moveDataToString(MoveData moveData) {
        return moveData.getFrom() + moveData.getTo() +
                (moveData.getPromotionUnit() != null ? moveData.getPromotionUnit() : "");
    }
}
