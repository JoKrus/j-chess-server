package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.generated.MoveData;
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
    protected List<MoveData> possibleToMoveToUnchecked(Position position) {
        List<MoveData> ret = new ArrayList<>();

        int x = this.getCoordinate().getX(), y = this.getCoordinate().getY();

        checkSquare(position, ret, x - 1, y - 1);
        checkSquare(position, ret, x, y - 1);
        checkSquare(position, ret, x + 1, y - 1);
        checkSquare(position, ret, x - 1, y);
        checkSquare(position, ret, x + 1, y);
        checkSquare(position, ret, x - 1, y + 1);
        checkSquare(position, ret, x, y + 1);
        checkSquare(position, ret, x + 1, y + 1);

        //add rochade only if my turn
        if (position.getCurrent() == this.getColor()) {
            checkForRochade(position, ret);
        }

        return ret;
    }

    private void checkForRochade(Position position, List<MoveData> ret) {
        //to only have to check for q and k and not distinguish between colors
        var asciiStepsToLower = this.getColor().equals(Color.WHITE) ? 0 : 'a' - 'A';
        var baseLine = this.getColor().equals(Color.WHITE) ? 7 : 0;

        //Kingside

        if (position.getPossibleRochades().indexOf(asciiStepsToLower + 'K') >= 0) {
            //Rook and King have not moved
            if (position.getPieceAt(Coordinate.of(5, baseLine)) == null && position.getPieceAt(Coordinate.of(6, baseLine)) == null) {
                //no pieces between rook and king
                List<Piece> anyReachable = new ArrayList<>();
                anyReachable.addAll(position.canMoveToSquare(Coordinate.of(4, baseLine), this.getColor().enemy()));
                anyReachable.addAll(position.canMoveToSquare(Coordinate.of(5, baseLine), this.getColor().enemy()));
                anyReachable.addAll(position.canMoveToSquare(Coordinate.of(6, baseLine), this.getColor().enemy()));

                if (anyReachable.size() == 0) {
                    ret.add(PieceHelper.coordinateToMoveData(this.getCoordinate(), Coordinate.of(6, baseLine)));
                }
            }
        }

        //Queenside

        if (position.getPossibleRochades().indexOf(asciiStepsToLower + 'Q') >= 0) {
            //Rook and King have not moved
            if (position.getPieceAt(Coordinate.of(1, baseLine)) == null &&
                    position.getPieceAt(Coordinate.of(2, baseLine)) == null &&
                    position.getPieceAt(Coordinate.of(3, baseLine)) == null) {
                //no pieces between rook and king
                List<Piece> anyReachable = new ArrayList<>();
                anyReachable.addAll(position.canMoveToSquare(Coordinate.of(2, baseLine), this.getColor().enemy()));
                anyReachable.addAll(position.canMoveToSquare(Coordinate.of(3, baseLine), this.getColor().enemy()));
                anyReachable.addAll(position.canMoveToSquare(Coordinate.of(4, baseLine), this.getColor().enemy()));

                if (anyReachable.size() == 0) {
                    ret.add(PieceHelper.coordinateToMoveData(this.getCoordinate(), Coordinate.of(2, baseLine)));
                }
            }
        }
    }

    private void checkSquare(Position position, List<MoveData> ret, int newX, int newY) {
        Coordinate coordinate = Coordinate.of(newX, newY);
        if (coordinate == null) {
            return;
        }
        Piece pieceAt = position.getPieceAt(coordinate);
        if (pieceAt == null || pieceAt.getColor() != getColor()) {
            ret.add(PieceHelper.coordinateToMoveData(this.getCoordinate(), coordinate));
        }
    }
}
