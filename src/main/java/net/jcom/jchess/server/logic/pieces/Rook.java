package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.logic.Color;
import net.jcom.jchess.server.logic.Coordinate;

import java.util.List;

public class Rook extends Piece {
    public Rook(Coordinate coordinate, Color color) {
        super(coordinate, color, PieceType.ROOK);
    }

    @Override
    protected List<Coordinate> possibleToMoveToUnchecked() {
        return null;
    }
}
