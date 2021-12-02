package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.logic.Color;
import net.jcom.jchess.server.logic.Coordinate;

import java.util.List;

public class King extends Piece {
    public King(Coordinate coordinate, Color color) {
        super(coordinate, color, PieceType.KING);
    }

    @Override
    protected List<Coordinate> possibleToMoveToUnchecked() {
        return null;
    }
}
