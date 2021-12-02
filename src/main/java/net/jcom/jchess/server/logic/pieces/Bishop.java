package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.logic.Color;
import net.jcom.jchess.server.logic.Coordinate;

import java.util.List;

public class Bishop extends Piece {
    public Bishop(Coordinate coordinate, Color color) {
        super(coordinate, color, PieceType.BISHOP);
    }

    @Override
    protected List<Coordinate> possibleToMoveToUnchecked() {
        return null;
    }
}
