package net.jcom.jchess.server.logic.pieces;

import net.jcom.jchess.server.logic.Coordinate;
import net.jcom.jchess.server.logic.Position;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KingTest {

    public static Stream<Triple<Coordinate, String, List<Coordinate>>> uncheckedMoveTestData() {
        return Stream.of(
//                Triple.of(Coordinate.of(4, 7), "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
//                        new ArrayList<>()),
//                Triple.of(Coordinate.of(4, 0), "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1",
//                        new ArrayList<>()),
//                Triple.of(Coordinate.of(4, 0), "rnbqkbnr/pppp1ppp/4p3/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1",
//                        List.of(Coordinate.of(4, 1))),
//                Triple.of(Coordinate.parse("e1"), "r3k2r/pppq1ppp/2nbbn2/3pp3/3PP3/2NBBN2/PPPQ1PPP/R3K2R w KQkq - 10 8",
//                        List.of(Coordinate.parse("e2"), Coordinate.parse("c1"), Coordinate.parse("d1"),
//                                Coordinate.parse("f1"), Coordinate.parse("g1"))),
//                Triple.of(Coordinate.parse("e8"), "r3k2r/pppq1ppp/2nbbn2/3pp3/3PP3/2NBBN2/PPPQ1PPP/R3K2R b KQkq - 10 8",
//                        List.of(Coordinate.parse("e7"), Coordinate.parse("c8"), Coordinate.parse("d8"),
//                                Coordinate.parse("f8"), Coordinate.parse("g8"))),
                Triple.of(Coordinate.parse("e8"), "r3k2r/ppK2ppp/4P3/n5P1/NQ3B1N/bb1B3n/PPP2P1P/R2q1R2 b kq - 12 30",
                        new ArrayList<>())
//                Triple.of(Coordinate.parse("e1"), "rn3r2/pbppq1p1/1p2pN2/8/3P2NP/6P1/PPP1BP1R/R3K1k1 w Q - 5 18",
//                        List.of(Coordinate.parse("c1"), Coordinate.parse("d1"), Coordinate.parse("d2"))),
//                Triple.of(Coordinate.parse("e1"), "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8",
//                        List.of(Coordinate.parse("f1"), Coordinate.parse("d2"), Coordinate.parse("f2"), Coordinate.parse("g1")))
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
                        .map(moveData -> Coordinate.parse(moveData.getTo())).collect(Collectors.toList())));
    }
}
