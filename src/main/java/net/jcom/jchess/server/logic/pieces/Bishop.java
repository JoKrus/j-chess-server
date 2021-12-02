package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.logic.Color;
import net.jcom.jchess.server.logic.Coordinate;
import net.jcom.jchess.server.logic.Position;

import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece {
    public Bishop(Coordinate coordinate, Color color) {
        super(coordinate, color, PieceType.BISHOP);
    }

    @Override
    protected List<Coordinate> possibleToMoveToUnchecked(Position position) {
        List<Coordinate> ret = new ArrayList<>();

        int x = this.getCoordinate().getX(), y = this.getCoordinate().getY();
        //up left
        checkDir(position, ret, x, y, -1, -1);

        //up right
        checkDir(position, ret, x, y, 1, -1);

        //down left
        checkDir(position, ret, x, y, -1, 1);

        //down right
        checkDir(position, ret, x, y, 1, 1);

        return ret;
    }

    private void checkDir(Position position, List<Coordinate> ret, int x, int y, int xDir, int yDir) {
        while (x > 0 && y > 0 && x < 7 && y < 7) {
            x += xDir;
            y += yDir;
            Coordinate coordinateAt = Coordinate.of(x, y);
            Piece pieceAt = position.getPieceAt(coordinateAt);
            if (pieceAt == null) {
                ret.add(coordinateAt);
            } else if (pieceAt.getColor() != this.getColor()) {
                ret.add(coordinateAt);
                break;
            } else {
                break;
            }
        }
    }
}
