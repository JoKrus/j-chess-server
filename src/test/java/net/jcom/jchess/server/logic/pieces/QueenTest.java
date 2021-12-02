package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.logic.Coordinate;
import net.jcom.jchess.server.logic.Position;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueenTest {
    public static Stream<Triple<Coordinate, String, List<Coordinate>>> uncheckedMoveTestData() {
        return Stream.of(
                Triple.of(Coordinate.of(3, 0), "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                        List.of()),
                Triple.of(Coordinate.of(3, 7), "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                        List.of()),
                Triple.of(Coordinate.of(3, 0), "rnbqkbnr/pp2pppp/8/2pp4/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                        List.of(Coordinate.parse("c7"), Coordinate.parse("b6"), Coordinate.parse("a5"),
                                Coordinate.parse("d7"), Coordinate.parse("d6")))
        );
    }

    @ParameterizedTest
    @MethodSource(value = "uncheckedMoveTestData")
    void parseTest(Triple<Coordinate, String, List<Coordinate>> arguments) {
        Position position = new Position(arguments.getMiddle());
        Piece piece = position.getPieceAt(arguments.getLeft());
        var calculatedPositions = piece.possibleToMoveToUnchecked(position);

        assertTrue(CollectionUtils.isEqualCollection(arguments.getRight(), calculatedPositions));
    }
}
