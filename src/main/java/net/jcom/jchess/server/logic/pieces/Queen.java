package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.logic.Color;
import net.jcom.jchess.server.logic.Coordinate;
import net.jcom.jchess.server.logic.Position;

import java.util.List;

public class Queen extends Piece {
    public Queen(Coordinate coordinate, Color color) {
        super(coordinate, color, PieceType.QUEEN);
    }

    @Override
    protected List<Coordinate> possibleToMoveToUnchecked(Position position) {
        return null;
    }
}
