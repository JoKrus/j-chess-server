package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.generated.MoveData;
import net.jcom.jchess.server.logic.Color;
import net.jcom.jchess.server.logic.Coordinate;
import net.jcom.jchess.server.logic.Position;

import java.util.ArrayList;
import java.util.List;

public abstract class Piece {
    private Coordinate coordinate;
    private final Color color;
    private final PieceType pieceType;

    public Piece(Coordinate coordinate, Color color, PieceType pieceType) {
        this.coordinate = coordinate;
        this.color = color;
        this.pieceType = pieceType;
    }

    public abstract List<MoveData> possibleToMoveToUnchecked(Position position);

    public List<MoveData> possibleToMoveTo(Position position) {
        List<MoveData> uncheckedMoves = possibleToMoveToUnchecked(position);

        List<MoveData> toRemove = new ArrayList<>();

        for (var move : uncheckedMoves) {
            Position fakePos = new Position(position);

            //Promotion is not relevant here since it only needs to check if moving would result in a check

            fakePos.playMove(move, true);

            if (fakePos.playerInCheck(this.getColor()).size() > 0) {
                toRemove.add(move);
            }
        }

        uncheckedMoves.removeAll(toRemove);
        return uncheckedMoves;
    }

    public List<MoveData> possibleToMoveToUnchecked(Position position, boolean force) {
        return possibleToMoveToUnchecked(position);
    }

    public Coordinate getCoordinate() {
        return this.coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public Color getColor() {
        return this.color;
    }

    public PieceType getPieceType() {
        return this.pieceType;
    }
}
