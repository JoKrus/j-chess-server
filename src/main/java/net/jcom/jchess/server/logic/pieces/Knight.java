package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.logic.Color;
import net.jcom.jchess.server.logic.Coordinate;
import net.jcom.jchess.server.logic.Position;

import java.util.List;

public class Knight extends Piece {
    public Knight(Coordinate coordinate, Color color) {
        super(coordinate, color, PieceType.KNIGHT);
    }

    @Override
    protected List<Coordinate> possibleToMoveToUnchecked(Position position) {
        return null;
    }
}
