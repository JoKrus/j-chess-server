package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.logic.Color;
import net.jcom.jchess.server.logic.Coordinate;
import net.jcom.jchess.server.logic.Position;

import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {
    public Rook(Coordinate coordinate, Color color) {
        super(coordinate, color, PieceType.ROOK);
    }

    @Override
    protected List<Coordinate> possibleToMoveToUnchecked(Position position) {
        List<Coordinate> ret = new ArrayList<>();

        int x = this.getCoordinate().getX(), y = this.getCoordinate().getY();
        //up
        PieceHelper.checkDir(position, ret, x, y, 0, -1, this.getColor());

        //right
        PieceHelper.checkDir(position, ret, x, y, 1, 0, this.getColor());

        //down
        PieceHelper.checkDir(position, ret, x, y, 0, 1, this.getColor());

        //left
        PieceHelper.checkDir(position, ret, x, y, -1, 0, this.getColor());

        return ret;
    }
}
