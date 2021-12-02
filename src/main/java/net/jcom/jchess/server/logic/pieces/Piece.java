package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.logic.Color;
import net.jcom.jchess.server.logic.Coordinate;
import net.jcom.jchess.server.logic.Position;

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

    //TODO filter if position results in check
    protected abstract List<Coordinate> possibleToMoveToUnchecked(Position position);

    public Coordinate getCoordinate() {
        return this.coordinate;
    }

    public Color getColor() {
        return this.color;
    }

    public PieceType getPieceType() {
        return this.pieceType;
    }
}
