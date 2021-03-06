package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.logic.Coordinate;
import net.jcom.jchess.server.logic.Position;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PawnTest {

    public static Stream<Triple<Coordinate, String, List<Coordinate>>> uncheckedMoveTestData() {
        return Stream.of(
                Triple.of(Coordinate.of(6, 6), "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                        List.of(Coordinate.of(6, 5), Coordinate.of(6, 4))),
                Triple.of(Coordinate.of(6, 1), "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1",
                        List.of(Coordinate.of(6, 2), Coordinate.of(6, 3))),
                Triple.of(Coordinate.parse("f5"), "rnbqkbnr/pppp1p1p/6p1/4pP2/8/8/PPPPP1PP/RNBQKBNR w KQkq e6 0 5",
                        List.of(Coordinate.parse("e6"), Coordinate.parse("f6"), Coordinate.parse("g6"))),
                Triple.of(Coordinate.parse("b7"), "1rn4k/1P6/8/8/8/8/8/1K6 w - - 4 3",
                        List.of()),
                Triple.of(Coordinate.parse("b7"), "1rn5/1P5k/8/8/8/8/8/2K5 w - - 6 4",
                        List.of(Coordinate.parse("c8")))
        );
    }

    @ParameterizedTest
    @MethodSource(value = "uncheckedMoveTestData")
    void uncheckedMoveGenerationTest(Triple<Coordinate, String, List<Coordinate>> arguments) {
        Position position = new Position(arguments.getMiddle());
        Piece piece = position.getPieceAt(arguments.getLeft());
        var calculatedPositions = piece.possibleToMoveTo(position);

        assertEquals(CollectionUtils.getCardinalityMap(arguments.getRight()),
                CollectionUtils.getCardinalityMap(calculatedPositions.stream()
                        .map(moveData -> Coordinate.parse(moveData.getTo())).distinct().collect(Collectors.toList())));
    }
}
