package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.generated.MoveData;
import net.jcom.jchess.server.logic.Color;
import net.jcom.jchess.server.logic.Coordinate;
import net.jcom.jchess.server.logic.Position;

import java.util.ArrayList;
import java.util.List;

public abstract class Piece {
    private Coordinate coordinate;
    private Color color;
    private PieceType pieceType;

    public Piece(Coordinate coordinate, Color color, PieceType pieceType) {
        this.coordinate = coordinate;
        this.color = color;
        this.pieceType = pieceType;
    }

    protected abstract List<Coordinate> possibleToMoveToUnchecked(Position position);

    public List<Coordinate> possibleToMoveTo(Position position) {
        List<Coordinate> uncheckedMoves = possibleToMoveToUnchecked(position);

        List<Coordinate> toRemove = new ArrayList<>();

        for (var move : uncheckedMoves) {
            Position fakePos = new Position(position);

            MoveData moveData = new MoveData();
            moveData.setFrom(Piece.this.coordinate.toString());
            moveData.setTo(move.toString());
            //Promotion is not relevant here since it only needs to check if moving would result in a check

            fakePos.playMove(moveData, true);
            if (fakePos.playerInCheck(this.getColor()).size() > 0) {
                toRemove.add(move);
            }
        }

        uncheckedMoves.removeAll(toRemove);
        return uncheckedMoves;
    }

    public List<Coordinate> possibleToMoveToUnchecked(Position position, boolean force) {
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
