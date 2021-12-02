package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.generated.MoveData;
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
    protected List<MoveData> possibleToMoveToUnchecked(Position position) {
        List<MoveData> ret = new ArrayList<>();

        int x = this.getCoordinate().getX(), y = this.getCoordinate().getY();

        int yDir = this.getColor().equals(Color.WHITE) ? -1 : 1;
        int baseLine = this.getColor().equals(Color.WHITE) ? 6 : 1;
        int endLine = this.getColor().equals(Color.WHITE) ? 0 : 7;

        var pushOne = Coordinate.of(x, y + yDir);
        var pushTwo = Coordinate.of(x, y + yDir * 2);
        var takeLowX = Coordinate.of(x - 1, y + yDir);
        var takeHighX = Coordinate.of(x + 1, y + yDir);

        if (pushOne != null && position.getPieceAt(pushOne) == null) {
            ret.addAll(checkEndline(pushOne, endLine));
        }

        if (pushTwo != null && y == baseLine && position.getPieceAt(pushOne) == null && position.getPieceAt(pushTwo) == null) {
            ret.addAll(checkEndline(pushTwo, endLine));
        }

        var pieceLowX = position.getPieceAt(takeLowX);
        if (takeLowX != null && (pieceLowX != null && pieceLowX.getColor() != this.getColor() || takeLowX.equals(position.getEnPassant()))) {
            ret.addAll(checkEndline(takeLowX, endLine));
        }

        var pieceHighX = position.getPieceAt(takeHighX);
        if (takeHighX != null && (pieceHighX != null && pieceHighX.getColor() != this.getColor() || takeHighX.equals(position.getEnPassant()))) {
            ret.addAll(checkEndline(takeHighX, endLine));
        }

        return ret;
    }

    private List<MoveData> checkEndline(Coordinate checkEndline, int endline) {
        List<MoveData> ret = new ArrayList<>();

        if (checkEndline.getY() == endline) {
            ret.add(PieceHelper.coordinateToMoveData(this.getCoordinate(), checkEndline, "q"));
            ret.add(PieceHelper.coordinateToMoveData(this.getCoordinate(), checkEndline, "r"));
            ret.add(PieceHelper.coordinateToMoveData(this.getCoordinate(), checkEndline, "b"));
            ret.add(PieceHelper.coordinateToMoveData(this.getCoordinate(), checkEndline, "n"));
        } else {
            ret.add(PieceHelper.coordinateToMoveData(this.getCoordinate(), checkEndline));
        }

        return ret;
    }
}
