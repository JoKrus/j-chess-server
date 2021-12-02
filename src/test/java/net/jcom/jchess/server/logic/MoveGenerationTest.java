package net.jcom.jchess.server.logic;

import net.jcom.jchess.server.generated.MoveData;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoveGenerationTest {

    public static Stream<Triple<String, Integer, Long>> moveGenTestData() {
        return Stream.of(
                Triple.of("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 1, 20L),
                Triple.of("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 2, 400L),
                Triple.of("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 3, 8_902L),
                Triple.of("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", 3, 97_862L),
                Triple.of("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 1, 44L),
                Triple.of("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 2, 1_486L),
                Triple.of("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 3, 62_379L)
        );
    }

    @ParameterizedTest
    @MethodSource(value = "moveGenTestData")
    void moveGenerationTest(Triple<String, Integer, Long> arguments) {
        Position position = new Position(arguments.getLeft());

        var res = moveGenTestRecursive(position, arguments.getMiddle());


        assertEquals(arguments.getRight(), res);
        //assertEquals(CollectionUtils.getCardinalityMap(arguments.getRight()), CollectionUtils.getCardinalityMap
        // (calculatedPositions));
    }

    private long moveGenTestRecursive(Position position, int depth) {
        if (depth == 0) {
            return 1;
        }
        List<MoveData> moveData = position.generateAllMoves(position.getCurrent());
        long numPos = 0;
        for (var data : moveData) {
            Position newPosition = new Position(position);
            newPosition.playMove(data, true);
            // ret.add(data);
            numPos += moveGenTestRecursive(newPosition, depth - 1 /*, ret*/);
        }
        return numPos;
    }
}
