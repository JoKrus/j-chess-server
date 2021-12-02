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
        PieceHelper.checkDir(position, ret, x, y, -1, -1, this.getColor());

        //up right
        PieceHelper.checkDir(position, ret, x, y, 1, -1, this.getColor());

        //down left
        PieceHelper.checkDir(position, ret, x, y, -1, 1, this.getColor());

        //down right
        PieceHelper.checkDir(position, ret, x, y, 1, 1, this.getColor());

        return ret;
    }
}
