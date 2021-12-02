package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.logic.Color;
import net.jcom.jchess.server.logic.Coordinate;
import net.jcom.jchess.server.logic.Position;

import java.util.List;

public class PieceHelper {
    public static void checkDir(Position position, List<Coordinate> ret, int x, int y, int xDir, int yDir,
                                Color myColor) {
        while (x >= 0 && y >= 0 && x <= 7 && y <= 7) {
            x += xDir;
            y += yDir;
            Coordinate coordinateAt = Coordinate.of(x, y);
            if (coordinateAt == null) {
                continue;
            }
            Piece pieceAt = position.getPieceAt(coordinateAt);
            if (pieceAt == null) {
                ret.add(coordinateAt);
            } else if (pieceAt.getColor() != myColor) {
                ret.add(coordinateAt);
                break;
            } else {
                break;
            }
        }
    }
}
