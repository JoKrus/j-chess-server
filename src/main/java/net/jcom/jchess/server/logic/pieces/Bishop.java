package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.generated.MoveData;
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
    public List<MoveData> possibleToMoveToUnchecked(Position position) {
        List<MoveData> ret = new ArrayList<>();

        int x = this.getCoordinate().getX(), y = this.getCoordinate().getY();

        PieceHelper.checkDiagonals(position, ret, x, y, this.getColor());

        return ret;
    }
}
