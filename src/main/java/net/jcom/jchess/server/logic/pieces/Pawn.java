package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.logic.Color;
import net.jcom.jchess.server.logic.Coordinate;
import net.jcom.jchess.server.logic.Position;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {
    public Pawn(Coordinate coordinate, Color color) {
        super(coordinate, color, PieceType.PAWN);
    }

    @Override
    protected List<Coordinate> possibleToMoveToUnchecked(Position position) {
        List<Coordinate> ret = new ArrayList<>();

        int x = this.getCoordinate().getX(), y = this.getCoordinate().getY();

        int yDir = this.getColor().equals(Color.WHITE) ? -1 : 1;
        int baseLine = this.getColor().equals(Color.WHITE) ? 6 : 1;

        var pushOne = Coordinate.of(x, y + yDir);
        var pushTwo = Coordinate.of(x, y + yDir * 2);
        var takeLowX = Coordinate.of(x - 1, y + yDir);
        var takeHighX = Coordinate.of(x + 1, y + yDir);

        if (pushOne != null && position.getPieceAt(pushOne) == null) {
            ret.add(pushOne);
        }

        if (pushTwo != null && y == baseLine && position.getPieceAt(pushOne) == null && position.getPieceAt(pushTwo) == null) {
            ret.add(pushTwo);
        }

        var pieceLowX = position.getPieceAt(takeLowX);
        if (takeLowX != null && (pieceLowX != null && pieceLowX.getColor() != this.getColor() || takeLowX.equals(position.getEnPassant()))) {
            ret.add(takeLowX);
        }

        var pieceHighX = position.getPieceAt(takeHighX);
        if (takeHighX != null && (pieceHighX != null && pieceHighX.getColor() != this.getColor() || takeHighX.equals(position.getEnPassant()))) {
            ret.add(takeHighX);
        }

        return ret;
    }
}
