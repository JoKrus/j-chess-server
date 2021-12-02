package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.logic.Coordinate;
import net.jcom.jchess.server.logic.Position;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BishopTest {

    public static Stream<Triple<Coordinate, String, List<Coordinate>>> uncheckedMoveTestData() {
        return Stream.of(
                Triple.of(Coordinate.of(2, 7), "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                        new ArrayList<>()),
                Triple.of(Coordinate.of(2, 0), "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1",
                        new ArrayList<>()),
                Triple.of(Coordinate.of(2, 0), "rnbqkbnr/p1pppppp/1p6/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1",
                        List.of(Coordinate.of(1, 1), Coordinate.of(0, 2)))
        );
    }

    @ParameterizedTest
    @MethodSource(value = "uncheckedMoveTestData")
    void uncheckedMoveGenerationTest(Triple<Coordinate, String, List<Coordinate>> arguments) {
        Position position = new Position(arguments.getMiddle());
        Piece piece = position.getPieceAt(arguments.getLeft());
        var calculatedPositions = piece.possibleToMoveTo(position);

        assertEquals(CollectionUtils.getCardinalityMap(arguments.getRight()), CollectionUtils.getCardinalityMap(calculatedPositions));
    }
}
