package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.generated.MoveData;
import net.jcom.jchess.server.logic.Color;
import net.jcom.jchess.server.logic.Coordinate;
import net.jcom.jchess.server.logic.Position;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class PieceHelper {
    public static void checkDir(Position position, List<MoveData> ret, int x, int y, int xDir, int yDir,
                                Color myColor) {
        Coordinate baseCoordinate = Coordinate.of(x, y);
        while (x >= 0 && y >= 0 && x <= 7 && y <= 7) {
            x += xDir;
            y += yDir;
            Coordinate coordinateAt = Coordinate.of(x, y);
            if (coordinateAt == null) {
                continue;
            }
            Piece pieceAt = position.getPieceAt(coordinateAt);
            if (pieceAt == null) {
                ret.add(PieceHelper.coordinateToMoveData(baseCoordinate, coordinateAt));
            } else if (pieceAt.getColor() == myColor.enemy()) {
                ret.add(PieceHelper.coordinateToMoveData(baseCoordinate, coordinateAt));
                break;
            } else {
                break;
            }
        }
    }

    public static void checkDiagonals(Position position, List<MoveData> ret, int x, int y, Color myColor) {
        //up left
        PieceHelper.checkDir(position, ret, x, y, -1, -1, myColor);

        //up right
        PieceHelper.checkDir(position, ret, x, y, 1, -1, myColor);

        //down left
        PieceHelper.checkDir(position, ret, x, y, -1, 1, myColor);

        //down right
        PieceHelper.checkDir(position, ret, x, y, 1, 1, myColor);
    }

    public static void checkStraights(Position position, List<MoveData> ret, int x, int y, Color myColor) {
        //up
        PieceHelper.checkDir(position, ret, x, y, 0, -1, myColor);

        //right
        PieceHelper.checkDir(position, ret, x, y, 1, 0, myColor);

        //down
        PieceHelper.checkDir(position, ret, x, y, 0, 1, myColor);

        //left
        PieceHelper.checkDir(position, ret, x, y, -1, 0, myColor);
    }

    //from to check if own piece moved, to to check if piece is taken
    public static String modifyRochadeString(String prevRochadeString, Coordinate from, Coordinate to) {
        String ret = prevRochadeString;

        if (from.equals(Coordinate.parse("a1"))) {
            ret = ret.replace("Q", "");
        } else if (from.equals(Coordinate.parse("a8"))) {
            ret = ret.replace("q", "");
        } else if (from.equals(Coordinate.parse("h1"))) {
            ret = ret.replace("K", "");
        } else if (from.equals(Coordinate.parse("h1"))) {
            ret = ret.replace("k", "");
        } else if (from.equals(Coordinate.parse("e1"))) {
            ret = ret.replace("Q", "").replace("K", "");
        } else if (from.equals(Coordinate.parse("e8"))) {
            ret = ret.replace("q", "").replace("k", "");
        }

        if (to.equals(Coordinate.parse("a1"))) {
            ret = ret.replace("Q", "");
        } else if (to.equals(Coordinate.parse("a8"))) {
            ret = ret.replace("q", "");
        } else if (to.equals(Coordinate.parse("h1"))) {
            ret = ret.replace("K", "");
        } else if (to.equals(Coordinate.parse("h1"))) {
            ret = ret.replace("k", "");
        }

        if (ret.isEmpty()) {
            ret = "-";
        }

        return ret;
    }

    public static Pair<Piece, Coordinate> getRochadeRook(Position position, Coordinate from, Coordinate to) {
        return Pair.of(position.getPieceAt(Coordinate.of(to.getX() == 2 ? 0 : 7, to.getY())),
                Coordinate.of(to.getX() == 2 ? 3 : 5, to.getY()));
    }

    public static MoveData coordinateToMoveData(Coordinate from, Coordinate to) {
        return coordinateToMoveData(from, to, null);
    }

    public static MoveData coordinateToMoveData(Coordinate from, Coordinate to, String promotion) {
        MoveData ret = new MoveData();
        ret.setFrom(from.toString());
        ret.setTo(to.toString());
        ret.setPromotionUnit(promotion);
        return ret;
    }
}
