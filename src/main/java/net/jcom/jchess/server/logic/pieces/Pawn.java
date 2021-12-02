package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.logic.Color;
import net.jcom.jchess.server.logic.Coordinate;
import net.jcom.jchess.server.logic.Position;

import java.util.List;

public class Pawn extends Piece {
    public Pawn(Coordinate coordinate, Color color) {
        super(coordinate, color, PieceType.PAWN);
    }

    @Override
    protected List<Coordinate> possibleToMoveToUnchecked(Position position) {
        return null;
    }
}
