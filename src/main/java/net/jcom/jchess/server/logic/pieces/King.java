package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.logic.Color;
import net.jcom.jchess.server.logic.Coordinate;
import net.jcom.jchess.server.logic.Position;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {
    public King(Coordinate coordinate, Color color) {
        super(coordinate, color, PieceType.KING);
    }

    @Override
    protected List<Coordinate> possibleToMoveToUnchecked(Position position) {
        List<Coordinate> ret = new ArrayList<>();

        int x = this.getCoordinate().getX(), y = this.getCoordinate().getY();

        checkSquare(position, ret, x - 1, y - 1);
        checkSquare(position, ret, x, y - 1);
        checkSquare(position, ret, x + 1, y - 1);
        checkSquare(position, ret, x - 1, y);
        checkSquare(position, ret, x + 1, y);
        checkSquare(position, ret, x - 1, y + 1);
        checkSquare(position, ret, x, y + 1);
        checkSquare(position, ret, x + 1, y + 1);

        //add rochade

        //to only have to check for q and k and not distinguish between colors
        var asciiStepsToLower = this.getColor().equals(Color.WHITE) ? 'a' - 'A' : 0;
        var baseLine = this.getColor().equals(Color.WHITE) ? 7 : 0;

        //Kingside

        if (position.getPossibleRochades().indexOf(asciiStepsToLower + 'K') >= 0) {
            //Rook and King have not moved
            if (position.getPieceAt(Coordinate.of(5, baseLine)) == null && position.getPieceAt(Coordinate.of(6, baseLine)) == null) {
                //no pieces between rook and king
                //TODO check if no square is under attack/check
                ret.add(Coordinate.of(6, baseLine));
            }
        }

        //Queenside

        if (position.getPossibleRochades().indexOf(asciiStepsToLower + 'Q') != 0) {
            //Rook and King have not moved
            if (position.getPieceAt(Coordinate.of(1, baseLine)) == null &&
                    position.getPieceAt(Coordinate.of(2, baseLine)) == null &&
                    position.getPieceAt(Coordinate.of(3, baseLine)) == null) {
                //no pieces between rook and king
                //TODO check if no square is under attack/check
                ret.add(Coordinate.of(2, baseLine));
            }
        }

        return ret;
    }

    private void checkSquare(Position position, List<Coordinate> ret, int newX, int newY) {
        Coordinate coordinate = Coordinate.of(newX, newY);
        if (coordinate == null) {
            return;
        }
        Piece pieceAt = position.getPieceAt(coordinate);
        if (pieceAt == null || pieceAt.getColor() != getColor()) {
            ret.add(coordinate);
        }
    }
}
