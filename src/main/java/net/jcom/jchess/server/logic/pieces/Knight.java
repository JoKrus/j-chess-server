package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.logic.Color;
import net.jcom.jchess.server.logic.Coordinate;
import net.jcom.jchess.server.logic.Position;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {
    public Knight(Coordinate coordinate, Color color) {
        super(coordinate, color, PieceType.KNIGHT);
    }

    @Override
    protected List<Coordinate> possibleToMoveToUnchecked(Position position) {
        List<Coordinate> ret = new ArrayList<>();

        int x = this.getCoordinate().getX(), y = this.getCoordinate().getY();
        //up left
        checkSquare(position, ret, x - 1, y - 2);
        checkSquare(position, ret, x - 1, y + 2);
        checkSquare(position, ret, x - 2, y - 1);
        checkSquare(position, ret, x - 2, y + 1);
        checkSquare(position, ret, x + 1, y - 2);
        checkSquare(position, ret, x + 1, y + 2);
        checkSquare(position, ret, x + 2, y - 1);
        checkSquare(position, ret, x + 2, y + 1);

        return ret;
    }

    private void checkSquare(Position position, List<Coordinate> ret, int newX, int newY) {
        Coordinate coordinate = Coordinate.of(newX, newY);
        if (coordinate == null) {
            return;
        }
        Piece pieceAt = position.getPieceAt(coordinate);
        if (pieceAt == null || pieceAt.getColor() != getColor()) {
            ret.add(coordinate);
        }
    }
}
