package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.logic.Color;
import net.jcom.jchess.server.logic.Coordinate;
import net.jcom.jchess.server.logic.Position;

import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece {
    public Queen(Coordinate coordinate, Color color) {
        super(coordinate, color, PieceType.QUEEN);
    }

    @Override
    protected List<Coordinate> possibleToMoveToUnchecked(Position position) {
        List<Coordinate> ret = new ArrayList<>();

        int x = this.getCoordinate().getX(), y = this.getCoordinate().getY();

        PieceHelper.checkStraights(position, ret, x, y, this.getColor());

        PieceHelper.checkDiagonals(position, ret, x, y, this.getColor());

        return ret;
    }
}
